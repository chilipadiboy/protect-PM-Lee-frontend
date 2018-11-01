package org.cs4239.team1.protectPMLeefrontendserver.model;

public enum Gender {
    ALL,
    MALE,
    FEMALE;

    public static Gender create(String gender) {
        return Gender.valueOf(gender.toUpperCase());
    }

    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
