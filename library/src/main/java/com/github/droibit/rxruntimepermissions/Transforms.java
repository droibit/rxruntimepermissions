package com.github.droibit.rxruntimepermissions;

import rx.Observable;
import rx.functions.Func1;

public final class Transforms {

    private Transforms() {
    }

    public static Func1<PermissionsResult, Boolean> areGranted() {
        return ARE_GRANTED;
    }

    private static final Func1<PermissionsResult, Boolean> ARE_GRANTED = new AreGranted();

    private static class AreGranted implements Func1<PermissionsResult, Boolean> {

        @Override
        public Boolean call(PermissionsResult permissionsResult) {
            return Observable.from(permissionsResult.permissions).all(new Func1<PermissionsResult.Permission, Boolean>() {
                @Override
                public Boolean call(PermissionsResult.Permission permission) {
                    return permission.isGranted();
                }
            }).toBlocking().single();
        }
    }

    public static Func1<PermissionsResult, GrantResult> toSingleGrantResult() {
        return TO_SINGLE_GRANT_RESULT;
    }

    private static final Func1<PermissionsResult, GrantResult> TO_SINGLE_GRANT_RESULT = new ToSingleGrantResult();

    private static class ToSingleGrantResult implements Func1<PermissionsResult, GrantResult> {

        @Override
        public GrantResult call(PermissionsResult permissionsResult) {
            return Observable.from(permissionsResult.permissions).toBlocking().single().grantResult;
        }
    }
}
