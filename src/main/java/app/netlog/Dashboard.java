package app.netlog;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.nio.file.*;
import java.util.*;

/** JavaFX dashboard for network logs and Wireshark captures. */
public final class Dashboard extends Application {
    private final ObservableList<Packet> rows=FXCollections.observableArrayList();
    private final TableView<Packet> table=new TableView<Packet>(rows);
    private final TextArea summary=new TextArea();
    private final TextField threshold=new TextField("1000");
    private final Label status=new Label("Open a log or capture to begin.");
    private List<Packet> packets=new ArrayList<Packet>(); private long activeThreshold=1000; private Path currentInput;
    @Override public void start(Stage stage){stage.setTitle("Network Log Analyzer");BorderPane root=new BorderPane();root.setTop(new VBox(menu(stage),controls()));
        TableColumn<Packet,String> time=new TableColumn<Packet,String>("Timestamp");time.setCellValueFactory(c->new javafx.beans.property.SimpleStringProperty(c.getValue().timestamp.toString()));
        TableColumn<Packet,String> src=new TableColumn<Packet,String>("Source");src.setCellValueFactory(c->new javafx.beans.property.SimpleStringProperty(c.getValue().source));
        TableColumn<Packet,String> dst=new TableColumn<Packet,String>("Destination");dst.setCellValueFactory(c->new javafx.beans.property.SimpleStringProperty(c.getValue().destination));
        TableColumn<Packet,String> protocol=new TableColumn<Packet,String>("Protocol");protocol.setCellValueFactory(c->new javafx.beans.property.SimpleStringProperty(c.getValue().protocol));
        TableColumn<Packet,Long> size=new TableColumn<Packet,Long>("Bytes");size.setCellValueFactory(c->new javafx.beans.property.SimpleObjectProperty<Long>(c.getValue().bytes));table.getColumns().addAll(time,src,dst,protocol,size);table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        summary.setEditable(false);summary.setPrefRowCount(8);VBox bottom=new VBox(5,new Label("Analysis summary and alerts"),summary,status);bottom.setPadding(new Insets(8));root.setCenter(table);root.setBottom(bottom);stage.setScene(new Scene(root,1050,680));stage.show();}
    private MenuBar menu(Stage s){Menu file=new Menu("File");MenuItem open=new MenuItem("Open log or capture...");open.setOnAction(e->open(s));MenuItem save=new MenuItem("Save session...");save.setOnAction(e->save(s));MenuItem load=new MenuItem("Load session...");load.setOnAction(e->load(s));MenuItem csv=new MenuItem("Export packets as CSV...");csv.setOnAction(e->exportCsv(s));MenuItem txt=new MenuItem("Export summary as TXT...");txt.setOnAction(e->exportTxt(s));MenuItem bin=new MenuItem("Write binary packet file...");bin.setOnAction(e->writeBinary(s));MenuItem copy=new MenuItem("Copy current input...");copy.setOnAction(e->copy(s));MenuItem zip=new MenuItem("Archive current input as ZIP...");zip.setOnAction(e->zip(s));file.getItems().addAll(open,save,load,new SeparatorMenuItem(),csv,txt,bin,new SeparatorMenuItem(),copy,zip);return new MenuBar(file);}
    private HBox controls(){threshold.setPrefColumnCount(10);Button apply=new Button("Apply threshold");apply.setOnAction(e->analyze());Button charts=new Button("View charts");charts.setOnAction(e->charts());HBox box=new HBox(10,new Label("Alert threshold (bytes per source):"),threshold,apply,charts);box.setPadding(new Insets(10));box.setAlignment(Pos.CENTER_LEFT);return box;}
    private void open(Stage s){FileChooser fc=new FileChooser();fc.setTitle("Open log or Wireshark capture");fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Logs and captures","*.log","*.txt","*.pcap","*.pcapng"),new FileChooser.ExtensionFilter("All files","*.*"));java.io.File f=fc.showOpenDialog(s);if(f==null)return;try{currentInput=f.toPath();String n=f.getName().toLowerCase(Locale.ROOT);if(n.endsWith(".pcap")||n.endsWith(".pcapng")){packets=new CaptureReader().read(currentInput);status.setText("Loaded "+packets.size()+" IP packets from "+f.getName());}else{LogParser.Result r=new LogParser().parse(currentInput);packets=r.packets;status.setText("Loaded "+packets.size()+" records; malformed rows: "+r.errors.size());if(!r.errors.isEmpty())alert("Input warnings",String.join("\n",r.errors.subList(0,Math.min(20,r.errors.size()))));}rows.setAll(packets);analyze();}catch(Exception ex){alert("Could not open file",ex.getMessage());}}
    private void analyze(){try{activeThreshold=Long.parseLong(threshold.getText().trim());if(activeThreshold<0)throw new NumberFormatException();}catch(NumberFormatException e){alert("Invalid threshold","Enter a non-negative whole number.");return;}Analyzer.Summary x=new Analyzer().analyze(packets,activeThreshold);StringBuilder b=new StringBuilder("Packets: ").append(x.totalPackets).append("    Total bytes: ").append(x.totalBytes).append("\n\nTraffic by protocol\n");x.byProtocol.forEach((k,v)->b.append("  ").append(k).append(": ").append(v).append(" bytes\n"));b.append("\nTraffic by source\n");x.bySource.forEach((k,v)->b.append("  ").append(k).append(": ").append(v).append(" bytes\n"));b.append("\nAlerts (threshold ").append(activeThreshold).append(")\n");if(x.alerts.isEmpty())b.append("  No sources meet the threshold.");else for(String a:x.alerts)b.append("  ⚠ ").append(a).append('\n');summary.setText(b.toString());}
    private void charts(){if(packets.isEmpty()){alert("No data","Open a log first.");return;}Analyzer.Summary x=new Analyzer().analyze(packets,activeThreshold);Stage stage=new Stage();stage.setTitle("Traffic charts");HBox h=new HBox(chart("Bytes by protocol",x.byProtocol),chart("Bytes by source",x.bySource));h.setSpacing(16);h.setPadding(new Insets(12));stage.setScene(new Scene(h,900,520));stage.show();}
    private BarChart<String,Number> chart(String title,Map<String,Long> values){CategoryAxis x=new CategoryAxis();NumberAxis y=new NumberAxis();BarChart<String,Number> c=new BarChart<String,Number>(x,y);c.setTitle(title);XYChart.Series<String,Number>s=new XYChart.Series<String,Number>();values.forEach((k,v)->s.getData().add(new XYChart.Data<String,Number>(k,v)));c.getData().add(s);return c;}
    private Path savePath(Stage s,String title,String ext){FileChooser f=new FileChooser();f.setTitle(title);f.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext.toUpperCase(Locale.ROOT),"*."+ext));java.io.File x=f.showSaveDialog(s);return x==null?null:x.toPath();}
    private Path openPath(Stage s,String title,String ext){FileChooser f=new FileChooser();f.setTitle(title);f.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext.toUpperCase(Locale.ROOT),"*."+ext));java.io.File x=f.showOpenDialog(s);return x==null?null:x.toPath();}
    private void save(Stage s){Path p=savePath(s,"Save session","ser");if(p!=null)run(()->new SessionStore().save(p,packets,activeThreshold));}
    private void load(Stage s){Path p=openPath(s,"Load session","ser");if(p==null)return;try{SessionStore.AnalysisSession z=new SessionStore().load(p);packets=z.packets;rows.setAll(packets);threshold.setText(String.valueOf(z.threshold));analyze();status.setText("Session loaded: "+p.getFileName());}catch(Exception e){alert("Load failed",e.getMessage());}}
    private void exportCsv(Stage s){Path p=savePath(s,"Export packet CSV","csv");if(p!=null)run(()->new ReportExporter().csv(p,packets));}
    private void exportTxt(Stage s){Path p=savePath(s,"Export summary","txt");if(p!=null)run(()->new ReportExporter().text(p,new Analyzer().analyze(packets,activeThreshold)));}
    private void writeBinary(Stage s){Path p=savePath(s,"Write packet binary","dat");if(p!=null)run(()->new PacketBinary().write(p,packets));}
    private void copy(Stage s){if(currentInput==null){alert("No input","Open a log or capture first.");return;}Path p=savePath(s,"Copy input","copy");if(p!=null)run(()->new FileTools().copy(currentInput,p));}
    private void zip(Stage s){if(currentInput==null){alert("No input","Open a log or capture first.");return;}Path p=savePath(s,"Archive input","zip");if(p!=null)run(()->new FileTools().zip(currentInput,p));}
    private void run(IOAction a){try{a.run();status.setText("Action completed successfully.");}catch(Exception e){alert("Operation failed",e.getMessage());}}
    private interface IOAction{void run()throws Exception;}
    private void alert(String title,String message){new Alert(Alert.AlertType.INFORMATION,message,ButtonType.OK).showAndWait();}
}
