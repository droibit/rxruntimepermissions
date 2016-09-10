package com.github.droibit.rxruntimepermissions;

import com.github.droibit.rxruntimepermissions.PermissionsResult.Permission;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.support.v4.content.PermissionChecker;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import rx.functions.Action2;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
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

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
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

            final List<Permission> permissions = asList(
                    new Permission(SINGLE_PERMISSION[0], PERMISSION_GRANTED)
            );
            testSubscriber.assertValue(new PermissionsResult(REQUEST_CODE_1, permissions));
        }

        // multiple permissions
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
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

            final List<Permission> permissions = asList(
                    new Permission(MULTIPLE_PERMISSIONS[0], PERMISSION_GRANTED),
                    new Permission(MULTIPLE_PERMISSIONS[1], PERMISSION_DENIED),
                    new Permission(MULTIPLE_PERMISSIONS[2], PERMISSION_GRANTED)
            );
            testSubscriber.assertValue(new PermissionsResult(REQUEST_CODE_1, permissions));
        }
    }

    @Test
    public void requestPermissions_hasTrigger() {
        // single permission
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions.requestPermissions(requestPermissions, trigger, REQUEST_CODE_1, SINGLE_PERMISSION)
                    .subscribe(testSubscriber);

            trigger.onNext(null);
            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    SINGLE_PERMISSION,
                    new int[]{PERMISSION_GRANTED}
            );

            final List<Permission> permissions = asList(
                    new Permission(SINGLE_PERMISSION[0], PERMISSION_GRANTED)
            );
            testSubscriber.assertReceivedOnNext(asList(new PermissionsResult(REQUEST_CODE_1, permissions)));
            testSubscriber.assertNoTerminalEvent();
        }

        // multiple permissions
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions.requestPermissions(requestPermissions, trigger, REQUEST_CODE_1, MULTIPLE_PERMISSIONS)
                    .subscribe(testSubscriber);

            trigger.onNext(null);
            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    MULTIPLE_PERMISSIONS,
                    new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
            );

            final List<Permission> permissions = asList(
                    new Permission(MULTIPLE_PERMISSIONS[0], PERMISSION_GRANTED),
                    new Permission(MULTIPLE_PERMISSIONS[1], PERMISSION_DENIED),
                    new Permission(MULTIPLE_PERMISSIONS[2], PERMISSION_GRANTED)
            );
            testSubscriber.assertReceivedOnNext(asList(new PermissionsResult(REQUEST_CODE_1, permissions)));
            testSubscriber.assertNoTerminalEvent();
        }
    }

    @Test
    public void requestPermissions_hasMultipleSubscribers() {
        final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

        final PublishSubject<Object> trigger1 = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriber1 = TestSubscriber.create();
        rxRuntimePermissions.requestPermissions(requestPermissions, trigger1, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriber1);

        final PublishSubject<Object> trigger2 = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriber2 = TestSubscriber.create();
        rxRuntimePermissions.requestPermissions(requestPermissions, trigger2, REQUEST_CODE_2, MULTIPLE_PERMISSIONS)
                .subscribe(testSubscriber2);

        // fired trigger1
        trigger1.onNext(null);
        rxRuntimePermissions.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );

        final List<Permission> permissions1 = asList(
                new Permission(SINGLE_PERMISSION[0], PERMISSION_GRANTED)
        );
        testSubscriber1.assertReceivedOnNext(asList(new PermissionsResult(REQUEST_CODE_1, permissions1)));
        testSubscriber1.assertNoTerminalEvent();
        testSubscriber2.assertNoValues();

        // fired trigger2
        trigger2.onNext(null);
        rxRuntimePermissions.onRequestPermissionsResult(
                REQUEST_CODE_2,
                MULTIPLE_PERMISSIONS,
                new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
        );

        final List<Permission> permissions2 = asList(
                new Permission(MULTIPLE_PERMISSIONS[0], PERMISSION_GRANTED),
                new Permission(MULTIPLE_PERMISSIONS[1], PERMISSION_DENIED),
                new Permission(MULTIPLE_PERMISSIONS[2], PERMISSION_GRANTED)
        );
        testSubscriber2.assertReceivedOnNext(asList(new PermissionsResult(REQUEST_CODE_2, permissions2)));
        testSubscriber2.assertNoTerminalEvent();
        testSubscriber1.assertValueCount(1);
    }

    @Test
    public void requestPermissions_screenRotated() {
        final RxRuntimePermissions rxRuntimePermissionsBefore = new RxRuntimePermissions();

        final PublishSubject<Object> triggerBefore = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriberBefore = TestSubscriber.create();
        rxRuntimePermissionsBefore.requestPermissions(requestPermissions, triggerBefore, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriberBefore);

        // After launching the activity, screen is rotated.
        triggerBefore.onNext(null);
        testSubscriberBefore.assertNoValues();

        // New RxRuntimePermissions is created.
        final RxRuntimePermissions rxRuntimePermissionsAfter = new RxRuntimePermissions();

        final PublishSubject<Object> triggerAfter = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriberAfter = TestSubscriber.create();
        rxRuntimePermissionsAfter.requestPermissions(requestPermissions, triggerAfter, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriberAfter);

        // New RxRuntimePermissions receives result of permissions result.
        rxRuntimePermissionsAfter.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );

        final List<Permission> permissions = asList(
                new Permission(SINGLE_PERMISSION[0], PERMISSION_GRANTED)
        );
        testSubscriberAfter.assertReceivedOnNext(asList(new PermissionsResult(REQUEST_CODE_1, permissions)));
        testSubscriberAfter.assertNoTerminalEvent();
    }
}