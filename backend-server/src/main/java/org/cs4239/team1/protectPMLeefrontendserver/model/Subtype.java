package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Subtype {
    ALL, ALLERGY, COLD, HEADACHES_AND_MIGRAINES, ASTHMA, HIGH_BLOOD_CHOLESTEROL, CANCER, DIABETES, HEART_DISEASE, HYPERTENSION,
    STROKE, PANIC_ATTACK, DEPRESSION, EATING_DISORDERS, OBSESSIVE_COMPULSIVE_DISORDER, SCHIZOPHRENIA, BRONCHITIS,
    BACK_PAIN, CATARACTS, CARIES, CHICKENPOX, GINGIVITIS, GOUT, HAEMORRHOIDS, URINARY, BLOOD_PRESSURE;

    @Override
    public String toString() {
        switch (this) {
            case HEADACHES_AND_MIGRAINES:
                return "Headaches and Migraines";
            default:
                List<String> stringList = Arrays.stream(name().split("_"))
                        .map(str -> str.charAt(0) + str.substring(1).toLowerCase())
                        .collect(Collectors.toList());
                return String.join(" ", stringList);
        }
    }

    public static Subtype create(String subtype) {
        return Subtype.valueOf(subtype.toUpperCase().replaceAll(" ", "_"));
    }
}
