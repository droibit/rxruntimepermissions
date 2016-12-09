package com.github.droibit.rxruntimepermissions;

import com.github.droibit.rxruntimepermissions.PermissionsResult.Permission;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;

public class TransformsTest {

    private static final int REQUEST_CODE = 1;

    private static final String[] PERMISSIONS = {"test-1", "test-2", "test-3"};

    @Test
    public void areGranted_allGranted() {
        // one permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED)
            );

            TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.areGranted())
                    .subscribe(testSubscriber);

            testSubscriber.assertReceivedOnNext(Arrays.asList(true));
        }

        // multiple permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[1], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[2], GrantResult.GRANTED)
            );

            TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.areGranted())
                    .subscribe(testSubscriber);

            testSubscriber.assertReceivedOnNext(Arrays.asList(true));
        }
    }

    @Test
    public void areGranted_containsDenied() {
        // one permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.SHOULD_SHOW_RATIONALE)
            );

            TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.areGranted())
                    .subscribe(testSubscriber);

            testSubscriber.assertReceivedOnNext(Arrays.asList(false));
        }

        // multiple permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(PERMISSIONS[2], GrantResult.NEVER_ASK_AGAIN)
            );

            TestSubscriber<Boolean> testSubscriber = TestSubscriber.create();
            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.areGranted())
                    .subscribe(testSubscriber);

            testSubscriber.assertReceivedOnNext(Arrays.asList(false));
        }
    }

    @Test
    public void toGrantResult() {
        // one permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.SHOULD_SHOW_RATIONALE)
            );

            TestSubscriber<GrantResult> testSubscriber = TestSubscriber.create();
            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.toSingleGrantResult())
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertReceivedOnNext(Arrays.asList(GrantResult.SHOULD_SHOW_RATIONALE));
        }

        // multiple permissions
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(PERMISSIONS[2], GrantResult.NEVER_ASK_AGAIN)
            );

            TestSubscriber<GrantResult> testSubscriber = TestSubscriber.create();
            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.toSingleGrantResult())
                    .subscribe(testSubscriber);

            testSubscriber.assertError(IllegalArgumentException.class);
            testSubscriber.assertNoValues();
        }
    }
}
