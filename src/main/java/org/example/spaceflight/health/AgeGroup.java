package org.example.spaceflight.health;

/** Age bracket used to select demographic vital-sign baselines. */
enum AgeGroup {
    YOUNG, MIDDLE, SENIOR;

    static AgeGroup fromAge(int age) {
        if (age < 30)  return YOUNG;
        if (age <= 50) return MIDDLE;
        return SENIOR;
    }
}
