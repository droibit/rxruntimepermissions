package com.github.droibit.rxruntimepermissions;

import android.support.annotation.NonNull;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionsResult {

    public static class Permission {

        public final String name;

        public final int grantResult;

        public Permission(@NonNull String name, int grantResult) {
            this.name = name;
            this.grantResult = grantResult;
        }

        public boolean isGranted() {
            return grantResult == PERMISSION_GRANTED;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Permission)) {
                return false;
            }

            Permission that = (Permission) o;

            return grantResult == that.grantResult && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + grantResult;
            return result;
        }

        @Override
        public String toString() {
            return "Permission{" +
                    "name='" + name + '\'' +
                    ", grantResult=" + grantResult +
                    '}';
        }
    }

    public final int requestCode;

    public final List<Permission> permissions;

    PermissionsResult(int requestCode, @NonNull List<Permission> permissions) {
        this.requestCode = requestCode;
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PermissionsResult)) {
            return false;
        }

        PermissionsResult that = (PermissionsResult) o;

        return requestCode == that.requestCode && permissions.equals(that.permissions);

    }

    @Override
    public int hashCode() {
        int result = requestCode;
        result = 31 * result + permissions.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PermissionsResult{" +
                "requestCode=" + requestCode +
                ", permissions=" + permissions +
                '}';
    }
}
