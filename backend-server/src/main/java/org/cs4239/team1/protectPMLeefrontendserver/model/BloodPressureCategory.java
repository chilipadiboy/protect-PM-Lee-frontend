package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BloodPressureCategory {
    NORMAL, ELEVATED, STAGE_1_HIGH, STAGE_2_HIGH;

    @Override
    public String toString() {
        List<String> stringList = Arrays.stream(name().split("_"))
                .map(str -> str.charAt(0) + str.substring(1).toLowerCase())
                .collect(Collectors.toList());
        return String.join(" ", stringList);
    }
}
