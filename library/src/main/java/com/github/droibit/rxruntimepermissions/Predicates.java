package com.github.droibit.rxruntimepermissions;

import rx.Observable;
import rx.functions.Func1;

final class Predicates {

    static class AreGranted implements Func1<PermissionsResult, Boolean> {

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
}
