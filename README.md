# Space Flight Support POC

## Project Purpose

This repository contains a focused JavaFX proof of concept for the **Space Flight** phase of a fictional space tourism company. The prototype supports one core process only:

**In-Flight Passenger Support and Emergency Escalation**

The goal is to show how onboard support staff and base-station staff can manage passenger incidents in a structured, traceable, and presentation-ready way.

## Problem Statement

During the actual space flight, passengers may experience:

- fear, stress, or panic
- nausea or motion sickness
- abnormal health symptoms
- situations where onboard support becomes unavailable

The existing process is assumed to be too ad hoc. This prototype introduces a workflow-driven process with status transitions, escalation rules, and transparent action logging.

## Personas

- Passenger: reports discomfort and receives support
- Onboard Support: handles normal in-flight cases
- Base Station Operator: takes over escalated or fallback cases

Detailed personas are documented in [docs/PERSONAS.md](docs/PERSONAS.md).

## Implemented Features

- JavaFX desktop application
- passenger overview panel
- incident creation form
- drag-and-drop incident workflow board
- workflow states: `New`, `Assessing`, `Monitoring`, `Escalated`, `Resolved`
- onboard support handling for normal incidents
- automatic escalation of `CRITICAL` incidents
- fallback escalation when onboard support is unavailable
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
- Maven 3.9+

Commands:

```bash
./mvnw test
./mvnw javafx:run
```

In IntelliJ, import the Maven project and run `com.spaceflight.support.SpaceFlightSupportApplication`.

## Documentation Index

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/SCOPE.md](docs/SCOPE.md)
- [docs/USER_MANUAL.md](docs/USER_MANUAL.md)
- [docs/PERSONAS.md](docs/PERSONAS.md)
- [docs/PROCESS_DESCRIPTIONS.md](docs/PROCESS_DESCRIPTIONS.md)
- [docs/TESTING.md](docs/TESTING.md)
