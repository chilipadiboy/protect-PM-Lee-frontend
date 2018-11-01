package org.cs4239.team1.protectPMLeefrontendserver.model;

public enum Type {
    ILLNESS, READING;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static Type create(String type) {
        return Type.valueOf(type.toUpperCase());
    }
}
