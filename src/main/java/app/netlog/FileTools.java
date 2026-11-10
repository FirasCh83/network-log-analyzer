package app.netlog;
import java.io.*; import java.nio.charset.StandardCharsets; import java.nio.file.*; import java.util.*; import java.util.zip.*;
public final class FileTools {
 public void copy(Path from,Path to)throws IOException{long size=Files.size(from),done=0;try(InputStream in=Files.newInputStream(from);OutputStream out=Files.newOutputStream(to)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);done+=n;System.out.println("Copy progress: "+(size==0?100:done*100/size)+"%");}}}
 public void zip(Path from,Path zip)throws IOException{try(ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(zip))){out.putNextEntry(new ZipEntry(from.getFileName().toString()));try(InputStream in=Files.newInputStream(from)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}out.closeEntry();}}
}
