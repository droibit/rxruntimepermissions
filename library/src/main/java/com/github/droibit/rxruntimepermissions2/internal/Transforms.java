package com.github.droibit.rxruntimepermissions2.internal;


import com.github.droibit.rxruntimepermissions2.PermissionsResult;
import com.github.droibit.rxruntimepermissions2.PermissionsResult.GrantResult;
import com.github.droibit.rxruntimepermissions2.PermissionsResult.Permission;

import android.support.annotation.RestrictTo;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class Transforms {

    private Transforms() {
    }

    public static final Function<PermissionsResult, Boolean> ARE_GRANTED = new AreGranted();

    private static class AreGranted implements Function<PermissionsResult, Boolean> {

        @Override
        public Boolean apply(PermissionsResult permissionsResult) throws Exception {
            return Observable.fromIterable(permissionsResult.permissions)
                    .all(new Predicate<Permission>() {
                        @Override
                        public boolean test(Permission permission) throws Exception {
                            return permission.isGranted();
                        }
                    }).blockingGet();
        }
    }

    public static final Function<PermissionsResult, GrantResult> TO_FIRST_GRANT_RESULT = new ToFirstGrantResult();

    private static class ToFirstGrantResult implements Function<PermissionsResult, GrantResult> {

        @Override
        public GrantResult apply(PermissionsResult permissionsResult) throws Exception {
            return Observable.fromIterable(permissionsResult.permissions)
                    .blockingFirst().grantResult;
        }
    }

}
