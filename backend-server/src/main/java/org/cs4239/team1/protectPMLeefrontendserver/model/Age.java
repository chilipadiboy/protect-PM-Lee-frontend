package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Age {
    ALL, BELOW_13, FROM_13_TO_18, FROM_19_TO_25, FROM_26_TO_35, FROM_36_TO_55, ABOVE_55;

    public static Age create(String age) {
        return Age.valueOf(age.toUpperCase().replaceAll(" ", "_"));
    }

    @Override
    public String toString() {
        List<String> stringList = Arrays.stream(name().split("_"))
                .map(str -> str.charAt(0) + str.substring(1).toLowerCase())
                .collect(Collectors.toList());
        return String.join(" ", stringList);
    }

    public boolean isInRange(int age) {
        switch (this) {
            case BELOW_13:
                return age < 13;
            case FROM_13_TO_18:
                return age >= 13 && age <= 18;
            case FROM_19_TO_25:
                return age >= 19 && age <= 25;
            case FROM_26_TO_35:
                return age >= 26 && age <= 35;
            case FROM_36_TO_55:
                return age >= 36 && age <= 55;
            case ABOVE_55:
                return age > 55;
            default:
                throw new AssertionError("Should not reach here");
        }
    }
}
