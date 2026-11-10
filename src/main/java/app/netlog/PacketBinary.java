package app.netlog;
import java.io.*; import java.nio.file.*; import java.util.*;
public final class PacketBinary {
 private static final int MAGIC=0x4e4c4f47,VERSION=1;
 public void write(Path path,List<Packet> ps)throws IOException{try(DataOutputStream d=new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))){d.writeInt(MAGIC);d.writeInt(VERSION);d.writeInt(ps.size());for(Packet p:ps){d.writeLong(p.timestamp.toEpochMilli());d.writeUTF(p.source);d.writeUTF(p.destination);d.writeUTF(p.protocol);d.writeLong(p.bytes);}}}
 public List<Packet> read(Path path)throws IOException{try(DataInputStream d=new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))){if(d.readInt()!=MAGIC||d.readInt()!=VERSION)throw new IOException("Invalid NLOG binary header/version");int n=d.readInt();if(n<0||n>10000000)throw new IOException("Invalid record count");List<Packet> ps=new ArrayList<Packet>();for(int i=0;i<n;i++)ps.add(new Packet(java.time.Instant.ofEpochMilli(d.readLong()),d.readUTF(),d.readUTF(),d.readUTF(),d.readLong()));return ps;}}
}
