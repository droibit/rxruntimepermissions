package com.github.droibit.rxruntimepermissions;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import rx.Observable;

public interface TriggeredPermissionsRequest extends PermissionsRequest {

    @NonNull
    @CheckResult
    PermissionsRequest on(@NonNull Observable<?> trigger);
}
