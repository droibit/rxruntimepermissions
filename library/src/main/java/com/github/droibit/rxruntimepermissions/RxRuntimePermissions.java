package com.github.droibit.rxruntimepermissions;


import com.github.droibit.rxruntimepermissions.PermissionsResult.Permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.SparseArrayCompat;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class RxRuntimePermissions {

    private static class TriggeredSubject {

        final boolean hasTrigger;

        final PublishSubject<PermissionsResult> subject;

        final Func1<String, Boolean> showRationaleChecker;

        TriggeredSubject(boolean hasTrigger, Func1<String, Boolean> showRationaleChecker) {
            this.hasTrigger = hasTrigger;
            this.subject = PublishSubject.create();
            this.showRationaleChecker = showRationaleChecker;
        }
    }

    private final SparseArrayCompat<TriggeredSubject> subjects;

    @Nullable
    private final CompositeSubscription subscriptions;

    public RxRuntimePermissions() {
        this(null);
    }

    public RxRuntimePermissions(@Nullable CompositeSubscription subscriptions) {
        this.subjects = new SparseArrayCompat<>();
        this.subscriptions = subscriptions;
    }

    public TriggeredRequestPermissionsSource with(@NonNull Activity activity) {
        return new RequestPermissionsSourceFactory.SourceActivity(this, activity);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public TriggeredRequestPermissionsSource with(@NonNull Fragment fragment) {
        return new RequestPermissionsSourceFactory.SourceFragment(this, fragment);
    }

    public TriggeredRequestPermissionsSource with(@NonNull android.support.v4.app.Fragment fragment) {
        return new RequestPermissionsSourceFactory.SourceSupportFragment(this, fragment);
    }

    public RequestPermissionsSource with(@NonNull PendingRequestPermissionsAction action) {
        return new RequestPermissionsSourceFactory.SourceAction(this, action);
    }

    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final TriggeredSubject triggeredSubject = subjects.get(requestCode);
        if (triggeredSubject == null) {
            return;
        }

        final PublishSubject<PermissionsResult> subject = triggeredSubject.subject;
        final PermissionsResult permissionsResult = createPermissionsResult(
                requestCode,
                permissions,
                grantResults,
                triggeredSubject.showRationaleChecker
        );
        subject.onNext(permissionsResult);

        if (!triggeredSubject.hasTrigger) {
            subject.onCompleted();
            subjects.remove(requestCode);
        }
    }

    @VisibleForTesting
    Observable<PermissionsResult> requestPermissions(
            final Action2<String[], Integer> requestPermissions,
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
                requestPermissions.call(permissions, requestCode);
            }
        });

        if (trigger != null && subscriptions != null) {
            subscriptions.add(subscription);
        }
        return subject;
    }

    private PublishSubject<PermissionsResult> createSubjectIfNotExist(int requestCode, boolean hasTrigger,
            Func1<String, Boolean> showRationaleChecker) {
        final TriggeredSubject target = subjects.get(requestCode);
        if (target == null) {
            final TriggeredSubject triggeredSubject = new TriggeredSubject(hasTrigger, showRationaleChecker);
            subjects.put(requestCode, triggeredSubject);
            return triggeredSubject.subject;
        }
        return target.subject;
    }

    private PermissionsResult createPermissionsResult(int requestCode, String[] permissions, int[] grantResults,
            Func1<String, Boolean> showRationaleChecker) {
        if (permissions.length != grantResults.length) {
            throw new IllegalArgumentException("permissions.length != grantResults.length");
        }

        final List<Permission> results = new ArrayList<>(permissions.length);
        for (int i = 0, length = permissions.length; i < length; i++) {
            final PermissionsResult.GrantResult grantResult = convertGrantResult(permissions[i], grantResults[i], showRationaleChecker);
            results.add(new Permission(permissions[i], grantResult));
        }
        return new PermissionsResult(requestCode, results);
    }

    private PermissionsResult.GrantResult convertGrantResult(String permission, int result, Func1<String, Boolean> showRationaleChecker) {
        if (result == PackageManager.PERMISSION_GRANTED) {
            return PermissionsResult.GrantResult.GRANTED;
        }
        return showRationaleChecker.call(permission) ? PermissionsResult.GrantResult.SHOULD_SHOW_RATIONALE : PermissionsResult.GrantResult.NEVER_ASK_AGAIN;
    }
}