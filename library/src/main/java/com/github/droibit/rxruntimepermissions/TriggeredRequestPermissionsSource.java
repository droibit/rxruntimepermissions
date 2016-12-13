package com.github.droibit.rxruntimepermissions;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import rx.Observable;

public interface TriggeredRequestPermissionsSource extends RequestPermissionsSource {

    @NonNull
    @CheckResult
    TriggeredRequestPermissionsSource on(@NonNull Observable<?> trigger);
}
