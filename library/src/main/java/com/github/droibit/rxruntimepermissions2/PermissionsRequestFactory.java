package com.github.droibit.rxruntimepermissions2;

import com.github.droibit.rxruntimepermissions2.RxRuntimePermissions.PermissionsRequest;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static io.reactivex.internal.functions.ObjectHelper.requireNonNull;

@RestrictTo(LIBRARY)
final class PermissionsRequestFactory {

    @NonNull
    static PermissionsRequest create(final Activity activity) {
        requireNonNull(activity, "activity must not be null.");
        return new PermissionsRequest() {
            @Override
            public void accept(@NonNull String[] permissions, @NonNull Integer requestCode) throws Exception {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }

            @Override
            public Boolean apply(@NonNull String permission) throws Exception {
                return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            }
        };
    }

    @NonNull
    static PermissionsRequest create(final Fragment fragment) {
        requireNonNull(fragment, "fragment must not be null.");
        return new PermissionsRequest() {
            @Override
            public void accept(@NonNull String[] permissions, @NonNull Integer requestCode) throws Exception {
                fragment.requestPermissions(permissions, requestCode);
            }

            @Override
            public Boolean apply(@NonNull String permission) throws Exception {
                return fragment.shouldShowRequestPermissionRationale(permission);
            }
        };
    }
}
