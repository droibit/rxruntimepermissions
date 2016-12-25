package com.github.droibit.rxruntimepermissions2.app;

import com.github.droibit.rxruntimepermissions2.PendingRequestPermissionsAction;
import com.github.droibit.rxruntimepermissions2.PermissionsResult.GrantResult;
import com.github.droibit.rxruntimepermissions2.RxRuntimePermissions;
import com.jakewharton.rxbinding.view.RxView;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.functions.Consumer;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;

    private static final int REQUEST_CALL = 2;

    private final RxRuntimePermissions rxRuntimePermissions = new RxRuntimePermissions(this);

    private final PendingRequestPermissionsAction pendingRequestPermissions = new PendingRequestPermissionsAction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        RxJavaInterop.toV2Observable(RxView.clicks(fab).map(new Func1<Void, Object>() {
                    @Override
                    public Object call(Void aVoid) {
                        return new Object();
                    }
                }))
                .compose(rxRuntimePermissions.thenRequest(REQUEST_CAMERA, Manifest.permission.CAMERA))
                .map(RxRuntimePermissions.areGranted())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        final String msg = granted ? "Granted" : "Denied";
                        Toast.makeText(MainActivity.this, "Camera Permission: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });

        pendingRequestPermissions.asObservable()
                .compose(rxRuntimePermissions.thenRequest(REQUEST_CALL, Manifest.permission.CALL_PHONE))
                .map(RxRuntimePermissions.toFirstGrantResult())
                .subscribe(new Consumer<GrantResult>() {
                    @Override
                    public void accept(GrantResult result) throws Exception {
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
        pendingRequestPermissions.call();
    }
}
