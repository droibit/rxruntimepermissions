package com.github.droibit.rxruntimepermissions;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionResult {

    public final int requestCode;

    public final String permission;

    public final int grantResult;

    PermissionResult(int requestCode, String permission, int grantResult) {
        this.requestCode = requestCode;
        this.permission = permission;
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
        if (!(o instanceof PermissionResult)) {
            return false;
        }

        PermissionResult that = (PermissionResult) o;

        if (requestCode != that.requestCode) {
            return false;
        }
        if (grantResult != that.grantResult) {
            return false;
        }
        return permission.equals(that.permission);

    }

    @Override
    public int hashCode() {
        int result = requestCode;
        result = 31 * result + permission.hashCode();
        result = 31 * result + grantResult;
        return result;
    }

    @Override
    public String toString() {
        return "PermissionResult{" +
                "requestCode=" + requestCode +
                ", permission='" + permission + '\'' +
                ", grantResult=" + grantResult +
                '}';
    }
}
