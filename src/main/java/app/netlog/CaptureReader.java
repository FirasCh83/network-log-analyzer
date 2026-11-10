package app.netlog;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Reads classic PCAP and common PCAPNG Ethernet captures without third-party libraries. */
public final class CaptureReader {
    public List<Packet> read(Path path) throws IOException {
        byte[] bytes=Files.readAllBytes(path); if(bytes.length<4)throw new IOException("Capture file is too short");
        if(bytes[0]==(byte)0x0a&&bytes[1]==0x0d&&bytes[2]==0x0d&&bytes[3]==0x0a)return pcapng(bytes);
        return pcap(bytes);
    }
    private List<Packet> pcap(byte[] a)throws IOException{
        int m=ByteBuffer.wrap(a,0,4).order(ByteOrder.BIG_ENDIAN).getInt();ByteOrder o;boolean nano;
        if(m==0xa1b2c3d4){o=ByteOrder.BIG_ENDIAN;nano=false;}else if(m==0xd4c3b2a1){o=ByteOrder.LITTLE_ENDIAN;nano=false;}else if(m==0xa1b23c4d){o=ByteOrder.BIG_ENDIAN;nano=true;}else if(m==0x4d3cb2a1){o=ByteOrder.LITTLE_ENDIAN;nano=true;}else throw new IOException("Unknown capture format (expected PCAP or PCAPNG)");
        ByteBuffer b=ByteBuffer.wrap(a).order(o);if(a.length<24)throw new IOException("Truncated PCAP header");((Buffer)b).position(20);int link=b.getInt();List<Packet> out=new ArrayList<Packet>();
        while(b.remaining()>0){if(b.remaining()<16)throw new IOException("Truncated PCAP packet header");long sec=b.getInt()&0xffffffffL,frac=b.getInt()&0xffffffffL;int cap=b.getInt();b.getInt();if(cap<0||cap>b.remaining())throw new IOException("Invalid/truncated PCAP packet length");byte[] frame=new byte[cap];b.get(frame);Packet p=decode(frame,link,sec*1000000000L+(nano?frac:frac*1000L));if(p!=null)out.add(p);}return out;
    }
    private List<Packet> pcapng(byte[] a)throws IOException{
        List<Packet> out=new ArrayList<Packet>();List<Integer> links=new ArrayList<Integer>();List<Boolean> nanos=new ArrayList<Boolean>();ByteOrder o=null;int pos=0;
        while(pos+12<=a.length){boolean shb=a[pos]==10&&a[pos+1]==13&&a[pos+2]==13&&a[pos+3]==10;if(shb){if(pos+28>a.length)throw new IOException("Truncated PCAPNG section header");int bom=ByteBuffer.wrap(a,pos+8,4).order(ByteOrder.BIG_ENDIAN).getInt();o=bom==0x1a2b3c4d?ByteOrder.BIG_ENDIAN:bom==0x4d3c2b1a?ByteOrder.LITTLE_ENDIAN:null;if(o==null)throw new IOException("Invalid PCAPNG byte order");}
            if(o==null)throw new IOException("PCAPNG missing section header");ByteBuffer b=ByteBuffer.wrap(a).order(o);((Buffer)b).position(pos+4);int type=b.getInt(),len=b.getInt();if(len<12||pos+len>a.length)throw new IOException("Invalid/truncated PCAPNG block");
            if(type==1){if(len<20)throw new IOException("Invalid interface block");((Buffer)b).position(pos+8);links.add(b.getShort()&65535);nanos.add(Boolean.TRUE);}
            else if(type==6){if(len<32)throw new IOException("Invalid enhanced packet block");((Buffer)b).position(pos+8);int iface=b.getInt();long hi=b.getInt()&0xffffffffL,lo=b.getInt()&0xffffffffL;int cap=b.getInt();b.getInt();if(iface<0||iface>=links.size()||cap<0||cap>len-32)throw new IOException("Invalid enhanced packet record");byte[] frame=new byte[cap];b.get(frame);long ticks=(hi<<32)|lo;long ns=nanos.get(iface)?ticks*1000L:ticks;Packet p=decode(frame,links.get(iface),ns);if(p!=null)out.add(p);}
            pos+=len;
        }return out;
    }
    private Packet decode(byte[] f,int link,long ns)throws IOException{
        int off;if(link==1){if(f.length<14)return null;int type=u16(f,12);off=14;while(type==0x8100||type==0x88a8){if(f.length<off+4)return null;type=u16(f,off+2);off+=4;}if(type==0x0800)return ipv4(f,off,ns);if(type==0x86dd)return ipv6(f,off,ns);return null;}if(link==101)return f.length>0&&(f[0]>>4)==4?ipv4(f,0,ns):ipv6(f,0,ns);return null;
    }
    private Packet ipv4(byte[] f,int o,long ns)throws IOException{if(f.length<o+20||(f[o]>>4)!=4)return null;int h=(f[o]&15)*4;if(h<20||f.length<o+h)return null;String proto=protocol(f[o+9]&255);String s=addr(Arrays.copyOfRange(f,o+12,o+16)),d=addr(Arrays.copyOfRange(f,o+16,o+20));return new Packet(Instant.ofEpochSecond(ns/1000000000L,ns%1000000000L),s,d,proto,Math.max(0,f.length-o));}
    private Packet ipv6(byte[] f,int o,long ns)throws IOException{if(f.length<o+40||(f[o]>>4)!=6)return null;String s=addr(Arrays.copyOfRange(f,o+8,o+24)),d=addr(Arrays.copyOfRange(f,o+24,o+40));return new Packet(Instant.ofEpochSecond(ns/1000000000L,ns%1000000000L),s,d,protocol(f[o+6]&255),Math.max(0,f.length-o));}
    private int u16(byte[] b,int p){return ((b[p]&255)<<8)|(b[p+1]&255);}private String addr(byte[] b)throws IOException{try{return InetAddress.getByAddress(b).getHostAddress();}catch(Exception e){throw new IOException(e);}}
    private String protocol(int p){switch(p){case 1:return "ICMP";case 6:return "TCP";case 17:return "UDP";case 58:return "ICMPv6";default:return "IP-"+p;}}
}
