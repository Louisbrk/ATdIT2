# Personas

This project now separates personas into two groups:

- personas that directly interact with the implemented JavaFX prototype
- personas that are relevant for BPMN and the next iteration, but are only mocked or documented today

This split is important because the professor explicitly asked for clear scope boundaries.

## Implemented Personas

### Jennifer Monroe

Role: Premium passenger

Characteristics:

- expects a premium, calm, and well-controlled experience
- reacts negatively to unclear communication or delays
- may escalate quickly from stress to panic when the situation feels uncertain

Relevant system needs:

- fast and reassuring seat-display interaction
- clear escalation visibility
- confidence that severe cases become high priority immediately

## Ben Cooper

Role: Standard passenger

Characteristics:

- first-time flyer with strong emotional investment
- needs reassurance and simple guidance
- likely to use the in-seat interface for support requests

Relevant system needs:

- understandable wording
- structured support flow
- visible follow-up after a request is sent

## Noah Fischer

Role: Young passenger

Characteristics:

- less experienced and more emotionally reactive
- needs especially simple, calming communication
- is a good example for why the seat display should not only show status, but allow interaction

Relevant system needs:

- large and obvious help actions
- simple explanations during launch and landing
- quick reassurance when crew movement is not possible

## Emma Bright

Role: Onboard support

Characteristics:

- handles normal incidents during the flight
- needs a low-friction operational dashboard
- works under time pressure and must triage quickly

Relevant system needs:

- passenger overview
- drag-and-drop workflow board
- direct access to incident detail and action history

## Brendon Fitz

Role: Base station operator

Characteristics:

- handles escalated incidents and fallback situations
- needs full traceability across all unresolved cases
- becomes critical when onboard support is unavailable or physically blocked

Relevant system needs:

- clear escalation workflow
- remote takeover path
- visibility into all actions already taken

## Documented / Mocked Next-Iteration Personas

### Remote Doctor

Role: Medical specialist in the base station

Why included:

- fits the group discussion and the professor feedback about exception handling
- makes medical escalation more realistic
- should appear in BPMN and presentation as a remote support role

Current prototype state:

- documented and visually referenced
- not implemented as a separate software workflow actor yet

### Psychologist / Calming Support

Role: Human specialist or chatbot-supported calming service

Why included:

- aligns with the professor's idea that a psychologist is expensive and a chatbot or guided calming flow may come first
- supports fear, panic, and stress cases beyond purely medical incidents

Current prototype state:

- partially represented through passenger-console interactions and calming support actions
- not implemented as a full specialist workflow with separate permissions

## Persona-to-Prototype Mapping

- Passenger personas interact with the `Passenger Console`
- Emma Bright interacts with `Mission Control` for the normal case
- Brendon Fitz interacts with `Mission Control` for escalation and fallback takeover
- Doctor and psychologist remain explicit BPMN / documentation roles for the next iteration

## Why This Matters for the Presentation

This mapping lets the team explain three layers clearly:

1. broad business context through the full persona set
2. implemented software scope through passenger, onboard support, and base station
3. next-iteration value through doctor / psychologist extensions
