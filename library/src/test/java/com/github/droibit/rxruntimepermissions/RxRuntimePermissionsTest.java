package com.github.droibit.rxruntimepermissions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import rx.functions.Action2;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.util.Collections.singletonList;

public class RxRuntimePermissionsTest {

    private static final int REQUEST_CODE_1 = 1;

    private static final int REQUEST_CODE_2 = 2;

    private static final String[] SINGLE_PERMISSION = {"test-1"};

    private static final String[] MULTIPLE_PERMISSIONS = {"test-1", "test-2", "test-3"};

    @Mock
    Action2<Integer, String[]> requestPermissions;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void requestPermissions_hasNotTrigger() {
        // one permission
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final TestSubscriber<List<PermissionResult>> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions.requestPermissions(requestPermissions, null, REQUEST_CODE_1, SINGLE_PERMISSION)
                    .subscribe(testSubscriber);

            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    SINGLE_PERMISSION,
                    new int[]{PERMISSION_GRANTED}
            );

            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();
            testSubscriber.assertUnsubscribed();

            final List<PermissionResult> permissionsResult = singletonList(
                    new PermissionResult(REQUEST_CODE_1, SINGLE_PERMISSION[0], PERMISSION_GRANTED)
            );
            testSubscriber.assertValue(permissionsResult);
        }

        // multiple permissions
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final TestSubscriber<List<PermissionResult>> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions.requestPermissions(requestPermissions, null, REQUEST_CODE_1, MULTIPLE_PERMISSIONS)
                    .subscribe(testSubscriber);

            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    MULTIPLE_PERMISSIONS,
                    new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
            );

            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();
            testSubscriber.assertUnsubscribed();

            final List<PermissionResult> permissionsResult = Arrays.asList(
                    new PermissionResult(REQUEST_CODE_1, MULTIPLE_PERMISSIONS[0], PERMISSION_GRANTED),
                    new PermissionResult(REQUEST_CODE_1, MULTIPLE_PERMISSIONS[1], PERMISSION_DENIED),
                    new PermissionResult(REQUEST_CODE_1, MULTIPLE_PERMISSIONS[2], PERMISSION_GRANTED)
            );
            testSubscriber.assertValue(permissionsResult);
        }
    }

    @Test
    public void requestPermissions_hasTrigger() {
        // single permission
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestSubscriber<List<PermissionResult>> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions.requestPermissions(requestPermissions, trigger, REQUEST_CODE_1, SINGLE_PERMISSION)
                    .subscribe(testSubscriber);

            trigger.onNext(null);
            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    SINGLE_PERMISSION,
                    new int[]{PERMISSION_GRANTED}
            );

            final List<PermissionResult> permissionsResult = singletonList(
                    new PermissionResult(REQUEST_CODE_1, SINGLE_PERMISSION[0], PERMISSION_GRANTED)
            );
            testSubscriber.assertReceivedOnNext(singletonList(permissionsResult));
            testSubscriber.assertNoTerminalEvent();
        }

        // multiple permissions
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestSubscriber<List<PermissionResult>> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions.requestPermissions(requestPermissions, trigger, REQUEST_CODE_1, MULTIPLE_PERMISSIONS)
                    .subscribe(testSubscriber);

            trigger.onNext(null);
            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    MULTIPLE_PERMISSIONS,
                    new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
            );

            final List<PermissionResult> permissionsResult = Arrays.asList(
                    new PermissionResult(REQUEST_CODE_1, MULTIPLE_PERMISSIONS[0], PERMISSION_GRANTED),
                    new PermissionResult(REQUEST_CODE_1, MULTIPLE_PERMISSIONS[1], PERMISSION_DENIED),
                    new PermissionResult(REQUEST_CODE_1, MULTIPLE_PERMISSIONS[2], PERMISSION_GRANTED)
            );
            testSubscriber.assertReceivedOnNext(singletonList(permissionsResult));
            testSubscriber.assertNoTerminalEvent();
        }
    }

    @Test
    public void requestPermissions_hasMultipleSubscribers() {
        final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

        final PublishSubject<Object> trigger1 = PublishSubject.create();
        final TestSubscriber<List<PermissionResult>> testSubscriber1 = TestSubscriber.create();
        rxRuntimePermissions.requestPermissions(requestPermissions, trigger1, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriber1);

        final PublishSubject<Object> trigger2 = PublishSubject.create();
        final TestSubscriber<List<PermissionResult>> testSubscriber2 = TestSubscriber.create();
        rxRuntimePermissions.requestPermissions(requestPermissions, trigger2, REQUEST_CODE_2, MULTIPLE_PERMISSIONS)
                .subscribe(testSubscriber2);

        // fired trigger1
        trigger1.onNext(null);
        rxRuntimePermissions.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );

        final List<PermissionResult> permissionsResult1 = singletonList(
                new PermissionResult(REQUEST_CODE_1, SINGLE_PERMISSION[0], PERMISSION_GRANTED)
        );
        testSubscriber1.assertReceivedOnNext(singletonList(permissionsResult1));
        testSubscriber1.assertNoTerminalEvent();
        testSubscriber2.assertNoValues();

        // fired trigger2
        trigger2.onNext(null);
        rxRuntimePermissions.onRequestPermissionsResult(
                REQUEST_CODE_2,
                MULTIPLE_PERMISSIONS,
                new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
        );

        final List<PermissionResult> permissionsResult2 = Arrays.asList(
                new PermissionResult(REQUEST_CODE_2, MULTIPLE_PERMISSIONS[0], PERMISSION_GRANTED),
                new PermissionResult(REQUEST_CODE_2, MULTIPLE_PERMISSIONS[1], PERMISSION_DENIED),
                new PermissionResult(REQUEST_CODE_2, MULTIPLE_PERMISSIONS[2], PERMISSION_GRANTED)
        );
        testSubscriber2.assertReceivedOnNext(singletonList(permissionsResult2));
        testSubscriber2.assertNoTerminalEvent();
        testSubscriber1.assertValueCount(1);
    }

    @Test
    public void requestPermissions_screenRotated() {
        final RxRuntimePermissions rxRuntimePermissionsBefore = new RxRuntimePermissions();

        final PublishSubject<Object> triggerBefore = PublishSubject.create();
        final TestSubscriber<List<PermissionResult>> testSubscriberBefore = TestSubscriber.create();
        rxRuntimePermissionsBefore.requestPermissions(requestPermissions, triggerBefore, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriberBefore);

        // After launching the activity, screen is rotated.
        triggerBefore.onNext(null);
        testSubscriberBefore.assertNoValues();

        // New RxRuntimePermissions is created.
        final RxRuntimePermissions rxRuntimePermissionsAfter = new RxRuntimePermissions();

        final PublishSubject<Object> triggerAfter = PublishSubject.create();
        final TestSubscriber<List<PermissionResult>> testSubscriberAfter = TestSubscriber.create();
        rxRuntimePermissionsAfter.requestPermissions(requestPermissions, triggerAfter, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriberAfter);

        // New RxRuntimePermissions receives result of permissions result.
        rxRuntimePermissionsAfter.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );

        final List<PermissionResult> permissionsResult = singletonList(
                new PermissionResult(REQUEST_CODE_1, SINGLE_PERMISSION[0], PERMISSION_GRANTED)
        );
        testSubscriberAfter.assertReceivedOnNext(singletonList(permissionsResult));
        testSubscriberAfter.assertNoTerminalEvent();
    }
}