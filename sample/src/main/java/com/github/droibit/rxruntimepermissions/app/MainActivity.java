package com.github.droibit.rxruntimepermissions.app;

import com.github.droibit.rxruntimepermissions.GrantResult;
import com.github.droibit.rxruntimepermissions.PendingRequestPermissionsAction;
import com.github.droibit.rxruntimepermissions.PermissionsResult;
import com.github.droibit.rxruntimepermissions.RxRuntimePermissions;
import com.github.droibit.rxruntimepermissions.Transforms;
import com.jakewharton.rxbinding.view.RxView;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import rx.Observable;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_CALL = 2;

    private final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions();

    private final PendingRequestPermissionsAction pendingRequestPermissionsAction = new PendingRequestPermissionsAction(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Observable<Void> trigger = RxView.clicks(fab);
        rxRuntimePermissions.from(this)
                .on(trigger)
                .requestPermissions(REQUEST_CAMERA, Manifest.permission.CAMERA)
                .map(Transforms.areGranted())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        final String msg = granted ? "Granted" : "Denied";
                        Toast.makeText(MainActivity.this, "Camera Permission: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });

        rxRuntimePermissions.from(pendingRequestPermissionsAction)
                .requestPermissions(REQUEST_CALL, Manifest.permission.CALL_PHONE)
                .map(Transforms.toSingleGrantResult())
                .subscribe(new Action1<GrantResult>() {
                    @Override
                    public void call(GrantResult result) {
                        Toast.makeText(MainActivity.this, "Phone Permission: " + result.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        rxRuntimePermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onClickCall(View v) {
        pendingRequestPermissionsAction.call();
    }
}
