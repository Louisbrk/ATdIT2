# User Manual

## Purpose

The application helps flight staff manage passenger incidents during a space flight.

## Main Areas of the Interface

### Passenger Overview

- shows all passengers
- allows the user to select a passenger
- displays a short profile summary
- contains the form for creating a new incident

### Incident Board

- shows all incidents by workflow state
- supports drag and drop between columns
- updates status when a move is valid

Columns:

- `New`
- `Assessing`
- `Monitoring`
- `Escalated`
- `Resolved`

### Incident Detail

- shows the selected incident
- shows current status, severity, responsible role, and description
- shows the full action history
- provides action buttons for support handling

## Typical Usage

### Normal Support Case

1. Select a passenger.
2. Enter an incident title, severity, and description.
3. Click `Report Incident`.
4. Drag the incident from `New` to `Assessing`.
5. Select a support action and apply it.
6. Drag the incident to `Monitoring`.
7. Resolve the incident when the passenger is stable.

### Emergency Case

1. Create a `CRITICAL` incident or disable onboard support.
2. Observe automatic escalation to the `Escalated` column.
3. Select the escalated incident.
4. Click `Base Station Take Over`.
5. Continue handling or resolve the case from base station control.

## Demo Buttons

- `Create Normal Demo Case`: creates a pre-filled normal support incident
- `Create Emergency Demo Case`: creates a pre-filled critical incident
- `Onboard support available`: toggles onboard support availability and triggers fallback escalation when disabled

## Important Workflow Rules

- `CRITICAL` incidents are escalated automatically
- unresolved incidents are escalated when onboard support becomes unavailable
- not every drag-and-drop move is valid
- every important action is written to the incident history
