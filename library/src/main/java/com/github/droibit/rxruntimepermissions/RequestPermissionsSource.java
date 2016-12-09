package com.github.droibit.rxruntimepermissions;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import rx.Observable;

public interface RequestPermissionsSource {

    @NonNull
    @CheckResult
    RequestPermissionsSource on(@NonNull Observable<?> trigger);

    @NonNull
    @CheckResult
    Observable<PermissionsResult> requestPermissions(int requestCode, @NonNull String... permissions);
}
