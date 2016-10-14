package com.github.droibit.rxruntimepermissions;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import rx.Observable;

public interface PendingRequestPermissionsSource {

    @CheckResult
    Observable<PermissionsResult> requestPermissions(int requestCode, @NonNull String... permissions);
}
