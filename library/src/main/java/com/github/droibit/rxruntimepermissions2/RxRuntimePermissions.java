package com.github.droibit.rxruntimepermissions2;


import com.github.droibit.rxruntimepermissions2.PermissionsResult.GrantResult;
import com.github.droibit.rxruntimepermissions2.PermissionsResult.Permission;
import com.github.droibit.rxruntimepermissions2.internal.Notification;
import com.github.droibit.rxruntimepermissions2.internal.Transforms;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

/**
 * Provide a way to receive the result of Runtime Permission request using RxJava.<br/>
 *
 * When the Activity(Fragment) receives the result of the request,
 * you <b>must</b> call {@link #onRequestPermissionsResult(int, String[], int[])}.
 */
public class RxRuntimePermissions {

    @RestrictTo(LIBRARY)
    interface PermissionsRequest extends BiConsumer<String[], Integer>, Function<String, Boolean> {

    }

    private static class TriggeredSubject {

        final boolean hasTrigger;

        final PublishSubject<PermissionsResult> actual;

        TriggeredSubject(boolean hasTrigger) {
            this.hasTrigger = hasTrigger;
            this.actual = PublishSubject.create();
        }
    }

    /**
     * Transform from {@link PermissionsResult} list to {@link Boolean}.<br/>
     * Returns {@code true} if all permissions are granted.
     */
    public static Function<PermissionsResult, Boolean> areGranted() {
        return Transforms.ARE_GRANTED;
    }

    /**
     * Transform from {@link PermissionsResult} list to single {@link GrantResult}.<br/>
     * Utility for a single permission grant request.
     */
    public static Function<PermissionsResult, GrantResult> toFirstGrantResult() {
        return Transforms.TO_FIRST_GRANT_RESULT;
    }

    private final PermissionsRequest permissionChecker;

    @VisibleForTesting
    final SparseArrayCompat<TriggeredSubject> subjects;

    public RxRuntimePermissions(@NonNull Activity activity) {
        this(PermissionsRequestFactory.create(activity));
    }

    public RxRuntimePermissions(@NonNull Fragment fragment) {
        this(PermissionsRequestFactory.create(fragment));
    }

    @VisibleForTesting
    RxRuntimePermissions(@NonNull PermissionsRequest permissionChecker) {
        this.permissionChecker = permissionChecker;
        this.subjects = new SparseArrayCompat<>();
    }

    /**
     * Requests permissions to be granted to this application.
     */
    @NonNull
    public Observable<PermissionsResult> request(int requestCode, @NonNull String... permissions) {
        return request(null, permissionChecker, requestCode, permissions);
    }

    /**
     * Requests permissions to be granted to this application.<br/>
     * Use {@link Observable#compose(ObservableTransformer)} of {@link Observable} as trigger in combination.
     */
    @NonNull
    public <T> ObservableTransformer<T, PermissionsResult> thenRequest(final int requestCode,
            @NonNull final String... permissions) {
        return new ObservableTransformer<T, PermissionsResult>() {
            @Override
            public ObservableSource<PermissionsResult> apply(Observable<T> trigger) {
                return request(trigger, permissionChecker, requestCode, permissions);
            }
        };
    }

    /**
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     * @see Fragment#onRequestPermissionsResult(int, String[], int[])
     */
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        final TriggeredSubject triggeredSubject = subjects.get(requestCode);
        if (triggeredSubject == null) {
            return;
        }

        final PublishSubject<PermissionsResult> subject = triggeredSubject.actual;
        final PermissionsResult permissionsResult = createPermissionsResult(requestCode, permissions, grantResults);
        subject.onNext(permissionsResult);

        if (!triggeredSubject.hasTrigger) {
            subject.onComplete();
            subjects.remove(requestCode);
        }
    }

    private Observable<PermissionsResult> request(
            @Nullable Observable<?> trigger,
            final BiConsumer<String[], Integer> requestPermissions,
            final int requestCode,
            final String[] permissions) {

        if (permissions.length == 0) {
            throw new IllegalArgumentException("permissions must not be null.");
        }

        final boolean hasTrigger = trigger != null;
        final Observable<?> requestTrigger = hasTrigger ? trigger : Observable.just(Notification.INSTANCE);
        requestTrigger.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object ignored) throws Exception {
                requestPermissions.accept(permissions, requestCode);
            }
        });
        return createSubjectIfNotExist(requestCode, hasTrigger);
    }

    @VisibleForTesting
    PermissionsResult createPermissionsResult(int requestCode, final String[] permissions, final int[] grantResults) {
        final List<Permission> appPermissions = Observable.range(0, permissions.length)
                .collectInto(new ArrayList<Permission>(permissions.length), new BiConsumer<List<Permission>, Integer>() {
                    @Override
                    public void accept(List<Permission> dest, Integer i) throws Exception {
                        final GrantResult grantResult = toAppGrantResult(permissionChecker, permissions[i], grantResults[i]);
                        dest.add(new Permission(permissions[i], grantResult));
                    }
                }).blockingGet();

        return new PermissionsResult(requestCode, appPermissions);
    }

    private GrantResult toAppGrantResult(
            Function<String, Boolean> showRationaleChecker, String permission, int grantResult) {
        try {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                return GrantResult.GRANTED;
            }
            return showRationaleChecker.apply(permission)
                    ? GrantResult.SHOULD_SHOW_RATIONALE : GrantResult.NEVER_ASK_AGAIN;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PublishSubject<PermissionsResult> createSubjectIfNotExist(int requestCode, boolean hasTrigger) {
        final TriggeredSubject existSubject = subjects.get(requestCode);
        if (existSubject != null) {
            return existSubject.actual;
        }
        final TriggeredSubject newSubject = new TriggeredSubject(hasTrigger);
        subjects.put(requestCode, newSubject);
        return newSubject.actual;
    }
}
