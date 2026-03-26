# Process Descriptions

## 1. Normal In-Flight Support Process

### Start Event

Passenger feels discomfort, stress, or mild sickness during the space flight.

### Participants / Lanes

- Passenger
- Onboard Support
- Base Station Operator
- Support Application

### Tasks in Order

1. Passenger reports discomfort.
2. Onboard Support creates or opens an incident in the application.
3. Application stores the incident with severity and status `New`.
4. Onboard Support reviews the case and moves it to `Assessing`.
5. Onboard Support chooses a support action.
6. Application logs the action and updates the incident history.
7. Onboard Support monitors the passenger and moves the case to `Monitoring`.
8. Passenger condition improves.
9. Onboard Support resolves the incident.
10. Application logs the resolution and closes the case.

### Gateway / Decision

Is the passenger condition stable after support?

- Yes: continue monitoring and resolve.
- No: escalate to base station.

### Exception Path

If the situation worsens during assessment or monitoring, the case is escalated and follows the emergency process.

### End Event

Incident is resolved and documented.

### Data Objects / Information Exchanged

- passenger identity
- incident title and description
- severity
- workflow status
- selected support action
- action history
- current responsible role

### Software Scope

Implemented in software:

- incident creation
- status management
- support action recording
- action logging
- resolution logging

Outside system scope:

- real medical treatment
- physical passenger care

## 2. Emergency Escalation / Fallback Process

### Start Event

Either:

- the passenger has a severe incident, or
- onboard support becomes unavailable or incapacitated

### Participants / Lanes

- Passenger
- Onboard Support
- Base Station Operator
- Support Application

### Tasks in Order

1. Passenger condition becomes critical or onboard support becomes unavailable.
2. Application evaluates escalation rules.
3. Application changes the incident status to `Escalated`.
4. Application assigns responsibility to base station.
5. Application logs the escalation reason.
6. Base Station Operator takes over the case.
7. Base Station Operator reviews the action history and current status.
8. Base Station Operator coordinates the response.
9. Base Station Operator continues monitoring or resolves the incident.
10. Application logs every status change and final resolution.

### Gateway / Decision

Is onboard support available?

- Yes: onboard support may continue normal handling unless severity is critical.
- No: base station must take over unresolved incidents.

### Exception Path

If multiple unresolved onboard incidents exist when support becomes unavailable, all of them are escalated and remain traceable in the board and history.

### End Event

Incident is either stabilized under base-station control or resolved with complete traceability.

### Data Objects / Information Exchanged

- incident severity
- escalation reason
- current workflow status
- assigned responsible role
- history of previous actions
- base-station takeover log

### Software Scope

Implemented in software:

- automatic critical escalation
- fallback escalation for support unavailability
- base-station takeover
- history logging
- workflow visibility

Outside system scope:

- emergency medical systems
- spacecraft control actions
- external communications infrastructure
