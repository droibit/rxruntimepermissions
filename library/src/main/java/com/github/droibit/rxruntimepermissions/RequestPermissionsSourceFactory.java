package com.github.droibit.rxruntimepermissions;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import java.util.List;

import rx.Observable;
import rx.functions.Action2;
import rx.functions.Func1;

class RequestPermissionsSourceFactory {

    private RequestPermissionsSourceFactory() {
    }

    static class FromActivity implements RequestPermissionsSource, Action2<Integer, String[]> {

        private final RxRuntimePermissions rxRuntimePermissions;

        private final Activity activity;

        @Nullable
        private Observable<?> trigger;

        FromActivity(RxRuntimePermissions rxRuntimePermissions, Activity activity) {
            this.rxRuntimePermissions = rxRuntimePermissions;
            this.activity = activity;
        }

        @Override
        public RequestPermissionsSource on(@NonNull Observable<?> trigger) {
            this.trigger = checkNotNull(trigger);
            return this;
        }

        @Override
        public Observable<Boolean> requestPermissions(int requestCode, @NonNull String... permissions) {
            return rxRuntimePermissions.requestPermissions(this, trigger, requestCode, checkNotNull(permissions))
                    .map(new AreAllGranted());
        }

        @Override
        public void call(@NonNull Integer requestCode, @NonNull String[] permissions) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    private static class AreAllGranted implements Func1<List<PermissionResult>, Boolean> {

        @Override
        public Boolean call(List<PermissionResult> permissionsResult) {
            return Observable.from(permissionsResult).all(new Func1<PermissionResult, Boolean>() {
                @Override
                public Boolean call(PermissionResult permissionResult) {
                    return permissionResult.isGranted();
                }
            }).toBlocking().single();
        }
    }

    private static <T> T checkNotNull(T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return object;
    }
}
