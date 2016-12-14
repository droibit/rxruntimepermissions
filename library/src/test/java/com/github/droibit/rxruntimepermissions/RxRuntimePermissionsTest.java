package com.github.droibit.rxruntimepermissions;

import com.github.droibit.rxruntimepermissions.PermissionsResult.GrantResult;
import com.github.droibit.rxruntimepermissions.PermissionsResult.Permission;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.functions.Action2;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class RxRuntimePermissionsTest {

    private static final int REQUEST_CODE_1 = 1;

    private static final int REQUEST_CODE_2 = 2;

    private static final String[] SINGLE_PERMISSION = {"test-1"};

    private static final String[] MULTIPLE_PERMISSIONS = {"test-1", "test-2", "test-3"};

    @Mock
    Action2<String[], Integer> requestPermissions;

    @Mock
    Func1<String, Boolean> showRationaleChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void requestPermissions_hasNotTrigger() {
        when(showRationaleChecker.call(anyString())).thenReturn(true);

        // one permission
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions
                    .requestPermissions(requestPermissions, showRationaleChecker, null, REQUEST_CODE_1, SINGLE_PERMISSION)
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
                    new Permission(SINGLE_PERMISSION[0], GrantResult.GRANTED)
            );
            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE_1)));
        }

        // multiple permissions
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions
                    .requestPermissions(requestPermissions, showRationaleChecker, null, REQUEST_CODE_1, MULTIPLE_PERMISSIONS)
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
                    new Permission(MULTIPLE_PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(MULTIPLE_PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(MULTIPLE_PERMISSIONS[2], GrantResult.GRANTED)
            );
            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE_1)));
        }
    }

    @Test
    public void requestPermissions_hasTrigger() {
        when(showRationaleChecker.call(anyString())).thenReturn(true);

        // single permission
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions
                    .requestPermissions(requestPermissions, showRationaleChecker, trigger, REQUEST_CODE_1, SINGLE_PERMISSION)
                    .subscribe(testSubscriber);

            trigger.onNext(null);
            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    SINGLE_PERMISSION,
                    new int[]{PERMISSION_GRANTED}
            );
            testSubscriber.assertNoTerminalEvent();

            final List<Permission> permissions = asList(
                    new Permission(SINGLE_PERMISSION[0], GrantResult.GRANTED)
            );
            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE_1)));
        }

        // multiple permissions
        {
            final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
            rxRuntimePermissions.requestPermissions(requestPermissions, showRationaleChecker, trigger, REQUEST_CODE_1,
                    MULTIPLE_PERMISSIONS)
                    .subscribe(testSubscriber);

            trigger.onNext(null);
            rxRuntimePermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    MULTIPLE_PERMISSIONS,
                    new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
            );
            testSubscriber.assertNoTerminalEvent();

            final List<Permission> permissions = asList(
                    new Permission(MULTIPLE_PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(MULTIPLE_PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(MULTIPLE_PERMISSIONS[2], GrantResult.GRANTED)
            );
            final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
            assertThat(permissionsResult.permissions, is(equalTo(permissions)));
            assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE_1)));
        }
    }

    @Test
    public void requestPermissions_hasMultipleSubscribers() {
        final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

        final PublishSubject<Object> trigger1 = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriber1 = TestSubscriber.create();
        rxRuntimePermissions
                .requestPermissions(requestPermissions, showRationaleChecker, trigger1, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriber1);

        final PublishSubject<Object> trigger2 = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriber2 = TestSubscriber.create();
        rxRuntimePermissions
                .requestPermissions(requestPermissions, showRationaleChecker, trigger2, REQUEST_CODE_2, MULTIPLE_PERMISSIONS)
                .subscribe(testSubscriber2);

        when(showRationaleChecker.call(anyString())).thenReturn(true);

        // fired trigger1
        trigger1.onNext(null);
        rxRuntimePermissions.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );

        testSubscriber2.assertNoValues();
        testSubscriber1.assertNoTerminalEvent();

        final List<Permission> permissions1 = asList(
                new Permission(SINGLE_PERMISSION[0], GrantResult.GRANTED)
        );
        final PermissionsResult permissionsResult1 = testSubscriber1.getOnNextEvents().get(0);
        assertThat(permissionsResult1.permissions, is(equalTo(permissions1)));
        assertThat(permissionsResult1.requestCode, is(equalTo(REQUEST_CODE_1)));

        // fired trigger2
        trigger2.onNext(null);
        rxRuntimePermissions.onRequestPermissionsResult(
                REQUEST_CODE_2,
                MULTIPLE_PERMISSIONS,
                new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
        );

        testSubscriber2.assertNoTerminalEvent();
        testSubscriber1.assertValueCount(1);

        final List<Permission> permissions2 = asList(
                new Permission(MULTIPLE_PERMISSIONS[0], GrantResult.GRANTED),
                new Permission(MULTIPLE_PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                new Permission(MULTIPLE_PERMISSIONS[2], GrantResult.GRANTED)
        );
        final PermissionsResult permissionsResult2 = testSubscriber2.getOnNextEvents().get(0);
        assertThat(permissionsResult2.permissions, is(equalTo(permissions2)));
        assertThat(permissionsResult2.requestCode, is(equalTo(REQUEST_CODE_2)));
    }

    @Test
    public void requestPermissions_screenRotated() {
        final RxRuntimePermissions rxRuntimePermissionsBefore = new RxRuntimePermissions();

        final PublishSubject<Object> triggerBefore = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriberBefore = TestSubscriber.create();
        rxRuntimePermissionsBefore
                .requestPermissions(requestPermissions, showRationaleChecker, triggerBefore, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriberBefore);

        // After launching the activity, screen is rotated.
        triggerBefore.onNext(null);
        testSubscriberBefore.assertNoValues();

        // New RxRuntimePermissions is created.
        final RxRuntimePermissions rxRuntimePermissionsAfter = new RxRuntimePermissions();

        final PublishSubject<Object> triggerAfter = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriberAfter = TestSubscriber.create();
        rxRuntimePermissionsAfter
                .requestPermissions(requestPermissions, showRationaleChecker, triggerAfter, REQUEST_CODE_1, SINGLE_PERMISSION)
                .subscribe(testSubscriberAfter);

        // New RxRuntimePermissions receives result of permissions result.
        rxRuntimePermissionsAfter.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );
        testSubscriberAfter.assertNoTerminalEvent();

        final List<Permission> permissions = asList(
                new Permission(SINGLE_PERMISSION[0], GrantResult.GRANTED)
        );
        final PermissionsResult permissionsResult = testSubscriberAfter.getOnNextEvents().get(0);
        assertThat(permissionsResult.permissions, is(equalTo(permissions)));
        assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE_1)));
    }

    @Test
    public void requestPermissions_deniedDetails() {
        final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

        when(showRationaleChecker.call(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        final PublishSubject<Object> trigger = PublishSubject.create();
        final TestSubscriber<PermissionsResult> testSubscriber = TestSubscriber.create();
        rxRuntimePermissions.requestPermissions(requestPermissions, showRationaleChecker, trigger, REQUEST_CODE_1,
                MULTIPLE_PERMISSIONS)
                .subscribe(testSubscriber);

        trigger.onNext(null);
        rxRuntimePermissions.onRequestPermissionsResult(
                REQUEST_CODE_1,
                MULTIPLE_PERMISSIONS,
                new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_DENIED}
        );
        testSubscriber.assertNoTerminalEvent();

        final List<Permission> permissions = asList(
                new Permission(MULTIPLE_PERMISSIONS[0], GrantResult.GRANTED),
                new Permission(MULTIPLE_PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                new Permission(MULTIPLE_PERMISSIONS[2], GrantResult.NEVER_ASK_AGAIN)
        );
        final PermissionsResult permissionsResult = testSubscriber.getOnNextEvents().get(0);
        assertThat(permissionsResult.permissions, is(equalTo(permissions)));
        assertThat(permissionsResult.requestCode, is(equalTo(REQUEST_CODE_1)));
    }
}