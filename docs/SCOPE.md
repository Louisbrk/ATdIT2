# Scope

## Core Scope After Professor Feedback

This repository implements a focused sub-process inside the **Space Flight** phase:

**In-Flight Passenger Support and Emergency Escalation**

The key design choice is that the team may still show a broader Signavio overview for context, but the runnable software must stay focused on one defendable core process.

## In Scope

- passenger interaction through an in-seat support display
- normal in-flight support handling
- emergency escalation to base station
- fallback handling when onboard support is unavailable
- launch / landing exception handling when nobody can move in the cabin
- incident workflow with clear statuses
- traceable action history
- visibility of responsibility changes between roles

## Explicitly Out of Scope

- check-in
- boarding orchestration as a software focus
- pre-flight medical clearance
- booking, sales, billing, and payment
- maintenance and logistics
- complete spacecraft control
- real telemetry integration
- real doctor / psychologist communication systems
- full enterprise platform behavior

## Why This Scope Fits the Project

- it directly addresses the professor's feedback about exceptions
- it supports two clear cases: normal handling and fallback / emergency handling
- it provides meaningful user interaction instead of passive reporting only
- it is realistic enough for a student proof of concept and small enough to finish

## Implemented vs Mocked Boundary

### Implemented

- JavaFX mission-control dashboard
- JavaFX passenger console
- incident creation and classification
- workflow board with drag and drop
- escalation logic
- base-station takeover
- launch / landing fallback behavior in the UI and process wording
- action logging

### Mocked / Documented

- remote doctor support
- psychologist / chatbot escalation workflow
- real medical devices
- real spacecraft systems
- external databases and communication infrastructure

## Presentation Guidance

When presenting:

- use the broad BPMN only as business context
- show the implemented software as the detailed in-flight support sub-process
- state clearly that check-in is intentionally not part of this prototype
