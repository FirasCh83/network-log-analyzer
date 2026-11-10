# Project Initialization Guide

## Project purpose

Network Log Analyzer is a JavaFX desktop application for importing network logs and packet captures, analyzing traffic, displaying results, and exporting or saving data. It combines the group's network topic with the course's four I/O themes: character streams, byte streams, binary data, and object streams.

## Current agreed features

1. Open `.log` and `.txt` text logs.
2. Open common Wireshark `.pcap` and `.pcapng` captures.
3. Convert supported records into one shared `Packet` model and display them in a sortable JavaFX table.
4. Summarize bytes by protocol and source address.
5. Flag sources that meet or exceed a configurable byte threshold.
6. Display JavaFX bar charts by protocol and source.
7. Export CSV/TXT reports, save/load sessions, and write the custom binary packet file.
8. Copy and ZIP the opened source file.

## Prerequisites and activation

Use JDK 17 or newer and Apache Maven 3.9 or newer. Maven resolves the JavaFX libraries from the `pom.xml`; there is no need to install OpenJFX separately.

From the project root, compile and start the JavaFX application:

```powershell
mvn clean compile
mvn javafx:run
```

If Maven is extracted under `C:\Users\user\tools` and not added to PATH:

```powershell
& "C:\Users\user\tools\apache-maven-3.10.0\bin\mvn.cmd" clean compile
& "C:\Users\user\tools\apache-maven-3.10.0\bin\mvn.cmd" javafx:run
```

The first run needs network access so Maven can download JavaFX and plugins.

## Demonstration workflow

1. Launch the app and choose **File → Open log or capture...**.
2. Load `samples/demo-capture.pcap` or `samples/sample.log`.
3. Review and sort the packet table.
4. Set a threshold and apply it to refresh summaries and alerts.
5. Open the charts to compare protocol and source traffic.
6. Demonstrate CSV/TXT export, binary packet output, session save/load, copy, or ZIP from the File menu.

## Input formats

Text logs use one comma-separated record per line:

`2026-11-10T09:15:00Z,192.168.1.10,10.0.0.5,TCP,512`

Fields: timestamp, source, destination, protocol, byte count. Blank/comment lines are ignored and invalid lines are reported.

Capture import currently supports common classic PCAP and PCAPNG Ethernet/raw-IP captures. It extracts IP addresses and protocol identifiers and counts captured frame bytes; it is not a full protocol dissector and does not handle every capture type.

## I/O mapping to the course

- Character streams: `BufferedReader` parses text; `PrintWriter` exports reports.
- Byte streams: file copy and ZIP archive operations.
- Binary data: `DataInputStream`/`DataOutputStream` use magic `NLOG`, version, record count, and packet fields; `CaptureReader` reads capture structures.
- Object streams: `AnalysisSession` is stored with `ObjectOutputStream` and loaded with `ObjectInputStream`.
- Collections: maps aggregate bytes by protocol/source; lists hold packet records.
- Exceptions: malformed input and file errors are surfaced with context.

## Suggested six-person division

1. Text parsing and reports.
2. Copy and ZIP operations.
3. Binary packet format and capture reader.
4. Session serialization.
5. JavaFX dashboard, table, and charts.
6. Integration, error handling, documentation, and demo coordination.

Coordinate changes through feature branches and pull requests. Do not commit generated `target/` output or captures containing private traffic. Only load `.ser` sessions from trusted sources.
