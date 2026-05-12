# SpaceFlight
SpaceFlight is a program developed in our university lecture called ATdIT.
It simulates a shuttle flight and supports ground crew and passengers during the flight with a focus on AI Health monitoring and prioritisation.

To run:
```bash
./mvnw javafx:run
```

## AI Health Classification

### Training Data (`src/main/resources/training_data.csv`)

The classifier is trained on **144 labelled cases** covering all demographic segments of the passenger population.

**CSV format:**

```
bpm, spo2, systolic, diastolic, respRate, ageGroup, gender, mode, label
```

| Column | Type | Values |
|--------|------|--------|
| `bpm` | int | Heart rate in beats per minute |
| `spo2` | double | Blood oxygen saturation in % |
| `systolic` | int | Systolic blood pressure in mmHg |
| `diastolic` | int | Diastolic blood pressure in mmHg |
| `respRate` | int | Respiratory rate in breaths per minute |
| `ageGroup` | enum | `YOUNG` (< 30), `MIDDLE` (30–50), `SENIOR` (> 50) |
| `gender` | enum | `MALE`, `FEMALE` |
| `mode` | enum | `RELAXED`, `NORMAL`, `ACTION` |
| `label` | enum | `GREEN` (healthy), `YELLOW` (warning), `RED` (critical) |

The dataset covers all **18 demographic segments** (3 age groups × 2 genders × 3 experience modes), each with representative GREEN, YELLOW and RED cases. Comments starting with `#` are ignored by the parser.

Baseline values are derived from published clinical reference ranges (AHA / ESC normal ranges segmented by age and sex). The `mode` column reflects expected physiological arousal:
- **ACTION** mode: higher BPM and blood pressure values are still considered normal (passengers are in an excited state)
- **RELAXED** mode: stricter thresholds — elevated stress indicators are flagged earlier
- **NORMAL** mode: standard clinical reference values

---

### Classification Algorithm (k-Nearest Neighbours)

The health status of each passenger is determined at every simulation tick by a **k-Nearest Neighbours (kNN)** classifier implemented in `KnnHealthEvaluationService`.

#### Step 1 — Feature normalisation

All five vital features are normalised to **[0, 1]** using the min/max values derived from the training set:

```
normalised = (value - min) / (max - min)
```

This ensures that features with larger absolute ranges (e.g. blood pressure 80–200) do not dominate features with smaller ranges (e.g. SpO2 80–100).

#### Step 2 — Weighted Euclidean distance

For each training case the distance to the current observation is computed as a **weighted Euclidean distance**:

```
d = sqrt( W_SpO2  * (Δspo2)²
        + W_Sys   * (Δsystolic)²
        + W_BPM   * (Δbpm)²
        + W_Dias  * (Δdiastolic)²
        + W_RR    * (ΔrespRate)² )
```

Feature weights reflect medical importance in an aerospace context:

| Feature | Weight |
|---------|--------|
| SpO2 | 0.30 |
| Systolic BP | 0.25 |
| BPM | 0.20 |
| Diastolic BP | 0.15 |
| Respiratory Rate | 0.10 |

#### Step 3 — Demographic context bonus

If a training case shares the same **age group**, **gender**, or **experience mode** as the passenger, a small distance bonus (`-0.08` per match) is applied. This ensures demographically similar cases are preferred as neighbours, so a senior male in ACTION mode is compared primarily against other senior male ACTION cases.

#### Step 4 — Majority vote (k = 5)

The **5 nearest neighbours** are selected and their labels are counted. The label with the most votes determines the overall health status:

```
GREEN=2, YELLOW=1, RED=2  →  RED wins (tie-break favours the more critical status)
```

#### Step 5 — Per-vital status via Z-Score

Independently of the kNN vote, each individual vital sign is classified by computing a **z-score** against a demographic population baseline (stored in `VitalProfileTable`):

```
z = |value - population_mean| / population_stdDev
```

- `z < 1.0` → GREEN
- `1.0 ≤ z < 2.0` → YELLOW
- `z ≥ 2.0` → RED

This drives the colour of each individual vital row in the dashboard (BPM, SpO2, BP, RR shown in green / amber / red independently).

#### Step 6 — Hard floor rule

If any single vital is classified as RED by the z-score, the overall status is elevated to RED regardless of the kNN vote. A single critical vital sign always constitutes a medical emergency.

---

## Architecture

### Current design (single-process)

All dashboards run in the same JVM. `SpaceFlightApp` is the entry point and owns
the lifecycle. All services are created once and shared as interface references.

```
SpaceFlightApp
  └── AppContext                 (one class that knows all concrete implementations)
        ├── SimulationService
        ├── FlightSimulationService
        ├── VitalSignsGenerator
        ├── AlertService
        ├── PsychologicalSupportService
        └── IPassengerRegistry
```

Every simulation tick `SpaceFlightApp` does three things:

1. Updates the flight state and generates new vital signs (server-side work).
2. Builds a `SimulationSnapshot` — an immutable, serialisation-ready data object.
3. Pushes the snapshot to all views via `Platform.runLater`.

```
Simulation tick
  │
  ├─ flightSimulationService.update()
  ├─ vitalSignsGenerator.generateNext()  ← mutates Passenger objects
  │
  ├─ new SimulationSnapshot(state, passengers, ...)   ← serialisable boundary
  │
  ├─ Base-station views  (use live objects — same process as the simulation)
  │     BaseStationView.updateFlightInfo(ShuttleState)
  │     AiHealthDashboardView.update(List<Passenger>, FlightPhase)
  │
  └─ Client-facing views  (receive only the snapshot — no direct object refs)
        PassengerDashboardView.update(SimulationSnapshot)
        StewardessInboxView.update(SimulationSnapshot)
```

Alert and psychological-support events travel via listener callbacks
(`AlertService.setOnAlertRaised`, `PsychologicalSupportService.setOnRequestRaised`).

---
