package com.github.droibit.rxruntimepermissions;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class RxRuntimePermissions {

    private final Map<Integer, Pair<PublishSubject<PermissionsResult>, Boolean>> subjects;

    @Nullable
    private final CompositeSubscription subscriptions;

    public RxRuntimePermissions() {
        this(null);
    }

    public RxRuntimePermissions(@Nullable CompositeSubscription subscriptions) {
        this.subjects = new HashMap<>();
        this.subscriptions = subscriptions;
    }

    public RequestPermissionsSource from(@NonNull Activity activity) {
        return new RequestPermissionsSourceFactory.FromActivity(this, activity);
    }

    public PendingRequestPermissionsSource from(@NonNull PendingRequestPermissionsAction action) {
        return new RequestPermissionsSourceFactory.FromAction(this, action);
    }

    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final Pair<PublishSubject<PermissionsResult>, Boolean> triggerableSubject = subjects.get(requestCode);
        if (triggerableSubject == null) {
            return;
        }

        final PublishSubject<PermissionsResult> subject = triggerableSubject.first;
        final boolean hasTrigger = triggerableSubject.second;

        final PermissionsResult permissionsResult = createPermissionsResult(requestCode, permissions, grantResults);
        subject.onNext(permissionsResult);

        if (!hasTrigger) {
            subject.onCompleted();
            subjects.remove(requestCode);
        }
    }

    Observable<PermissionsResult> requestPermissions(
            final Action2<Integer, String[]> requestPermissions,
            @Nullable Observable<?> trigger,
            final int requestCode,
            final String[] permissions) {
        final boolean hasTrigger = trigger != null;
        final PublishSubject<PermissionsResult> subject = createSubjectIfNotExist(requestCode, hasTrigger);

        final Observable<?> observable = trigger != null ? trigger : Observable.just(null);
        final Subscription subscription = observable.subscribe(new Action1<Object>() {
            @Override
            public void call(Object ignored) {
                requestPermissions.call(requestCode, permissions);
            }
        });

        if (trigger != null && subscriptions != null) {
            subscriptions.add(subscription);
        }
        return subject;
    }

    private PublishSubject<PermissionsResult> createSubjectIfNotExist(int requestCode, boolean hasTrigger) {
        if (!subjects.containsKey(requestCode)) {
            final PublishSubject<PermissionsResult> newSubject = PublishSubject.create();
            subjects.put(requestCode, Pair.create(newSubject, hasTrigger));
            return newSubject;
        }
        return subjects.get(requestCode).first;
    }

    private PermissionsResult createPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length != grantResults.length) {
            throw new IllegalArgumentException("permissions.length != grantResults.length");
        }

        final List<PermissionsResult.Permission> results = new ArrayList<>(permissions.length);
        for (int i = 0, length = permissions.length; i < length; i++) {
            results.add(new PermissionsResult.Permission(permissions[i], grantResults[i]));
        }
        return new PermissionsResult(requestCode, results);
    }
}