package com.example.sunsoo.bluetoothtest.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sunsoo.bluetoothtest.ConnectionManager;
import com.example.sunsoo.bluetoothtest.R;

import java.util.Set;
import java.util.UUID;

/**
 * Created by sunsoo on 2015-05-12.
 */
public abstract class BaseFragment extends Fragment {
    public int VERSION = -1;

    public abstract void stopFind();
    public abstract boolean isWebviewVisible();
    public abstract void setWebviewInVisible();

    private Toast mMsgToast = null;
    public Activity mContext = null;
    public BluetoothAdapter mBTAdapter = null;
    static final int REQUEST_ENABLE_BT = 1001;

    MyAdapter mAdapter;
    MyAdapter mPairedAdapter;
    BroadcastReceiver BTRCV = null;
    ListView mListView;
    ListView mPairedListView;
    Button btn_find;
    Button btn_cancel;
    Button btn_server;
    Button btn_discover;
    static final UUID MY_SVC_UUID =
            UUID.fromString("333ff333-200a-11e0-ac64-0800200c9a66");
    static final UUID MY_UUID_SVC0 =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    static final UUID MY_UUID_SVC1 =
            UUID.fromString("137f1331-200a-11e0-ac64-0800200c9a66");
    static final UUID MY_UUID_SVC2 =
            UUID.fromString("433f1331-200a-11e0-ac64-0800200c9a66");

    public void setVERSION() {
        VERSION = Build.VERSION.SDK_INT;
    }

    public void showMSG(final String msg) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mMsgToast == null) {
                    mMsgToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
                } else {
                    mMsgToast.setText(msg);
                }
                mMsgToast.show();

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void checkBT() {
        if (isBTAvailable()) {
            if (!mBTAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            } else {
                btn_find.setEnabled(true);
                btn_cancel.setEnabled(true);
            }
        }
    }

    public void initPairedDevices() {
        Set<BluetoothDevice> devices = mBTAdapter.getBondedDevices();
        if (mPairedAdapter == null) {
            mPairedAdapter = new MyAdapter(mContext, R.layout.listview_item);
            mPairedListView.setAdapter(mPairedAdapter);
        } else {
            mPairedAdapter.clear();
            mPairedAdapter.notifyDataSetInvalidated();
        }

        mPairedAdapter.addAll(devices);
        if (devices.size() == 0) {
            showMSG("no paired devices");
        }
        for (BluetoothDevice bluetoothDevice : devices) {
            Log.d("test", "PAIRED devices : " + bluetoothDevice.getName().toString());
        }
        mPairedAdapter.notifyDataSetChanged();
    }

    private boolean isBTAvailable() {
        if (mBTAdapter == null) {
            mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (mBTAdapter == null) {
            showMSG("no blueTooth");
            return false;
        }
        return true;
    }

    Handler serverCallback = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionManager.CONNECTFAIL:
                    showMSG("connect fail");
                    break;
                case ConnectionManager.CONNECTED:
                    showMSG("connect success");
                    break;
                case ConnectionManager.DISCONNCECTED:
                    showMSG("disconnected");
                    break;
                case ConnectionManager.MSG_READ:
                    byte[] readBytes = (byte[]) msg.obj;

                    String message = new String(readBytes, 0, msg.arg1);
                    showMSG("RCV msg :: " + message);
                    ConnectionManager.sendmsg("test msg from >>server");
                    break;

            }
        }

    };
    Handler clientCallback = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionManager.CONNECTFAIL:
                    showMSG("connect fail");
                    break;
                case ConnectionManager.CONNECTED:
                    showMSG("connect success");
                    ConnectionManager.sendmsg("test msg from >>client");
                    break;
                case ConnectionManager.DISCONNCECTED:
                    showMSG("disconnected");
                    break;
                default:
                    byte[] readBytes = (byte[]) msg.obj;

                    String message = new String(readBytes, 0, msg.arg1);
                    showMSG("RCV msg :: " + message);
                    break;

            }
        }

    };
    ProgressDialog mProgressDialog = null;
    Handler UIupdateHandler = new Handler();

    public void showProgress(boolean show, String msg) {
        if (show) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext);
            }
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle(msg);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    stopFind();
                }
            });
            mProgressDialog.show();
        } else {
            if (mProgressDialog == null) return;
            mProgressDialog.cancel();
        }


    }

}
