package com.github.droibit.rxruntimepermissions2;

import com.github.droibit.rxruntimepermissions2.internal.Notification;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class PendingRequestPermissionsAction {

    private final PublishSubject<Object> trigger;

    public PendingRequestPermissionsAction() {
        trigger = PublishSubject.create();
    }

    public void call() {
        trigger.onNext(Notification.INSTANCE);
    }

    @NonNull
    public Observable<Object> asObservable() {
        return trigger;
    }
}
