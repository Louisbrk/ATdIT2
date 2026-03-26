# Scope

## In Scope

This team is responsible only for the **Space Flight** phase of the fictional company process.

The proof of concept covers one focused core process:

**In-Flight Passenger Support and Emergency Escalation**

Implemented process capabilities:

- view passengers during flight
- create a passenger incident
- classify an incident by severity
- manage incident workflow on a drag-and-drop board
- handle normal onboard support cases
- escalate severe or fallback cases to base station
- log all important actions for traceability
- show which role is currently responsible

## Out of Scope

The prototype does **not** implement a full company platform.

Not included:

- booking and sales
- billing and payment management
- pre-flight medical clearance
- spacecraft logistics and maintenance planning
- review management
- enterprise user management
- real telemetry integration
- real network communication
- real persistence beyond in-memory data

## Why the Scope Is Appropriate

The chosen scope is narrow on purpose:

- it supports a concrete business problem
- it matches the professor’s request for a realistic prototype
- it includes both the normal case and the exception/fallback case
- it remains feasible for a university student project

## Implemented vs Mocked Boundary

### Implemented

- workflow support inside the Java application
- incident status management
- escalation logic
- action log visibility
- demo-ready passengers and incidents

### Mocked

- sensor input
- spacecraft communication
- medical diagnosis
- persistence and external integrations

This boundary is intentional and should be explained explicitly during the presentation.
