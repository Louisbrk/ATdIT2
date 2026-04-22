package org.example.spaceflight.model;

/** Snapshot of a passenger's five monitored vital signs at a single point in time. */
public class VitalSigns {

    private int bpm;
    private double spO2;
    private int systolicBp;
    private int diastolicBp;
    private int respiratoryRate;

    public VitalSigns(int bpm, double spO2, int systolicBp, int diastolicBp, int respiratoryRate) {
        this.bpm = bpm;
        this.spO2 = spO2;
        this.systolicBp = systolicBp;
        this.diastolicBp = diastolicBp;
        this.respiratoryRate = respiratoryRate;
    }

    public int getBpm() { return bpm; }
    public void setBpm(int bpm) { this.bpm = bpm; }

    public double getSpO2() { return spO2; }
    public void setSpO2(double spO2) { this.spO2 = spO2; }

    public int getSystolicBp() { return systolicBp; }
    public void setSystolicBp(int systolicBp) { this.systolicBp = systolicBp; }

    public int getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(int diastolicBp) { this.diastolicBp = diastolicBp; }

    public int getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(int respiratoryRate) { this.respiratoryRate = respiratoryRate; }

    @Override
    public String toString() {
        return "VitalSigns{bpm=" + bpm + ", spO2=" + spO2 +
                ", bp=" + systolicBp + "/" + diastolicBp +
                ", respRate=" + respiratoryRate + "}";
    }
}
