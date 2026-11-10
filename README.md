# Network Log Analyzer

A JavaFX desktop application for analyzing network text logs and Wireshark packet captures. The project demonstrates Java character, byte, binary, and object I/O through a practical network traffic dashboard.

## Features

- Open text logs (`.log`, `.txt`) and common captures (`.pcap`, `.pcapng`).
- Display imported packets in a sortable JavaFX table.
- Aggregate captured bytes by protocol and source address.
- Flag sources whose traffic meets or exceeds a configurable threshold.
- Display JavaFX bar charts by protocol and source.
- Export packet rows to CSV and analysis summaries to TXT.
- Save and load analysis sessions; write packets in the project's binary format.
- Copy the opened input and create a ZIP archive.

The repository includes `samples/demo-capture.pcap` and `samples/sample.log` for a quick demo.

## Requirements

- JDK 17 or newer (the project uses Java 17; JDK 25 also works).
- Apache Maven 3.9 or newer.
- Git.
- Windows commands below use PowerShell. Maven downloads JavaFX dependencies on the first build, so internet access is needed then.

## Clone and launch

Get the repository URL from a teammate, then:

```powershell
git clone <repository-url>
cd network-log-analyzer
mvn clean compile
mvn javafx:run
```

If Maven is extracted locally and not on PATH, replace `mvn` with its full command path. For example:

```powershell
& "C:\Users\user\tools\apache-maven-3.10.0\bin\mvn.cmd" clean compile
& "C:\Users\user\tools\apache-maven-3.10.0\bin\mvn.cmd" javafx:run
```

The first Maven run downloads JavaFX and build plugins. The `pom.xml` manages those dependencies; teammates do not need to install OpenJFX separately.

## Use the dashboard

1. Choose **File → Open log or capture...**.
2. Select `samples\demo-capture.pcap` or `samples\sample.log`, or choose your own supported file.
3. Browse and sort packet rows in the table.
4. Set **Alert threshold (bytes per source)** and choose **Apply threshold**. Sources whose byte totals meet or exceed the threshold appear in the summary alerts.
5. Choose **View charts** to see traffic by protocol and source.
6. Use the **File** menu to save/load sessions, export CSV/TXT, write binary data, copy the input, or create a ZIP.

## Input formats and limitations

Text records have five comma-separated fields per line:

```text
2026-11-10T09:15:00Z,192.168.1.10,10.0.0.5,TCP,512
```

The fields are ISO-8601 timestamp, source address, destination address, protocol, and byte count. Blank lines and comments beginning with `#` are skipped; malformed lines are reported.

The capture reader supports classic PCAP and common PCAPNG Ethernet/raw-IP captures. It extracts IPv4/IPv6 addresses and IP protocol numbers; byte totals use captured frame lengths. It does not decode application payloads or support every capture link type or timestamp resolution.

## Project layout

```text
src/main/java/app/netlog/  Java source
samples/                   Demo log and PCAP capture
data/                      Demo output directory
pom.xml                    Maven and JavaFX configuration
init.md                    Project scope, workflow, and course mapping
```

Main classes: `Packet` is the shared model; `LogParser` reads text; `CaptureReader` reads captures; `Analyzer` aggregates traffic and alerts; `Dashboard` contains the JavaFX interface; `ReportExporter`, `PacketBinary`, `SessionStore`, and `FileTools` implement the outputs and file operations.

## Git workflow for teammates

Create a branch for your task:

```powershell
git switch -c feature/your-task
```

Compile before committing, then push your branch:

```powershell
mvn clean compile
git add README.md init.md pom.xml src samples
git commit -m "Describe your change"
git push -u origin feature/your-task
```

Open a pull request for team review. Do not commit generated `target/` files or private captures containing sensitive traffic. Share only captures you are allowed to distribute.

## Course concepts

- Character I/O: `BufferedReader` parses logs; `PrintWriter` exports CSV/TXT.
- Byte I/O: file copying and ZIP streams.
- Binary I/O: `DataInputStream`/`DataOutputStream` implement the custom `NLOG` format; `CaptureReader` reads PCAP structures.
- Object I/O: `ObjectInputStream`/`ObjectOutputStream` store analysis sessions.
- Collections and exceptions: collections aggregate traffic; malformed or unreadable input produces errors.

Only load `.ser` sessions from trusted sources because arbitrary Java object deserialization is unsafe.
