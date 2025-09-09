package com.record.myprivateproject.domain;

public enum PermissionType {
    READ("READ_AUTHORITY"),
    WRITE("WRITE_AUTHORITY"),
    OWNER("OWNER_AUTHORITY");
    private final String permissionType;
    PermissionType(String permission){
        this.permissionType = permission;
    }
}
