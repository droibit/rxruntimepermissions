package com.github.droibit.rxruntimepermissions;

import com.github.droibit.rxruntimepermissions.PermissionsResult.Permission;

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
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;


public class RequestPermissionsSourceFactoryTest {

    private static final int REQUEST_CODE = 1;

    private static final String[] PERMISSIONS = {"test-1", "test-2", "test-3"};

    @Mock
    RxRuntimePermissions rxRuntimePermissions;

    @Mock
    Activity activity;

    private RequestPermissionsSourceFactory.SourceActivity requestPermissionsSource;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        requestPermissionsSource = new RequestPermissionsSourceFactory.SourceActivity(rxRuntimePermissions, activity);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestPermissions_allGranted() {
        // single permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<String[], Integer>) any(),
                    (Func1<String, Boolean>) any(),
                    (Observable<?>) any(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE)));
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
        }

        // multiple permissions
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[1], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[2], GrantResult.GRANTED)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<String[], Integer>) any(),
                    (Func1<String, Boolean>) any(),
                    (Observable<?>) any(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE)));
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestPermissions_someDenied() {
        final List<Permission> permissions = Arrays.asList(
                new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                new Permission(PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                new Permission(PERMISSIONS[2], GrantResult.GRANTED)
        );

        when(rxRuntimePermissions.requestPermissions(
                (Action2<String[], Integer>) any(),
                (Func1<String, Boolean>) any(),
                (Observable<?>) any(),
                anyInt(),
                any(String[].class)
        )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

        final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
        requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);

        final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
        assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE)));
        assertThat(permissionsResult.permissions, is(equalTo(permissions)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestPermissions_allDenied() {
        // onsinglee permission
        {
            final List<Permission> permissions = Collections.singletonList(
                    new Permission(PERMISSIONS[0], GrantResult.SHOULD_SHOW_RATIONALE)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<String[], Integer>) any(),
                    (Func1<String, Boolean>) any(),
                    (Observable<?>) any(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE)));
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
        }

        // multiple permissions
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(PERMISSIONS[2], GrantResult.SHOULD_SHOW_RATIONALE)
            );

            when(rxRuntimePermissions.requestPermissions(
                    (Action2<String[], Integer>) any(),
                    (Func1<String, Boolean>) any(),
                    (Observable<?>) any(),
                    anyInt(),
                    any(String[].class)
            )).thenReturn(Observable.just(new PermissionsResult(REQUEST_CODE, permissions)));

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            requestPermissionsSource.requestPermissions(REQUEST_CODE, PERMISSIONS[0])
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE)));
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
        }
    }
}