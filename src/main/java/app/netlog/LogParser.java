package app.netlog;
import java.io.*; import java.nio.charset.StandardCharsets; import java.nio.file.*; import java.time.Instant; import java.util.*;
public final class LogParser {
 public static final class Result { public final List<Packet> packets=new ArrayList<Packet>(); public final List<String> errors=new ArrayList<String>(); }
 public Result parse(Path path) throws IOException { Result r=new Result(); try(BufferedReader in=Files.newBufferedReader(path,StandardCharsets.UTF_8)){ String s; int n=0; while((s=in.readLine())!=null){ n++; s=s.trim(); if(s.length()==0||s.startsWith("#"))continue; try { String[] f=s.split(",",-1); if(f.length!=5)throw new IllegalArgumentException("expected 5 comma-separated fields"); r.packets.add(new Packet(Instant.parse(f[0].trim()),f[1],f[2],f[3],Long.parseLong(f[4].trim()))); } catch(Exception e){r.errors.add("Line "+n+": "+e.getMessage());} } } return r; }
}
