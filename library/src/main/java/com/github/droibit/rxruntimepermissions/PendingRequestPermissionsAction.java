package com.github.droibit.rxruntimepermissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;

import rx.functions.Action2;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class PendingRequestPermissionsAction {

    @NonNull
    @CheckResult
    public static PendingRequestPermissionsAction create(@NonNull final Activity activity) {
        return new PendingRequestPermissionsAction(new Action2<String[], Integer>() {
            @Override
            public void call(@NonNull String[] permissions, @NonNull Integer requestCode) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }
        }, new Func1<String, Boolean>() {
            @Override
            public Boolean call(@NonNull String permission) {
                return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    @NonNull
    @CheckResult
    public static PendingRequestPermissionsAction create(@NonNull final Fragment fragment) {
        return new PendingRequestPermissionsAction(new Action2<String[], Integer>() {
            @Override
            public void call(@NonNull String[] permissions, @NonNull Integer requestCode) {
                fragment.requestPermissions(permissions, requestCode);
            }
        }, new Func1<String, Boolean>() {
            @Override
            public Boolean call(@NonNull String permission) {
                return fragment.shouldShowRequestPermissionRationale(permission);
            }
        });
    }

    @NonNull
    @CheckResult
    public static PendingRequestPermissionsAction create(@NonNull final android.support.v4.app.Fragment fragment) {
        return new PendingRequestPermissionsAction(new Action2<String[], Integer>() {
            @Override
            public void call(@NonNull String[] permissions, @NonNull Integer requestCode) {
                fragment.requestPermissions(permissions, requestCode);
            }
        }, new Func1<String, Boolean>() {
            @Override
            public Boolean call(@NonNull String permission) {
                return fragment.shouldShowRequestPermissionRationale(permission);
            }
        });
    }

    final PublishSubject<Void> trigger = PublishSubject.create();

    final Action2<String[], Integer> requestPermissions;

    final Func1<String, Boolean> showRationaleChecker;

    @VisibleForTesting
    PendingRequestPermissionsAction(
            @NonNull Action2<String[], Integer> requestPermissions,
            @NonNull Func1<String, Boolean> showRationaleChecker) {
        this.requestPermissions = requestPermissions;
        this.showRationaleChecker = showRationaleChecker;
    }

    public void call() {
        trigger.onNext(null);
    }
}
