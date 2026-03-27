# Process Descriptions

These descriptions reflect the professor feedback from session 2.

Important consequence:

- the broad Signavio overview may still show a larger end-to-end flight process
- the detailed implementation focus of this repository is only the in-flight support sub-process
- if the older Signavio conflicts with the feedback, the feedback wins

## 1. Normal In-Flight Support Process

### Start Event

Passenger feels stress, discomfort, nausea, or confusion during the flight.

### Participants / Lanes

- Passenger
- Passenger Console
- Onboard Support
- Base Station Operator
- Support Application

### Tasks in Order

1. Passenger experiences a non-critical problem during the flight.
2. Passenger reports the problem through the in-seat display or onboard support notices the issue directly.
3. Support Application creates an incident with status `New`.
4. Support Application checks the current flight phase.
5. Gateway: `Is onboard movement possible?`
6. If yes, onboard support opens the case and moves it to `Assessing`.
7. Onboard support selects a support action such as reassurance, guided breathing, or motion-sickness handling.
8. Support Application logs the selected action.
9. Onboard support continues observation and moves the case to `Monitoring`.
10. Gateway: `Is the passenger stable?`
11. If yes, onboard support resolves the incident.
12. Support Application logs the resolution and closes the case.

### Exception Paths

If the passenger becomes worse during assessment or monitoring:

- severity is raised
- the incident is escalated
- the process switches to the emergency / fallback flow

If the current flight phase is launch or landing and nobody can move in the cabin:

- the passenger console still accepts the request
- the application keeps the incident traceable
- calming guidance can start immediately through the display
- onboard physical response is delayed until movement is allowed again, unless escalation is required earlier

### End Event

Passenger is stabilized and the incident is resolved with a full action history.

### Data Objects / Information Exchanged

- passenger identity
- flight phase
- incident title and description
- severity
- workflow status
- selected support action
- action history
- responsible role

### Software Scope

Implemented in software:

- passenger interaction through the seat-display view
- incident creation
- status handling
- support action selection
- action logging
- resolution logging

Outside system scope:

- physical passenger treatment
- real medical measurement
- cabin hardware

## 2. Emergency Escalation / Fallback Process

### Start Event

One of the following happens:

- the passenger has a severe or critical issue
- onboard support becomes unavailable or incapacitated
- the cabin is in a strapped-in phase and immediate physical response is impossible

### Participants / Lanes

- Passenger
- Passenger Console
- Onboard Support
- Base Station Operator
- External Support (Doctor / Psychologist, mocked)
- Support Application

### Tasks in Order

1. Passenger triggers an emergency request or a severe incident is detected.
2. Support Application evaluates escalation rules.
3. Gateway: `Is the incident critical or is onboard support unavailable?`
4. If yes, Support Application changes the status to `Escalated`.
5. Support Application assigns responsibility to base station.
6. Support Application logs the escalation reason.
7. Base Station Operator reviews the existing action history and current incident state.
8. Base Station Operator takes over the case.
9. If relevant, remote doctor or psychologist support is requested as an external specialist step.
10. Base Station Operator coordinates further handling and resolves the case when stable.
11. Support Application records each transition and the final resolution.

### Exception Paths

If multiple unresolved incidents exist when onboard support becomes unavailable:

- all unresolved onboard incidents are escalated automatically
- each incident remains visible and traceable on the workflow board

If the issue starts during launch or landing:

- the passenger console remains active
- self-guidance or calming instructions can start immediately
- remote escalation can begin before physical onboard help becomes possible

### End Event

Incident is stabilized under remote supervision or fully resolved with complete traceability.

### Data Objects / Information Exchanged

- incident severity
- escalation reason
- current flight phase
- current responsible role
- action history
- takeover log
- specialist-support note if a doctor or psychologist is consulted

### Software Scope

Implemented in software:

- automatic critical escalation
- fallback escalation when onboard support is unavailable
- launch / landing fallback handling through the passenger console plus workflow visibility
- base-station takeover
- action history and traceability

Mocked / documented only:

- remote doctor workflow
- psychologist / chatbot workflow
- emergency medical hardware
- spacecraft control actions
