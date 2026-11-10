package app.netlog;
import java.util.*;
public final class Analyzer {
 public static final class Summary { public final Map<String,Long> byProtocol=new TreeMap<String,Long>(), bySource=new TreeMap<String,Long>(); public final List<String> alerts=new ArrayList<String>(); public long totalPackets,totalBytes; }
 public Summary analyze(List<Packet> ps,long threshold){ Summary s=new Summary(); s.totalPackets=ps.size(); for(Packet p:ps){s.totalBytes+=p.bytes; add(s.byProtocol,p.protocol,p.bytes); add(s.bySource,p.source,p.bytes);} for(Map.Entry<String,Long> e:s.bySource.entrySet())if(e.getValue()>=threshold)s.alerts.add("High traffic from "+e.getKey()+": "+e.getValue()+" bytes"); return s; }
 private void add(Map<String,Long> m,String k,long v){Long n=m.get(k);m.put(k,(n==null?0:n)+v);}
}
