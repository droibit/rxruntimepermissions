package com.github.droibit.rxruntimepermissions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.app.Activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Action2;
import rx.observers.TestSubscriber;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;


public class RequestPermissionsSourceFactoryTest {

    private static final int REQUEST_CODE = 1;

    private static final String[] PERMISSIONS = {"test-1", "test-2", "test-3"};

    @Mock
    RxRuntimePermissions rxRuntimePermissions;

    @Mock
    Activity activity;

    private RequestPermissionsSourceFactory.FromActivity requestPermissionsSource;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        requestPermissionsSource = new RequestPermissionsSourceFactory.FromActivity(rxRuntimePermissions, activity);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestPermissions_allGranted() {
        // single permission
        {
            final List<PermissionsResult.Permission> permissions = Collections.singletonList(
                    new PermissionsResult.Permission(PERMISSIONS[0], PERMISSION_GRANTED)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<Integer, String[]>) anyObject(),
                    (Observable<?>) anyObject(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            testSubscriber.assertValue(true);
        }

        // multiple permissions
        {
            final List<PermissionsResult.Permission> permissions = Arrays.asList(
                    new PermissionsResult.Permission(PERMISSIONS[0], PERMISSION_GRANTED),
                    new PermissionsResult.Permission(PERMISSIONS[1], PERMISSION_GRANTED),
                    new PermissionsResult.Permission(PERMISSIONS[2], PERMISSION_GRANTED)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<Integer, String[]>) anyObject(),
                    (Observable<?>) anyObject(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            testSubscriber.assertValue(true);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestPermissions_someDenied() {
        final List<PermissionsResult.Permission> permissions = Arrays.asList(
                new PermissionsResult.Permission(PERMISSIONS[0], PERMISSION_GRANTED),
                new PermissionsResult.Permission(PERMISSIONS[1], PERMISSION_DENIED),
                new PermissionsResult.Permission(PERMISSIONS[2], PERMISSION_GRANTED)
        );

        when(rxRuntimePermissions.requestPermissions(
                (Action2<Integer, String[]>) anyObject(),
                (Observable<?>) anyObject(),
                anyInt(),
                any(String[].class)
        )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

        final TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
        requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestPermissions_allDenied() {
        // onsinglee permission
        {
            final List<PermissionsResult.Permission> permissions = Collections.singletonList(
                    new PermissionsResult.Permission(PERMISSIONS[0], PERMISSION_DENIED)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<Integer, String[]>) anyObject(),
                    (Observable<?>) anyObject(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            testSubscriber.assertValue(false);
        }

        // multiple permissions
        {
            final List<PermissionsResult.Permission> permissions = Arrays.asList(
                    new PermissionsResult.Permission(PERMISSIONS[0], PERMISSION_DENIED),
                    new PermissionsResult.Permission(PERMISSIONS[1], PERMISSION_DENIED),
                    new PermissionsResult.Permission(PERMISSIONS[2], PERMISSION_DENIED)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<Integer, String[]>) anyObject(),
                    (Observable<?>) anyObject(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            testSubscriber.assertValue(false);
        }
    }
}