package com.example.sunsoo.bluetoothtest.view;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sunsoo.bluetoothtest.R;

import java.lang.Override;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by sunsoo on 2015-05-12.
 */
public class BluetoothLE_SENSOR_Fragment extends BaseFragment {
    private BluetoothManager mBTManager;
    private BluetoothLeAdvertiser leAdvertiser;
    private int GATT_Action = -1;

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
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_main_sensor, container, false);
        btn_cancel = (Button) viewGroup.findViewById(R.id.btn_stop_find);
        btn_find = (Button) viewGroup.findViewById(R.id.btn_start_find);
        btn_discover = (Button) viewGroup.findViewById(R.id.btn_discover);
        btn_server = (Button) viewGroup.findViewById(R.id.btn_server);
        viewGroup.findViewById(R.id.btn_discover).setVisibility(View.GONE);
        btn_find.setText("action_naver");
        btn_cancel.setText("action_daum");
        btn_server.setText("action_google");
        initListener();
        checkBT();
        return viewGroup;
    }


    @Override
    public void stopFind() {
        stopAdvertising();
//        mGattServer.close();
    }

    @Override
    public boolean isWebviewVisible() {
        return false;
    }

    @Override
    public void setWebviewInVisible() {
        //do nothing
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertising() {
        if (VERSION >= Build.VERSION_CODES.LOLLIPOP) {
            if (mGattServer != null) {
                mGattServer.close();
                mGattServer = null;
            }
            leAdvertiser = mBTAdapter.getBluetoothLeAdvertiser();
            showMSG("start advertise");
            AdvertiseSettings setting = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setTimeout(20000)
                    .setConnectable(true).build();
//                ByteBuffer mManufacturerData = ByteBuffer.allocate(22);
//                byte[] uuid = getIdAsByte(UUID.fromString("137f1331-200a-11e0-ac64-0800200c9a66"));
//                mManufacturerData.put(0, (byte) 0xBE); // Beacon Identifier
//                mManufacturerData.put(1, (byte) 0xAC); // Beacon Identifier
//                for (int i = 2; i <= 17; i++) {
//                    mManufacturerData.put(i, uuid[i - 2]); // adding the UUID
//                }
//                mManufacturerData.put(18, (byte) 0x00); // first byte of Major
//                mManufacturerData.put(19, (byte) 0x09); // second byte of Major
//                mManufacturerData.put(20, (byte) 0x00); // first minor
//                mManufacturerData.put(21, (byte) 0x06); // second minor

            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addServiceUuid(ParcelUuid.fromString(MY_SVC_UUID.toString()))
//                    .addServiceData(ParcelUuid.fromString("137f1331-200a-11e0-ac64-0800200c9a66"), "1".getBytes())
                    .build();

            leAdvertiser.startAdvertising(setting, data, mAdvertiseCallback);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showProgress(false, null);
                }
            }, 20000);
            runGattServer();
            showProgress(true, "advertising....");
        } else {
            showMSG("advertising is not avilable");
        }

    }

    private void stopAdvertising() {
        if (VERSION >= Build.VERSION_CODES.LOLLIPOP) {
            showMSG("stop advertise");
            showProgress(false, null);
            if (leAdvertiser != null) {
                leAdvertiser.stopAdvertising(mAdvertiseCallback);
            }

        } else {
            showMSG("advertising is not avilable");
        }
    }


    public byte[] getIdAsByte(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private String getAdverTiseErrName(int errCode) {
        switch (errCode) {
            case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                return "Failed to start advertising as the advertising is already started. ";
            case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                return "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes. ";
            case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                return "This feature is not supported on this platform. ";
            case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                return "Operation failed due to an internal error. ";
            case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                return "Failed to start advertising because no advertising instance is available. ";

        }
        return "etc";
    }


    void initListener() {
        btn_find.setOnClickListener(mClickListener);
        btn_cancel.setOnClickListener(mClickListener);
        btn_server.setOnClickListener(mClickListener);
        btn_discover.setOnClickListener(mClickListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startAdvertising();
        mGattServer.close();
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(btn_find)) {
                GATT_Action = 1;
            } else if (v.equals(btn_cancel)) {
                GATT_Action = 2;
            } else if (v.equals(btn_server)) {
                GATT_Action = 0;
            } else if (v.equals(btn_discover)) {
//                makeDiscoverable();
            }
            startAdvertising();
        }
    };

    private AdvertiseCallback mAdvertiseCallback = null;

    {
        if (Build.VERSION.SDK_INT > 20) {
            mAdvertiseCallback = new AdvertiseCallback() {

                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    showMSG("advertise start success >>" + settingsInEffect.describeContents());
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Log.e("test", "fail:::" + getAdverTiseErrName(errorCode));
                    showMSG("fail:::" + getAdverTiseErrName(errorCode));
                    showProgress(false, null);
                }
            };
        }
    }

    private BluetoothGattServer mGattServer;

    private void runGattServer() {
        mGattServer = mBTManager.openGattServer(mContext, new BluetoothGattServerCallback() {

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            }

            @Override
            public void onConnectionStateChange(final BluetoothDevice device, int status, final int newState) {
                super.onConnectionStateChange(device, status, newState);
                UIupdateHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (newState == BluetoothGatt.STATE_CONNECTED) {
                            Log.v("test", "server ::: " + device.getAddress() + ">>STATE_CONNECTED");
                            stopAdvertising();
                        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                            Log.v("test", "server ::: " + device.getName() + ">>STATE_DISCONNECTED");
                            mGattServer.clearServices();
                            mGattServer.close();
                        }
                    }
                });
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
                String result = "";
                if (BluetoothGatt.GATT_SUCCESS == status) {
                    result = "success";
                } else {
                    result = "fail";
                }
                Log.v("test", "server service added ::: " + result + service.getUuid());
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                Log.v("test", "server onCharacteristicReadRequest ::: ");
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                super.onNotificationSent(device, status);
            }
        });
        setService();
    }

    private void setService() {
        if (GATT_Action == -1) return;
        UUID serviceUUID = null;
        switch (GATT_Action) {
            case 0:
                serviceUUID = MY_UUID_SVC0;
                break;
            case 1:
                serviceUUID = MY_UUID_SVC1;
                break;
            case 2:
                serviceUUID = MY_UUID_SVC2;
                break;

        }

        BluetoothGattService service = new BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(serviceUUID,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_BROADCAST);

        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(serviceUUID,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        descriptor.setValue("service".getBytes());
        characteristic.setValue("hello world");
        characteristic.addDescriptor(descriptor);
        service.addCharacteristic(characteristic);
        mGattServer.addService(service);
    }
}
