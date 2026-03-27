# User Manual

## Purpose

The application helps passengers, onboard support, and base station staff manage incidents during a space flight.

## Main Areas of the Interface

### Mission Control Tab

- contains the operational dashboard for onboard support and base station
- includes the workflow board, passenger intake, incident detail, and action history
- provides controls for flight phase and crew readiness

### Passenger Console Tab

- simulates the in-seat passenger display
- shows flight information and current support mode
- allows passengers to request medical help, psychological help, onboard support, or emergency escalation
- creates real incidents in the workflow board
- remains useful during launch and landing when the crew cannot move

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

1. Open the `Passenger Console` tab or select a passenger in `Mission Control`.
2. Create a help request through the seat display or the manual incident form.
3. In `Mission Control`, drag the incident from `New` to `Assessing`.
4. Select a support action and apply it.
5. Drag the incident to `Monitoring`.
6. Resolve the incident when the passenger is stable.

### Emergency Case

1. Create a `CRITICAL` incident from the seat display or the form, or disable onboard support.
2. Observe automatic escalation to the `Escalated` column.
3. Select the escalated incident.
4. Click `Base Station Take Over`.
5. Continue handling or resolve the case from base station control.

### Launch / Landing Exception Demo

1. In `Mission Control`, change the flight phase to launch or landing.
2. Confirm that the mission status indicates a strapped-in phase.
3. Open the `Passenger Console` and create a support request.
4. Explain that physical onboard response is blocked, but the request is still logged, visible, and escalatable.

## Demo Buttons

- `Create Normal Demo Case`: creates a pre-filled normal support incident
- `Create Emergency Demo Case`: creates a pre-filled critical incident
- `Flight phase`: models launch, orbit, and landing constraints
- `Onboard support team ready`: triggers fallback escalation when the crew becomes unavailable

## Important Workflow Rules

- `CRITICAL` incidents are escalated automatically
- unresolved incidents are escalated when onboard support becomes unavailable
- during launch and landing, passengers can still use the seat display even when crew movement is blocked
- not every drag-and-drop move is valid
- every important action is written to the incident history
