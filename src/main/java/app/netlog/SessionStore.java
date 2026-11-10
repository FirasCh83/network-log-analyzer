package app.netlog;
import java.io.*;import java.nio.file.*;import java.util.*;
public final class SessionStore { public void save(Path p,List<Packet> packets,long threshold)throws IOException{try(ObjectOutputStream o=new ObjectOutputStream(Files.newOutputStream(p))){o.writeObject(new AnalysisSession(packets,threshold));}} public AnalysisSession load(Path p)throws IOException{try(ObjectInputStream i=new ObjectInputStream(Files.newInputStream(p))){try{return (AnalysisSession)i.readObject();}catch(ClassNotFoundException e){throw new IOException("Unknown session format",e);}}}
 public static final class AnalysisSession implements Serializable{private static final long serialVersionUID=1L;public final List<Packet> packets;public final long threshold;AnalysisSession(List<Packet> p,long t){packets=new ArrayList<Packet>(p);threshold=t;}}
}
