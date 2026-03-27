# Space Flight Support POC

## Project Purpose

This repository contains a focused JavaFX proof of concept for the **Space Flight** phase of a fictional space tourism company. The prototype supports one core process only:

**In-Flight Passenger Support and Emergency Escalation**

The goal is to show how passengers, onboard support staff, and base-station staff can manage in-flight incidents in a structured, traceable, and presentation-ready way.

## Problem Statement

During the actual space flight, passengers may experience:

- fear, stress, or panic
- nausea or motion sickness
- abnormal health symptoms
- situations where onboard support becomes unavailable

The existing process is assumed to be too ad hoc. This prototype introduces a workflow-driven process with status transitions, escalation rules, and transparent action logging.

## Personas

- Passenger: reports discomfort and receives support through the seat display
- Onboard Support: handles normal in-flight cases
- Base Station Operator: takes over escalated or fallback cases
- Remote Doctor / Psychologist: currently documented as mocked extension roles for escalated cases

Detailed personas are documented in [docs/PERSONAS.md](docs/PERSONAS.md).

## Implemented Features

- JavaFX desktop application
- two interactive views: `Mission Control` and `Passenger Console`
- passenger overview panel
- seat-display inspired passenger dashboard with interactive help buttons
- incident creation form
- drag-and-drop incident workflow board
- workflow states: `New`, `Assessing`, `Monitoring`, `Escalated`, `Resolved`
- onboard support handling for normal incidents
- automatic escalation of `CRITICAL` incidents
- fallback escalation when onboard support is unavailable
- flight-phase handling for launch, orbit, and landing constraints
- launch and landing exception handling when crew movement is blocked
- base-station takeover for escalated cases
- incident detail panel and action history
- in-memory repositories with realistic demo data
- JUnit tests for the most important business rules

## Mocked Features

- real medical devices and suit telemetry
- spacecraft control systems
- network communication between spacecraft and base station
- external database persistence
- AI diagnosis and analytics
- full doctor / psychologist workflows beyond UI placeholders and documentation
- advanced authentication and authorization

## Architecture Summary

The project uses a small layered architecture:

- `domain`: entities, enums, domain exceptions
- `repository`: repository abstractions and in-memory implementations
- `service`: business rules and use-case logic
- `ui`: JavaFX presentation layer
- `app`: bootstrap, logging configuration, demo data

The design deliberately follows **SOLID**, **DRY**, **KISS**, and **YAGNI**:

- UI does not contain business rules.
- services contain workflow and escalation logic.
- repositories are abstractions.
- enums model stable workflow concepts.
- the scope stays small and demo-focused.

More detail is available in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## How to Run

Prerequisites:

- Java 25
- internet connection for the first Maven-wrapper download

The repository already contains the Maven wrapper, so a separate Maven installation is optional.
Important: the terminal must be able to find the JDK. That means either:

- `JAVA_HOME` points to your JDK 25 folder, or
- `java` is available on your `PATH`

### Windows

Check first:

```powershell
java -version
```

If that fails, set the JDK for the current terminal session:

```powershell
$env:JAVA_HOME="C:\Path\To\JDK-25"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

Example for the local setup used during development:

```powershell
$env:JAVA_HOME="C:\Users\18266\.jdks\openjdk-25.0.2"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Then run:

```powershell
.\mvnw.cmd test
.\mvnw.cmd javafx:run
```

### macOS / Linux

Check first:

```bash
java -version
```

If that fails, set the JDK for the current shell session:

```bash
export JAVA_HOME=/path/to/jdk-25
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Then run:

```bash
chmod +x mvnw
./mvnw test
./mvnw javafx:run
```

### IntelliJ IDEA

1. Open the repository as a Maven project.
2. Set the Project SDK to `JDK 25`.
3. Refresh Maven if IntelliJ imported with a different JDK.
4. Run either the main class `com.spaceflight.support.SpaceFlightSupportApplication` or the Maven goal `javafx:run`.

If Maven reports `release version 25 not supported`, IntelliJ or the terminal is using the wrong JDK.
If Maven reports that `JAVA_HOME` is not defined correctly, your terminal cannot currently find the JDK.

## Scope Reminder

Your team may maintain a broader Signavio overview of the entire space-flight phase, but this repository implements only the detailed in-flight support sub-process after the professor feedback:

- normal support during flight
- emergency escalation
- launch / landing fallback when nobody can move in the cabin

It does **not** implement check-in or the full end-to-end flight business process.

## Documentation Index

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/SCOPE.md](docs/SCOPE.md)
- [docs/USER_MANUAL.md](docs/USER_MANUAL.md)
- [docs/PERSONAS.md](docs/PERSONAS.md)
- [docs/PROCESS_DESCRIPTIONS.md](docs/PROCESS_DESCRIPTIONS.md)
- [docs/TESTING.md](docs/TESTING.md)
