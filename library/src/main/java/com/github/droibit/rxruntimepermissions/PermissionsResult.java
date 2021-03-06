package com.github.droibit.rxruntimepermissions;

import android.support.annotation.NonNull;

import java.util.List;

public class PermissionsResult {

    public enum GrantResult {
        GRANTED,
        SHOULD_SHOW_RATIONALE,
        NEVER_ASK_AGAIN
    }

    public static class Permission {

        @NonNull
        public final String name;

        @NonNull
        public final GrantResult grantResult;

        public Permission(@NonNull String name, @NonNull GrantResult grantResult) {
            this.name = name;
            this.grantResult = grantResult;
        }

        public boolean isGranted() {
            return grantResult == GrantResult.GRANTED;
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
            result = 31 * result + grantResult.hashCode();
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

    @NonNull
    public final List<Permission> permissions;

    public PermissionsResult(int requestCode, @NonNull List<Permission> permissions) {
        this.requestCode = requestCode;
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "PermissionsResult{" +
                "requestCode=" + requestCode +
                ", permissions=" + permissions +
                '}';
    }
}
