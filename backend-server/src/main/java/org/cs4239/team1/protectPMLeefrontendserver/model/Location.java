package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum Location {
    ALL, SOUTH, SOUTH_WEST, CENTRAL, NORTH, NORTH_EAST, WEST, NORTH_WEST, EAST;

    private static final Map<Integer, Location> POSTAL_CODE_TO_LOCATION = new HashMap<>();

    static {
        IntStream.range(1, 14).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, SOUTH));
        IntStream.range(14, 17).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, SOUTH_WEST));
        IntStream.range(17, 31).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, CENTRAL));
        IntStream.range(31, 38).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH_EAST));
        IntStream.range(38, 53).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, EAST));
        IntStream.range(53, 56).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH_EAST));
        IntStream.range(56, 58).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH));
        IntStream.range(58, 60).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, SOUTH_WEST));
        IntStream.range(60, 65).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, WEST));
        IntStream.range(65, 69).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH_WEST));
        IntStream.range(69, 72).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, WEST));
        IntStream.range(72, 75).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH_WEST));
        IntStream.range(75, 77).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH_WEST));
        IntStream.range(77, 81).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH));
        IntStream.range(81, 82).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, EAST));
        IntStream.range(82, 83).forEach(i -> POSTAL_CODE_TO_LOCATION.put(i, NORTH_EAST));
    }

    public static Location create(String location) {
        return Location.valueOf(location.toUpperCase().replaceAll("-", "_"));
    }

    @Override
    public String toString() {
        List<String> stringList = Arrays.stream(name().split("_"))
                .map(str -> str.charAt(0) + str.substring(1).toLowerCase())
                .collect(Collectors.toList());
        return String.join("-", stringList);
    }

    public boolean isInLocation(int postalCode) {
        return this == POSTAL_CODE_TO_LOCATION.get(postalCode / 10000);
    }
}
