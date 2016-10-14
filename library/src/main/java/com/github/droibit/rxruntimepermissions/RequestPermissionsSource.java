package com.github.droibit.rxruntimepermissions;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import rx.Observable;

public interface RequestPermissionsSource {

    @CheckResult
    RequestPermissionsSource on(@NonNull Observable<?> trigger);

    @CheckResult
    Observable<PermissionsResult> requestPermissions(int requestCode, @NonNull String... permissions);
}
