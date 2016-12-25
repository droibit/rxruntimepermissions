package com.github.droibit.rxruntimepermissions2;

import com.github.droibit.rxruntimepermissions2.internal.Notification;

import io.reactivex.subjects.PublishSubject;

public class PendingRequestPermissionsAction {

    private final PublishSubject<Object> trigger;

    public PendingRequestPermissionsAction() {
        trigger = PublishSubject.create();
    }

    public void call() {
        trigger.onNext(Notification.INSTANCE);
    }
}
