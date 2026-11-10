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

## Install prerequisites on Windows

You need JDK 17 or newer, Maven 3.9 or newer, and Git. JavaFX is fetched by Maven from `pom.xml`; teammates do not need a separate JavaFX SDK. Official guides: [Temurin JDK downloads](https://adoptium.net/temurin/releases/), [Apache Maven install](https://maven.apache.org/install.html), [OpenJFX Maven setup](https://openjfx.io/openjfx-docs/), and [Git for Windows](https://git-scm.com/download/win).

### Install and verify Java

Install a Windows x64 Temurin JDK 17, 21, or 25. If offered, enable adding Java to PATH and setting `JAVA_HOME`. Open a new PowerShell window and check:

```powershell
java -version
javac -version
echo $env:JAVA_HOME
```

Both Java commands must report version 17 or newer. If `JAVA_HOME` is empty, set it for the current PowerShell window to the actual JDK folder, then prepend its `bin` directory to PATH:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.x-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Replace the example folder name with the one installed on your computer.

### Install and verify Maven

Download the Maven **binary ZIP** from the Apache install page and extract it, for example to `C:\Users\user\tools\apache-maven-3.10.0`. Verify it directly:

```powershell
& "C:\Users\user\tools\apache-maven-3.10.0\bin\mvn.cmd" -version
```

Maven should report its version and Java 17 or newer. Alternatively, add Maven's `bin` directory to PATH and use `mvn -version`.

### Install and verify Git

Install Git for Windows, open a new PowerShell window, and run `git --version`.

## Clone, build, and launch

Clone the team repository, then enter it:

```powershell
git clone https://github.com/FirasCh83/network-log-analyzer.git
cd network-log-analyzer
```

Build and launch from the repository root:

```powershell
& "C:\Users\user\tools\apache-maven-3.10.0\bin\mvn.cmd" clean compile
& "C:\Users\user\tools\apache-maven-3.10.0\bin\mvn.cmd" javafx:run
```

If `mvn` is on PATH, use `mvn clean compile` and `mvn javafx:run` instead. The first build needs internet access so Maven can download JavaFX and build plugins. The second command opens the dashboard. Close its window to exit.

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
