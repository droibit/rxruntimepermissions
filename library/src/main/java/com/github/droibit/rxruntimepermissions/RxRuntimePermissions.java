package com.github.droibit.rxruntimepermissions;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class RxRuntimePermissions {

    private static class TriggerSubject {

        final boolean hasTrigger;

        final PublishSubject<PermissionsResult> subject;

        final Func1<String, Boolean> showRationaleChecker;

        TriggerSubject(boolean hasTrigger, Func1<String, Boolean> showRationaleChecker) {
            this.hasTrigger = hasTrigger;
            this.subject = PublishSubject.create();
            this.showRationaleChecker = showRationaleChecker;
        }
    }

    private final Map<Integer, TriggerSubject> subjects;

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
        final TriggerSubject triggerSubject = subjects.get(requestCode);
        if (triggerSubject == null) {
            return;
        }

        final PublishSubject<PermissionsResult> subject = triggerSubject.subject;
        final PermissionsResult permissionsResult = createPermissionsResult(requestCode, permissions, grantResults,
                triggerSubject.showRationaleChecker);
        subject.onNext(permissionsResult);

        if (!triggerSubject.hasTrigger) {
            subject.onCompleted();
            subjects.remove(requestCode);
        }
    }

    Observable<PermissionsResult> requestPermissions(
            final Action2<Integer, String[]> requestPermissions,
            final Func1<String, Boolean> showRationaleChecker,
            @Nullable Observable<?> trigger,
            final int requestCode,
            final String[] permissions) {

        final PublishSubject<PermissionsResult> subject = createSubjectIfNotExist(
                requestCode,
                /*hasTrigger=*/trigger != null,
                showRationaleChecker
        );
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

    private PublishSubject<PermissionsResult> createSubjectIfNotExist(int requestCode, boolean hasTrigger,
            Func1<String, Boolean> showRationaleChecker) {
        if (!subjects.containsKey(requestCode)) {
            final TriggerSubject triggerSubject = new TriggerSubject(hasTrigger, showRationaleChecker);
            subjects.put(requestCode, triggerSubject);
            return triggerSubject.subject;
        }
        return subjects.get(requestCode).subject;
    }

    private PermissionsResult createPermissionsResult(int requestCode, String[] permissions, int[] grantResults,
            Func1<String, Boolean> showRationaleChecker) {
        if (permissions.length != grantResults.length) {
            throw new IllegalArgumentException("permissions.length != grantResults.length");
        }

        final List<PermissionsResult.Permission> results = new ArrayList<>(permissions.length);
        for (int i = 0, length = permissions.length; i < length; i++) {
            final GrantResult grantResult = convertGrantResult(permissions[i], grantResults[i], showRationaleChecker);
            results.add(new PermissionsResult.Permission(permissions[i], grantResult));
        }
        return new PermissionsResult(requestCode, results);
    }

    private GrantResult convertGrantResult(String permission, int result, Func1<String, Boolean> showRationaleChecker) {
        if (result == PackageManager.PERMISSION_GRANTED) {
            return GrantResult.GRANTED;
        }
        return showRationaleChecker.call(permission) ? GrantResult.SHOULD_SHOW_RATIONALE : GrantResult.NEVER_ASK_AGAIN;
    }
}