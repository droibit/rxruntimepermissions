package com.github.droibit.rxruntimepermissions;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;

import rx.functions.Action2;
import rx.subjects.PublishSubject;

public class PendingRequestPermissionsAction {

    final PublishSubject<Void> trigger = PublishSubject.create();

    final Action2<Integer, String[]> requestPermissions;

    public PendingRequestPermissionsAction(@NonNull final Activity activity) {
        this(new Action2<Integer, String[]>() {
            @Override
            public void call(@NonNull Integer requestCode, @NonNull String[] permissions) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }
        });
    }

    @VisibleForTesting
    PendingRequestPermissionsAction(@NonNull Action2<Integer, String[]> requestPermissions) {
        this.requestPermissions = requestPermissions;
    }

    public void call() {
        trigger.onNext(null);
    }
}
