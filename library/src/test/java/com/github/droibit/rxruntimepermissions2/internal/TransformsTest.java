package com.github.droibit.rxruntimepermissions2.internal;

import com.github.droibit.rxruntimepermissions2.PermissionsResult;
import com.github.droibit.rxruntimepermissions2.PermissionsResult.GrantResult;
import com.github.droibit.rxruntimepermissions2.PermissionsResult.Permission;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;


@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class TransformsTest {

    private static final int REQUEST_CODE = 1;

    private static final String[] PERMISSIONS = {"test-1", "test-2", "test-3"};

    @Test
    public void areGranted_granted() {
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED)
            );

            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.ARE_GRANTED)
                    .test()
                    .assertValue(true);
        }

        // multiple permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[1], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[2], GrantResult.GRANTED)
            );

            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.ARE_GRANTED)
                    .test()
                    .assertValue(true);
        }
    }

    @Test
    public void areGranted_denied() {
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.SHOULD_SHOW_RATIONALE)
            );

            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.ARE_GRANTED)
                    .test()
                    .assertValue(false);
        }

        // multiple permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(PERMISSIONS[2], GrantResult.NEVER_ASK_AGAIN)
            );

            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.ARE_GRANTED)
                    .test()
                    .assertValue(false);
        }
    }

    @Test
    public void toGrantResult() {
        {
            final List<Permission> granted = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED)
            );
            Observable.just(new PermissionsResult(REQUEST_CODE, granted))
                    .map(Transforms.TO_FIRST_GRANT_RESULT)
                    .test()
                    .assertNoErrors()
                    .assertValue(GrantResult.GRANTED);

            final List<Permission> shouldShowRationale = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.SHOULD_SHOW_RATIONALE)
            );
            Observable.just(new PermissionsResult(REQUEST_CODE, shouldShowRationale))
                    .map(Transforms.TO_FIRST_GRANT_RESULT)
                    .test()
                    .assertNoErrors()
                    .assertValue(GrantResult.SHOULD_SHOW_RATIONALE);

            final List<Permission> neverAskAgain = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.NEVER_ASK_AGAIN)
            );
            Observable.just(new PermissionsResult(REQUEST_CODE, neverAskAgain))
                    .map(Transforms.TO_FIRST_GRANT_RESULT)
                    .test()
                    .assertNoErrors()
                    .assertValue(GrantResult.NEVER_ASK_AGAIN);
        }

        // multiple permission
        {
            final List<Permission> permissions = Arrays.asList(
                    new Permission(PERMISSIONS[0], GrantResult.GRANTED),
                    new Permission(PERMISSIONS[1], GrantResult.SHOULD_SHOW_RATIONALE),
                    new Permission(PERMISSIONS[2], GrantResult.NEVER_ASK_AGAIN)
            );

            Observable.just(new PermissionsResult(REQUEST_CODE, permissions))
                    .map(Transforms.TO_FIRST_GRANT_RESULT)
                    .test()
                    .assertValue(GrantResult.GRANTED);
        }
    }
}