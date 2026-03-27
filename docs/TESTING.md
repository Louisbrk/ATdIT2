# Testing

## Testing Strategy

The automated tests focus on the service layer because the main grading risk is incorrect process logic.

The UI adds value for the demo, but the workflow rules, escalation behavior, and traceability are the real core of the proof of concept.

## Automated Coverage

The JUnit tests cover:

- creating incidents
- automatic escalation of `CRITICAL` incidents
- fallback escalation when onboard support becomes unavailable
- allowed and disallowed workflow transitions
- action-log creation
- incident resolution

These tests are aligned with `IncidentServiceImpl`.

## Manual Demo Checks

Because the professor also evaluates interaction quality, these manual checks should be part of the demo preparation:

### Passenger Console

- create a normal support request from the seat display
- create an emergency request from the seat display
- switch between different passengers

### Mission Control

- drag an incident through the normal workflow
- verify that invalid moves are blocked
- open the history panel and confirm traceability

### Exception Handling

- set the flight phase to launch or landing
- show that crew movement is blocked conceptually
- create a request anyway through the passenger console
- show fallback visibility and escalation behavior

### Crew Unavailability

- disable the onboard support readiness checkbox
- confirm that unresolved incidents are escalated automatically

## How to Run Tests

### Windows

```powershell
.\mvnw.cmd test
```

### macOS / Linux

```bash
./mvnw test
```

## Residual Risks

- JavaFX behavior is not covered by automated UI tests
- drag-and-drop still requires manual verification
- doctor / psychologist support is currently documented or mocked, not fully implemented

These risks are acceptable because this is a focused university proof of concept.
