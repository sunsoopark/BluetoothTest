package com.example.sunsoo.bluetoothtest.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sunsoo.bluetoothtest.InterfaceClient;
import com.example.sunsoo.bluetoothtest.R;
import com.example.sunsoo.bluetoothtest.util.ScanRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sunsoo on 2015-05-12.
 */
public class BluetoothLE_SCANNER_Fragment extends BaseFragment implements InterfaceClient {
    private BluetoothManager mBTManager;
    private WebView mWebView;
    private LinearLayout ll_contents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        showMSG("BTLE mode");
        setVERSION();
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        mBTManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = mBTManager.getAdapter();
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
        viewGroup.findViewById(R.id.btn_server).setVisibility(View.GONE);
        viewGroup.findViewById(R.id.btn_discover).setVisibility(View.GONE);
        ll_contents = (LinearLayout) viewGroup.findViewById(R.id.ll_content);
        mWebView = (WebView) viewGroup.findViewById(R.id.webview);
        mWebView.setVisibility(View.GONE);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        ((TextView) viewGroup.findViewById(R.id.txt_first)).setText("Device");
        ((TextView) viewGroup.findViewById(R.id.txt_second)).setText("Service");

        initListener();
        checkBT();
        return viewGroup;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void findDevice() {
        showProgress(true, "find device");
        mAdapter.notifyDataSetInvalidated();
        mPairedAdapter.notifyDataSetInvalidated();
        removeConnection(null);
        if (this.VERSION > 20) {
            BluetoothLeScanner scanner = mBTAdapter.getBluetoothLeScanner();

            ScanSettings.Builder settings = new ScanSettings.Builder();
            settings.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
            settings.setReportDelay(10000);

            scanner.startScan(buildScanFilters(), settings.build(), mScanCallback);
        } else {
            UUID[] list = new UUID[]{MY_SVC_UUID};
//            mBTAdapter.startLeScan(list, mLeScanCallback);
            mBTAdapter.startLeScan(mLeScanCallback);

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgress(false, null);
            }
        }, 30000);
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(MY_SVC_UUID.toString())).build());
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(MY_UUID_SVC0.toString())).build());
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(MY_UUID_SVC1.toString())).build());
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(MY_UUID_SVC2.toString())).build());
        return filters;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void stopFindDevice() {
        showMSG("stop scan!!");
        showProgress(false, null);
        if (this.VERSION > 20) {
            BluetoothLeScanner scanner = mBTAdapter.getBluetoothLeScanner();
            scanner.stopScan(mScanCallback);
        } else {
            mBTAdapter.stopLeScan(mLeScanCallback);
        }
    }


    @Override
    public void stopFind() {
        stopFindDevice();
    }

    @Override
    public boolean isWebviewVisible() {
        if (mWebView == null || mWebView != null && (mWebView.getVisibility() != View.VISIBLE)) {
            return false;
        }
        return true;
    }

    @Override
    public void setWebviewInVisible() {
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
            ll_contents.setVisibility(View.VISIBLE);
        }
    }

    CopyOnWriteArrayList<BluetoothGatt> mConnectedDevice = new CopyOnWriteArrayList<>();
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyAdapter adapter = (MyAdapter) mPairedListView.getAdapter();
            mAdapter.notifyDataSetInvalidated();
            stopFindDevice();
            final BluetoothDevice item = (BluetoothDevice) adapter.getItem(position);
            new AlertDialog.Builder(mContext)
                    .setTitle("conncect to server ").setPositiveButton("connect", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(item.getAddress());
                    if (device == null) {
                        Log.e("test", "device null");
                        return;
                    }
                    BluetoothGatt gatt = device.connectGatt(mContext, true, new BluetoothGattCallback() {

                                @Override
                                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                                    super.onCharacteristicChanged(gatt, characteristic);
                                    Log.i("test", "device:" + gatt.getDevice().getName() + "onCharacteristicChanged");
                                }

                                @Override
                                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                            super.onConnectionStateChange(gatt, status, newState);
                                    String msg = "";
                                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                                        msg = "Conncected";
                                        gatt.discoverServices();
                                    } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                                        msg = "Disconnected";
                                        removeConnection(gatt.getDevice());
                                    }
                                    Log.i("test", "device:" + gatt.getDevice().getName() + " is " + msg);
                                }

                                @Override
                                public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
//                            super.onServicesDiscovered(gatt, status);
                                    if (status == BluetoothGatt.GATT_SUCCESS) {
                                        Log.i("test", "onServicesDiscovered");
                                        UIupdateHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mAdapter.addAll(gatt.getServices());
                                                mAdapter.notifyDataSetChanged();

                                            }
                                        });

                                        List<BluetoothGattService> serviceList = gatt.getServices();
                                        for (BluetoothGattService service : serviceList) {
                                            Log.i("char", "service type>>" + service.getType());
                                            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
                                            for (BluetoothGattCharacteristic characteristic : characteristicList) {
                                                Log.i("char", "proper>>" + characteristic.getValue());

                                            }
                                        }
                                    } else {
                                        Log.i("test", "onServicesDiscovered fail");
                                    }
                                }

                                @Override
                                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                                    super.onCharacteristicRead(gatt, characteristic, status);
                                    if (status == BluetoothGatt.GATT_SUCCESS) {
                                        Log.i("test", "onCharacteristicRead");

                                    }
                                }

                            }
                    );
                    mConnectedDevice.add(gatt);
                }
            })
                    .show();

        }
    };
    AdapterView.OnItemClickListener onSVCItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyAdapter adapter = (MyAdapter) parent.getAdapter();
            final BluetoothGattService item = (BluetoothGattService) adapter.getItem(position);
            String uuid = item.getUuid().toString();
            mWebView.setVisibility(View.VISIBLE);
            ll_contents.setVisibility(View.GONE);

            if (uuid.equals(MY_UUID_SVC0.toString())) {
                mWebView.loadUrl("http://www.google.com");
            } else if (uuid.equals(MY_UUID_SVC1.toString())) {
                mWebView.loadUrl("http://www.naver.com");
            } else if (uuid.equals(MY_UUID_SVC2.toString())) {
                mWebView.loadUrl("http://www.daum.com");
            }
        }
    };


    void initListener() {
        btn_find.setOnClickListener(mClickListener);
        btn_cancel.setOnClickListener(mClickListener);
        btn_server.setOnClickListener(mClickListener);
        btn_discover.setOnClickListener(mClickListener);
        mPairedListView.setOnItemClickListener(onItemClickListener);
        mListView.setOnItemClickListener(onSVCItemClickListener);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(btn_find)) {
                findDevice();
            } else if (v.equals(btn_cancel)) {
                stopFindDevice();
            }
        }
    };
    private LeScanCallback mLeScanCallback = null;

    {
        if (Build.VERSION.SDK_INT > 17) {
            mLeScanCallback = new LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    UIupdateHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            ScanRecord record = ScanRecord.parseFromBytes(scanRecord);
                            List<ParcelUuid> svcList = record.getServiceUuids();
                            Map data = record.getServiceData();
                            if (svcList != null) {
                                for (ParcelUuid uuid : svcList) {
                                    Log.i("test", "svc uuid::" + uuid.getUuid());

                                    if (data != null)
                                        Log.i("test", "data ::" + data.get(uuid.getUuid()));
                                }

                            }

                            Log.v("test", "getBLE >> " + device.getName() + " / rssi:" + rssi);
                            showMSG("getBLE>>" + device.getName() + "/rssi:" + rssi + "/rec");
                            mPairedAdapter.add(device);
                            mPairedAdapter.notifyDataSetChanged();
                            if (device.getName().equals("Soo")) {
                                showMSG("find divice [soo]");
                                stopFindDevice();
                            }

                        }
                    });
                }
            };
        }
    }

    private ScanCallback mScanCallback = null;

    {
        if (Build.VERSION.SDK_INT > 20) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanFailed(int errorCode) {
                    showMSG("scan fail >>" + errorCode);
                    showProgress(false, null);
                }

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    UIupdateHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            BluetoothDevice device = result.getDevice();
                            showMSG("getBLE[lolipop]>>" + device.getName() + "/rssi::" + result.getRssi() + "/rec" + result.getScanRecord().getManufacturerSpecificData(224));
                            Log.v("test", "uuid::" + device.getUuids());
                            mPairedAdapter.add(device);
                            mPairedAdapter.notifyDataSetChanged();


                        }
                    });
                }
            };
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFindDevice();
        removeConnection(null);
    }

    private void removeConnection(BluetoothDevice device) {
        if (mConnectedDevice == null) return;
        for (BluetoothGatt gatt : mConnectedDevice) {
            if (device != null) {
                if (gatt.getDevice().getUuids().toString().equals(device.getUuids().toString())) {
                    gatt.disconnect();
                    gatt.close();
                }
            } else {
                gatt.disconnect();
                gatt.close();

            }
        }
    }

}
