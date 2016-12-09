package com.github.droibit.rxruntimepermissions;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.functions.Action2;
import rx.functions.Func1;

class RequestPermissionsSourceFactory {

    private RequestPermissionsSourceFactory() {
    }

    static class FromActivity implements RequestPermissionsSource, Action2<Integer, String[]>, Func1<String, Boolean> {

        private final RxRuntimePermissions rxRuntimePermissions;

        private final Activity activity;

        @Nullable
        private Observable<?> trigger;

        FromActivity(RxRuntimePermissions rxRuntimePermissions, Activity activity) {
            this.rxRuntimePermissions = rxRuntimePermissions;
            this.activity = checkNotNull(activity);
        }

        @NonNull
        @Override
        public RequestPermissionsSource on(@NonNull Observable<?> trigger) {
            this.trigger = checkNotNull(trigger);
            return this;
        }

        @NonNull
        @Override
        public Observable<PermissionsResult> requestPermissions(int requestCode, @NonNull String... permissions) {
            return rxRuntimePermissions.requestPermissions(
                    /*requestPermissions=*/this,
                    /*showRationaleChecker=*/this,
                    trigger,
                    requestCode,
                    checkNotNull(permissions)
            );
        }

        @Override
        public void call(@NonNull Integer requestCode, @NonNull String[] permissions) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }

        @Override
        public Boolean call(@NonNull String permission) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        }
    }

    static class FromAction implements PendingRequestPermissionsSource {

        private final RxRuntimePermissions rxRuntimePermissions;

        private final PendingRequestPermissionsAction action;

        FromAction(RxRuntimePermissions rxRuntimePermissions, PendingRequestPermissionsAction action) {
            this.rxRuntimePermissions = rxRuntimePermissions;
            this.action = checkNotNull(action);
        }

        @Override
        public Observable<PermissionsResult> requestPermissions(int requestCode, @NonNull String... permissions) {
            return rxRuntimePermissions
                    .requestPermissions(
                            action.requestPermissions,
                            action.showRationaleChecker,
                            action.trigger, requestCode, checkNotNull(permissions)
                    );
        }
    }

    private static <T> T checkNotNull(T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return object;
    }
}
