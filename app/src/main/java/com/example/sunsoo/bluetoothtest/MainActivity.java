package com.example.sunsoo.bluetoothtest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.sunsoo.bluetoothtest.view.BaseFragment;
import com.example.sunsoo.bluetoothtest.view.BluetoothClassic_Fragment;
import com.example.sunsoo.bluetoothtest.view.BluetoothLE_SCANNER_Fragment;
import com.example.sunsoo.bluetoothtest.view.BluetoothLE_SENSOR_Fragment;

import java.lang.Override;

public class MainActivity extends FragmentActivity {
    LinearLayout ll_button, ll_container;

    Button btn_classic;
    Button btn_sensor;
    Button btn_monitor;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PackageManager pm = getPackageManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        ll_button = (LinearLayout) findViewById(R.id.ll_button);
        ll_container = (LinearLayout) findViewById(R.id.ll_container);
        btn_classic = (Button) findViewById(R.id.btn_classic);
        btn_sensor = (Button) findViewById(R.id.btn_sensor);
        btn_monitor = (Button) findViewById(R.id.btn_mother);
        btn_classic.setOnClickListener(mClickListener);
        btn_sensor.setOnClickListener(mClickListener);
        btn_monitor.setOnClickListener(mClickListener);

        btn_sensor.setEnabled(false);
        btn_monitor.setEnabled(false);
        if (Build.VERSION.SDK_INT >= 18) {
            if (pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Log.i("test", "support btle");
                if (Build.VERSION.SDK_INT > 20) {
                    btn_sensor.setEnabled(true);
                }
                btn_monitor.setEnabled(true);
            }
        }

    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(btn_classic)) {
                chooseMode(0);
            } else if (v.equals(btn_sensor)) {
                chooseMode(1);
            } else if (v.equals(btn_monitor)) {
                chooseMode(2);
            }
        }
    };

    private int mode = -1;

    public int getMode() {
        return mode;
    }

    private Fragment CurrentFragment = null;

    private void chooseMode(int index) {
        if (ll_button.getVisibility() == View.VISIBLE) {
            ll_button.setVisibility(View.GONE);
        }
        ll_container.setVisibility(View.VISIBLE);

        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        mode = index;
        if (index == 0) {
            CurrentFragment = new BluetoothClassic_Fragment();
        } else if (index == 1) {
            CurrentFragment = new BluetoothLE_SENSOR_Fragment();
        } else if (index == 2) {
            CurrentFragment = new BluetoothLE_SCANNER_Fragment();
        }
        if (count > 0) {
            fm.beginTransaction().replace(R.id.ll_container, CurrentFragment).commit();
        } else {
            fm.beginTransaction().add(R.id.ll_container, CurrentFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        showMSG("back pressed");

        boolean isWebviewVisible = false;
        if(CurrentFragment != null){
            if(((BaseFragment)CurrentFragment).isWebviewVisible()){
                isWebviewVisible = true;
            }
        }

        if (findViewById(R.id.ll_container).getVisibility() == View.INVISIBLE) {
            super.onBackPressed();
        } else if (isWebviewVisible) {
            ((BaseFragment)CurrentFragment).setWebviewInVisible();
        } else {
            ll_button.setVisibility(View.VISIBLE);
            ll_container.setVisibility(View.INVISIBLE);
            ll_container.removeAllViews();
        }
    }

    Toast mMsgToast;

    public void showMSG(final String msg) {
        if (mMsgToast == null) {
            mMsgToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        } else {
            mMsgToast.setText(msg);
        }
        mMsgToast.show();

    }
}
