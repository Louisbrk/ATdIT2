package org.example.spaceflight.model;

// Snapshot of a passenger's five monitored vital signs at a single point in time
public class VitalSigns {

    private final int bpm;
    private final double spO2;
    private final int systolicBp;
    private final int diastolicBp;
    private final int respiratoryRate;

    public VitalSigns(int bpm, double spO2, int systolicBp, int diastolicBp, int respiratoryRate) {
        this.bpm = bpm;
        this.spO2 = spO2;
        this.systolicBp = systolicBp;
        this.diastolicBp = diastolicBp;
        this.respiratoryRate = respiratoryRate;
    }

    public int getBpm() { return bpm; }

    public double getSpO2() { return spO2; }

    public int getSystolicBp() { return systolicBp; }

    public int getDiastolicBp() { return diastolicBp; }

    public int getRespiratoryRate() { return respiratoryRate; }

    @Override
    public String toString() {
        return "VitalSigns{bpm=" + bpm + ", spO2=" + spO2 +
                ", bp=" + systolicBp + "/" + diastolicBp +
                ", respRate=" + respiratoryRate + "}";
    }
}
