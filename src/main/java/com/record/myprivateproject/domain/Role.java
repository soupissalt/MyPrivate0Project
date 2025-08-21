package com.record.myprivateproject.domain;

public enum Role {
    ADMIN, MAINTAINER, READER;

    public String toDb() { return name().toLowerCase(); }
    public static Role fromDb(String s) {
        return switch (s) {
            case "admin" -> ADMIN;
            case "maintainer" -> MAINTAINER;
            case "reader" -> READER;
            default -> throw new IllegalArgumentException("Unknown role: " + s);
        };
    }
}