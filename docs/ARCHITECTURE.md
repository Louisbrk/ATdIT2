# Architecture

## Architectural Goal

The architecture is intentionally small, layered, and presentation-friendly.

It supports one core claim:

- business rules belong in services
- JavaFX belongs in the presentation layer
- the prototype stays understandable and maintainable

## Package Responsibilities

### `com.spaceflight.support.app`

- application bootstrap
- logging configuration
- seeded demo data

### `com.spaceflight.support.domain.enums`

- stable workflow concepts:
  - `IncidentStatus`
  - `IncidentSeverity`
  - `ResponsibleRole`
  - `SupportActionType`
  - `PassengerTier`
  - `FlightPhase`

### `com.spaceflight.support.domain.model`

- `Passenger`
- `Incident`
- `IncidentActionLog`

### `com.spaceflight.support.domain.exception`

- simple domain-level runtime exceptions for invalid workflow behavior

### `com.spaceflight.support.repository`

- repository abstractions for incidents and passengers

### `com.spaceflight.support.repository.inmemory`

- proof-of-concept storage used for demo and tests

### `com.spaceflight.support.service`

- use-case interfaces for incident handling and support availability

### `com.spaceflight.support.service.dto`

- request object for incident creation

### `com.spaceflight.support.service.impl`

- incident workflow logic
- escalation rules
- fallback handling
- workflow transition validation

### `com.spaceflight.support.ui`

- JavaFX presentation layer
- `Mission Control` tab for onboard support and base station
- `Passenger Console` tab for the in-seat display interaction
- drag-and-drop board rendering
- detail and history visualization

## Main Program Flow

1. `SpaceFlightSupportApplication` starts JavaFX.
2. `ApplicationBootstrap` wires repositories and services.
3. `DemoDataInitializer` seeds demo passengers and incidents.
4. `MainDashboardView` renders two tabs:
   - `Mission Control`
   - `Passenger Console`
5. User actions in either tab call the service layer.
6. Services validate the workflow and update incidents.
7. The UI refreshes and visualizes the newest state.

## Key Design Decisions

### Figma Used as Design Input, Not as Web Technology

The professor requires Java, so the Figma design was translated into JavaFX instead of HTML or CSS frameworks.

Result:

- visual inspiration is reused
- technical constraints are still respected

### Service Layer as Process Boundary

The UI never decides:

- whether escalation is required
- whether a drag-and-drop move is valid
- whether an action is allowed

These decisions stay in `IncidentServiceImpl`.

### Passenger Console + Mission Control

The professor asked for meaningful interaction, not just reporting.

That is why the architecture now supports two UI perspectives:

- passenger perspective through the seat display
- operational perspective through the support dashboard

### Flight Phase as a First-Class Workflow Context

The feedback about launch and landing created a real modeling need:

- sometimes nobody in the cabin can move
- the software still has to do something useful

`FlightPhase` makes this explicit in the UI and in the documented process.

## SOLID Usage

### Single Responsibility Principle

- `Passenger` models passenger data only
- `Incident` models workflow state and action history only
- `IncidentServiceImpl` contains use-case logic
- `MainDashboardView` handles JavaFX interactions only
- `DemoDataInitializer` seeds demo data only

### Open/Closed Principle

The design is open for extension through:

- new repository implementations
- additional views
- additional service operations

without changing the overall structure.

### Liskov Substitution Principle

Repository and service interfaces can be replaced by compatible implementations without changing callers.

### Interface Segregation Principle

Interfaces are small and focused:

- `PassengerService`
- `IncidentService`
- `SupportAvailabilityService`

### Dependency Inversion Principle

The service layer depends on repository abstractions instead of concrete storage classes.

## Why This Architecture Fits the Grading Context

- easy to explain in a short presentation
- clearly separates UI and process logic
- supports both the normal and exception case
- realistic enough to show engineering quality without overengineering
