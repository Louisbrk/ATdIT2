# Architecture

## Architectural Goal

The architecture is intentionally small, layered, and easy to defend in a university grading context. It separates process logic from presentation logic and keeps every class focused on one responsibility.

## Package Responsibilities

### `com.spaceflight.support.app`

- application bootstrap
- logger configuration
- demo users and seeded mock data

### `com.spaceflight.support.domain.enums`

- stable business concepts such as status, severity, passenger tier, support role, and support action

### `com.spaceflight.support.domain.model`

- core domain entities:
  - `Passenger`
  - `Incident`
  - `IncidentActionLog`

### `com.spaceflight.support.domain.exception`

- simple runtime exceptions for not found cases and invalid workflow operations

### `com.spaceflight.support.repository`

- repository abstractions for passengers and incidents

### `com.spaceflight.support.repository.inmemory`

- in-memory repository implementations used by the proof of concept

### `com.spaceflight.support.service`

- service interfaces used by the UI
- explicit use-case boundary for incident handling

### `com.spaceflight.support.service.dto`

- simple request object for incident creation

### `com.spaceflight.support.service.impl`

- service implementations
- workflow rule definition in `IncidentWorkflowRules`

### `com.spaceflight.support.ui`

- JavaFX dashboard UI
- user interactions
- drag-and-drop board rendering
- detail and history visualization

## Main Program Flow

1. `SpaceFlightSupportApplication` starts the JavaFX application.
2. `ApplicationBootstrap` creates repositories and services.
3. `DemoDataInitializer` seeds passengers and initial incidents.
4. `MainDashboardView` renders the dashboard.
5. User actions in the UI call service methods.
6. Services validate workflow rules, apply business logic, and update incidents.
7. The UI refreshes and visualizes the latest state.

## Main Design Decisions

### Service Layer as Process Boundary

The most important design decision is that the JavaFX layer does not decide:

- whether a move is allowed
- whether escalation is required
- whether an action is valid

All of that happens in `IncidentServiceImpl`.

### Repository Abstractions

Repositories are interfaces because the prototype should be explainable as a future-ready design:

- today: in-memory data
- later: JDBC or another persistence mechanism

The UI and service interfaces do not need to change if persistence changes.

### Enums for Workflow Stability

Statuses and severities are modeled as enums because they are limited, central workflow concepts:

- `IncidentStatus`
- `IncidentSeverity`
- `ResponsibleRole`
- `SupportActionType`

This keeps the code readable and reduces invalid states.

### Drag-and-Drop Without Business Logic in UI

The UI only visualizes the board and forwards user actions.
The board checks allowed transitions through the service layer.
This keeps the drag-and-drop feature meaningful and not just decorative.

## SOLID Usage

### Single Responsibility Principle

- `Passenger` only models passenger data.
- `Incident` only models incident state and history.
- `IncidentServiceImpl` contains the incident use-case logic.
- `MainDashboardView` only handles UI behavior.
- `DemoDataInitializer` only creates demo data.

### Open/Closed Principle

The architecture is open for extension through:

- new repository implementations
- new UI views
- additional service methods

without rewriting the existing structure.

### Liskov Substitution Principle

Repository and service interfaces can be replaced by compatible implementations without changing callers.

### Interface Segregation Principle

The prototype keeps interfaces small:

- `PassengerService`
- `IncidentService`
- `SupportAvailabilityService`

### Dependency Inversion Principle

The service layer depends on repository abstractions, not concrete storage classes.

## Why This Scope Fits a Proof of Concept

This architecture is large enough to demonstrate:

- process thinking
- business rules
- exception handling
- maintainability
- UI support for a defined workflow

It is also small enough to explain in a short presentation and realistic for a student team.
