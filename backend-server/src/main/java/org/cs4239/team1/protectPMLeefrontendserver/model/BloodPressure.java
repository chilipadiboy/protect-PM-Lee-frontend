package org.cs4239.team1.protectPMLeefrontendserver.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BloodPressure {
    private final int systolic;
    private final int diastolic;

    public BloodPressureCategory getCategory() {
        if (systolic < 120 && diastolic < 80) {
            return BloodPressureCategory.NORMAL;
        } else if (systolic >= 120 && systolic <= 129 && diastolic < 80) {
            return BloodPressureCategory.ELEVATED;
        } else if ((systolic >= 130 && systolic <= 139) || (diastolic >= 80 && diastolic <= 89)) {
            return BloodPressureCategory.STAGE_1_HIGH;
        } else if (systolic >= 140 || diastolic >= 90) {
            return BloodPressureCategory.STAGE_2_HIGH;
        } else {
            throw new AssertionError("Should not happen.");
        }
    }

    @Override
    public String toString() {
        return systolic + "/" + diastolic;
    }

    public static BloodPressure create(String bloodPressure) {
        String[] values = bloodPressure.split("/");
        return new BloodPressure(Integer.valueOf(values[0]), Integer.valueOf(values[1]));
    }
}
