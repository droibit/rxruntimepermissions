package com.github.droibit.rxruntimepermissions2;

import com.github.droibit.rxruntimepermissions2.PermissionsResult.GrantResult;
import com.github.droibit.rxruntimepermissions2.PermissionsResult.Permission;
import com.github.droibit.rxruntimepermissions2.RxRuntimePermissions.PermissionsRequest;
import com.github.droibit.rxruntimepermissions2.internal.Notification;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class RxRuntimePermissionsTest {

    private static final int REQUEST_CODE_1 = 1;

    private static final int REQUEST_CODE_2 = 2;

    private static final String[] SINGLE_PERMISSION = {"test-1"};

    private static final String[] MULTIPLE_PERMISSIONS = {"test-1", "test-2", "test-3"};

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private PermissionsRequest permissionsRequest;

    @Test
    public void request() throws Exception {
        when(permissionsRequest.apply(anyString())).thenReturn(true);
        final RxRuntimePermissions rxPermissions = new RxRuntimePermissions(permissionsRequest);

        {
            final TestObserver<PermissionsResult> testObserver = rxPermissions
                    .request(REQUEST_CODE_1, SINGLE_PERMISSION)
                    .test();

            rxPermissions.onRequestPermissionsResult(
                    REQUEST_CODE_1,
                    SINGLE_PERMISSION,
                    new int[]{PERMISSION_GRANTED}
            );

            testObserver
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(singlePermissionsResult());
            assertThat(rxPermissions.subjects.size(), is(0));
        }

        // multiple permissions
        {
            final TestObserver<PermissionsResult> testObserver = rxPermissions
                    .request(REQUEST_CODE_2, MULTIPLE_PERMISSIONS)
                    .test();

            rxPermissions.onRequestPermissionsResult(
                    REQUEST_CODE_2,
                    MULTIPLE_PERMISSIONS,
                    new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
            );

            testObserver
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(multiplePermissionsResult());
            assertThat(rxPermissions.subjects.size(), is(0));
        }
    }

    @Test
    public void thenRequest() throws Exception {
        when(permissionsRequest.apply(anyString())).thenReturn(true);
        final RxRuntimePermissions rxPermissions = new RxRuntimePermissions(permissionsRequest);

        {
            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestObserver<PermissionsResult> testObserver = trigger
                    .compose(rxPermissions.thenRequest(REQUEST_CODE_1, SINGLE_PERMISSION))
                    .test();

            trigger.onNext(Notification.INSTANCE);
            rxPermissions.onRequestPermissionsResult(REQUEST_CODE_1, SINGLE_PERMISSION, new int[]{PERMISSION_GRANTED});

            testObserver
                    .assertNoErrors()
                    .assertValue(singlePermissionsResult());
            assertThat(rxPermissions.subjects.size(), is(1));
        }

        // multiple permissions
        {
            final PublishSubject<Object> trigger = PublishSubject.create();
            final TestObserver<PermissionsResult> testObserver = trigger
                    .compose(rxPermissions.thenRequest(REQUEST_CODE_2, MULTIPLE_PERMISSIONS))
                    .test();

            trigger.onNext(Notification.INSTANCE);
            rxPermissions.onRequestPermissionsResult(
                    REQUEST_CODE_2,
                    MULTIPLE_PERMISSIONS,
                    new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
            );

            testObserver
                    .assertNoErrors()
                    .assertValue(multiplePermissionsResult());
            assertThat(rxPermissions.subjects.size(), is(2));
        }
    }

    @Test
    public void request_multipleFires() throws Exception {
        when(permissionsRequest.apply(anyString())).thenReturn(true);
        final RxRuntimePermissions rxPermissions = new RxRuntimePermissions(permissionsRequest);

        final TestObserver<PermissionsResult> testObserver1 = rxPermissions.request(REQUEST_CODE_1, SINGLE_PERMISSION)
                .test();

        // fire
        rxPermissions.onRequestPermissionsResult(REQUEST_CODE_1, SINGLE_PERMISSION, new int[]{PERMISSION_GRANTED});

        testObserver1
                .assertNoErrors()
                .assertComplete()
                .assertValue(singlePermissionsResult());

        // fire again
        final TestObserver<PermissionsResult> testObserver2 = rxPermissions.request(REQUEST_CODE_1, SINGLE_PERMISSION)
                .test();
        rxPermissions.onRequestPermissionsResult(REQUEST_CODE_1, SINGLE_PERMISSION, new int[]{PERMISSION_GRANTED});

        testObserver2
                .assertNoErrors()
                .assertComplete()
                .assertValues(singlePermissionsResult());

        assertThat(rxPermissions.subjects.size(), is(0));
    }

    @Test
    public void thenRequest_multipleFires() throws Exception {
        when(permissionsRequest.apply(anyString())).thenReturn(true);
        final RxRuntimePermissions rxPermissions = new RxRuntimePermissions(permissionsRequest);

        final PublishSubject<Object> trigger = PublishSubject.create();
        final TestObserver<PermissionsResult> testObserver = trigger
                .compose(rxPermissions.thenRequest(REQUEST_CODE_1, SINGLE_PERMISSION))
                .test();

        // fire
        trigger.onNext(Notification.INSTANCE);
        rxPermissions.onRequestPermissionsResult(REQUEST_CODE_1, SINGLE_PERMISSION, new int[]{PERMISSION_GRANTED});

        final PermissionsResult permissionsResult = singlePermissionsResult();
        testObserver
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(permissionsResult);

        // fire again
        trigger.onNext(Notification.INSTANCE);
        rxPermissions.onRequestPermissionsResult(REQUEST_CODE_1, SINGLE_PERMISSION, new int[]{PERMISSION_GRANTED});

        testObserver
                .assertNoErrors()
                .assertNotComplete()
                .assertValues(permissionsResult, permissionsResult);

        assertThat(rxPermissions.subjects.size(), is(1));
    }

    @Test
    public void thenRequest_multipleTriggers() throws Exception {
        when(permissionsRequest.apply(anyString())).thenReturn(true);
        final RxRuntimePermissions rxPermissions = new RxRuntimePermissions(permissionsRequest);

        final PublishSubject<Object> trigger1 = PublishSubject.create();
        final TestObserver<PermissionsResult> testObserver1 = trigger1
                .compose(rxPermissions.thenRequest(REQUEST_CODE_1, SINGLE_PERMISSION))
                .test();

        final PublishSubject<Object> trigger2 = PublishSubject.create();
        final TestObserver<PermissionsResult> testObserver2 = trigger2
                .compose(rxPermissions.thenRequest(REQUEST_CODE_2, MULTIPLE_PERMISSIONS))
                .test();

        // trigger1 fires
        trigger1.onNext(Notification.INSTANCE);
        rxPermissions.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );

        final PermissionsResult permissionsResult1 = singlePermissionsResult();
        testObserver1
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(permissionsResult1);
        testObserver2.assertNoValues();
        assertThat(rxPermissions.subjects.size(), is(2));

        // trigger2 fires
        trigger2.onNext(Notification.INSTANCE);
        rxPermissions.onRequestPermissionsResult(
                REQUEST_CODE_2,
                MULTIPLE_PERMISSIONS,
                new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_GRANTED}
        );

        final PermissionsResult permissionsResult2 = multiplePermissionsResult();
        testObserver1
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(permissionsResult1);
        testObserver2
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(permissionsResult2);
        assertThat(rxPermissions.subjects.size(), is(2));
    }

    @Test
    public void thenRequest_rotated() throws Exception {
        when(permissionsRequest.apply(anyString())).thenReturn(true);
        final RxRuntimePermissions oldRxPermissions = new RxRuntimePermissions(permissionsRequest);

        final PublishSubject<Object> oldTrigger = PublishSubject.create();
        final TestObserver<PermissionsResult> testOldObserver = oldTrigger
                .compose(oldRxPermissions.thenRequest(REQUEST_CODE_1, SINGLE_PERMISSION))
                .test();

        // After launching the activity, screen is rotated.
        oldTrigger.onNext(Notification.INSTANCE);
        testOldObserver.assertNoValues();

        // New RxRuntimePermissions is created.
        final RxRuntimePermissions newRxPermissions = new RxRuntimePermissions(permissionsRequest);

        final PublishSubject<Object> newTrigger = PublishSubject.create();
        final TestObserver<PermissionsResult> testNewObserver = newTrigger
                .compose(newRxPermissions.thenRequest(REQUEST_CODE_1, SINGLE_PERMISSION))
                .test();

        // New RxRuntimePermissions receives result of permissions result.
        newRxPermissions.onRequestPermissionsResult(
                REQUEST_CODE_1,
                SINGLE_PERMISSION,
                new int[]{PERMISSION_GRANTED}
        );

        testNewObserver
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(singlePermissionsResult());
        assertThat(newRxPermissions.subjects.size(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void request_noPermissions() {
        new RxRuntimePermissions(permissionsRequest)
                .request(REQUEST_CODE_1)
                .subscribe();
    }

    @Test(expected = IllegalArgumentException.class)
    public void thenRequest_noPermissions() {
        final RxRuntimePermissions rxPermissions = new RxRuntimePermissions(permissionsRequest);
        final PublishSubject<Object> trigger = PublishSubject.create();
        trigger.compose(rxPermissions.thenRequest(REQUEST_CODE_1))
                .subscribe();
    }

    @Test
    public void createPermissionsResult() throws Exception {
        when(permissionsRequest.apply(anyString()))
                .thenReturn(false)
                .thenReturn(true);

        final PermissionsResult permissionsResult = new RxRuntimePermissions(permissionsRequest)
                .createPermissionsResult(
                        REQUEST_CODE_1,
                        MULTIPLE_PERMISSIONS,
                        new int[]{PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_DENIED}
                );
        assertThat(permissionsResult, equalTo(
                new PermissionsResult(
                        REQUEST_CODE_1,
                        asList(
                                new Permission(MULTIPLE_PERMISSIONS[0], GrantResult.GRANTED),
                                new Permission(MULTIPLE_PERMISSIONS[1], GrantResult.NEVER_ASK_AGAIN),
                                new Permission(MULTIPLE_PERMISSIONS[2], GrantResult.SHOULD_SHOW_RATIONALE)
                        )
                )
        ));
    }

    private PermissionsResult singlePermissionsResult() {
        return new PermissionsResult(
                REQUEST_CODE_1,
                asList(new Permission(SINGLE_PERMISSION[0], GrantResult.GRANTED))
        );
    }

    private PermissionsResult multiplePermissionsResult() {
        return new PermissionsResult(
                REQUEST_CODE_2,
                asList(
                        new Permission(MULTIPLE_PERMISSIONS[0], GrantResult.GRANTED),
                        new Permission(MULTIPLE_PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                        new Permission(MULTIPLE_PERMISSIONS[2], GrantResult.GRANTED)
                )
        );
    }
}