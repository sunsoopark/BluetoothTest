package com.example.sunsoo.bluetoothtest.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.sunsoo.bluetoothtest.ConnectionManager;
import com.example.sunsoo.bluetoothtest.InterfaceClient;
import com.example.sunsoo.bluetoothtest.InterfaceServer;
import com.example.sunsoo.bluetoothtest.R;

import java.util.UUID;

/**
 * Created by sunsoo on 2015-05-12.
 */
public class BluetoothClassic_Fragment extends BaseFragment implements InterfaceClient, InterfaceServer {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        showMSG("Classic mode");
        setVERSION();
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        RegistRCV();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        btn_cancel = (Button) viewGroup.findViewById(R.id.btn_stop_find);
        btn_find = (Button) viewGroup.findViewById(R.id.btn_start_find);
        btn_discover = (Button) viewGroup.findViewById(R.id.btn_discover);
        btn_server = (Button) viewGroup.findViewById(R.id.btn_server);
        mPairedListView = (ListView) viewGroup.findViewById(R.id.pair_listView);
        mListView = (ListView) viewGroup.findViewById(R.id.listView);

        mPairedAdapter = new MyAdapter(mContext, R.layout.listview_item);
        mAdapter = new MyAdapter(mContext, R.layout.listview_item);
        mPairedListView.setAdapter(mPairedAdapter);
        mListView.setAdapter(mAdapter);
        initListener();
        checkBT();
        initPairedDevices();
        return viewGroup;
    }

    public void RegistRCV() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        BTRCV = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i("test", "found devices : " + action);
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    showMSG("found devices : " + device.getName().toString());
                    Log.i("test", "found devices : " + device.getName().toString() + "/type>>" + (VERSION > 17 ? device.getType() : ""));
                    if (mAdapter == null) {
                        mAdapter = new MyAdapter(mContext, R.layout.listview_item);
                    }
                    UIupdateHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.add(device);
                            mListView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };
        mContext.registerReceiver(BTRCV, filter);
    }


    @Override
    public void findDevice() {
        showMSG("start find");
        mBTAdapter.startDiscovery();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showMSG("30sec passed");
                stopFindDevice();
            }
        }, 1000 * 30);

    }

    @Override
    public void stopFindDevice() {
        showMSG("stop find Devices!");
        mBTAdapter.cancelDiscovery();
    }

    @Override
    public void stopFind() {
        stopFindDevice();
    }

    @Override
    public boolean isWebviewVisible() {
        return false;
    }

    @Override
    public void setWebviewInVisible() {
        //do nothing
    }

    public void makeDiscoverable() {
        showMSG("can find me");
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);//second
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("test", "req Code : " + requestCode + "/ res Code : " + resultCode);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                btn_find.setEnabled(true);
                btn_cancel.setEnabled(true);
                showMSG("enabled");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showMSG("request cancled");
                btn_cancel.setEnabled(false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (BTRCV != null) {
            mContext.unregisterReceiver(BTRCV);
        }
        super.onDestroy();
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyAdapter adapter = null;
            if (parent.equals(mListView)) {
                adapter = (MyAdapter) mListView.getAdapter();
            } else {
                adapter = (MyAdapter) mPairedListView.getAdapter();
            }
            final BluetoothDevice item = (BluetoothDevice) adapter.getItem(position);
            Log.v("test", "bound state ::" + mBTAdapter.getRemoteDevice(item.getAddress()).getBondState());
            new AlertDialog.Builder(mContext)
                    .setMessage("choose running mode").setTitle("choose mode ").setPositiveButton("client", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(item.getAddress());
                    if (device == null) {
                        Log.e("test", "device null");
                        return;
                    }
                    ConnectionManager.startClient(device, mBTAdapter, MY_SVC_UUID, clientCallback);
                }
            })
                    .show();
            showMSG("position : " + position + "/" + item.getName() + "/uuid:" + UUID.randomUUID());

        }
    };

    void initListener() {
        btn_find.setOnClickListener(mClickListener);
        btn_cancel.setOnClickListener(mClickListener);
        btn_server.setOnClickListener(mClickListener);
        btn_discover.setOnClickListener(mClickListener);
        mPairedListView.setOnItemClickListener(onItemClickListener);
        mListView.setOnItemClickListener(onItemClickListener);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.equals(btn_find)) {
                findDevice();
            } else if (v.equals(btn_cancel)) {
                stopFindDevice();
            } else if (v.equals(btn_server)) {
                startServer();
            } else if (v.equals(btn_discover)) {
                makeDiscoverable();
            }
        }
    };

    @Override
    public void startServer() {
        btn_server.setText("stop server");
        showMSG("start server");
        showProgress(false, "listening..");
        ConnectionManager.startServer(mBTAdapter, MY_SVC_UUID, serverCallback);
    }

    @Override
    public void stopServer() {
        ConnectionManager.closeConnection();
        showProgress(false, null);
        showMSG("stop server");
        btn_server.setText("start server");
    }
}
