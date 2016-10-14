package com.github.droibit.rxruntimepermissions;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;

import rx.functions.Action2;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class PendingRequestPermissionsAction {

    final PublishSubject<Void> trigger = PublishSubject.create();

    final Action2<Integer, String[]> requestPermissions;

    final Func1<String, Boolean> showRationaleChecker;

    public PendingRequestPermissionsAction(@NonNull final Activity activity) {
        this(new Action2<Integer, String[]>() {
            @Override
            public void call(@NonNull Integer requestCode, @NonNull String[] permissions) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }
        }, new Func1<String, Boolean>() {
            @Override
            public Boolean call(@NonNull String permission) {
                return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            }
        });
    }

    @VisibleForTesting
    PendingRequestPermissionsAction(
            @NonNull Action2<Integer, String[]> requestPermissions,
            @NonNull Func1<String, Boolean> showRationaleChecker) {
        this.requestPermissions = requestPermissions;
        this.showRationaleChecker = showRationaleChecker;
    }

    public void call() {
        trigger.onNext(null);
    }
}
