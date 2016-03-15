package com.example.sunsoo.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sunsoo on 2015-05-11.
 */
public class ConnectionManager {
    public static final int CONNECTFAIL = -1;
    public static final int CONNECTED = 0;
    public static final int DISCONNCECTED = 1;
    public static final int MSG_READ = 2;

    public static Thread runningTread;

    public static void startServer( BluetoothAdapter adapter, UUID uuid, Handler callback) {
        runningTread = new AcceptThread( adapter, uuid, callback);
        runningTread.start();
    }

    public static void startClient(BluetoothDevice device, BluetoothAdapter adapter, UUID uuid, Handler callback) {
        runningTread = new ConnectThread(device, adapter, uuid, callback);
        runningTread.start();
    }

    public static void closeConnection() {
        if (runningTread != null && runningTread.isAlive()) {
            runningTread.interrupt();

        }
    }

    private static class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;
        private final Handler mCallback;

        private CopyOnWriteArrayList<ManageThread> threadList = new CopyOnWriteArrayList();

        public AcceptThread(BluetoothAdapter btAdapter, UUID uuid, Handler callback) {
            BluetoothServerSocket tmp = null;
            this.mCallback = callback;
            try {
                tmp = btAdapter.listenUsingRfcommWithServiceRecord("testService", uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            Log.e("test", "server interrupted");
            cancel();
            if (threadList.size() > 0) {
                for (Thread thread : threadList) {
                    thread.interrupt();
                }
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                if (socket != null) {
                    Message msg = new Message();

//                    mCallback.sendEmptyMessage(CONNECTED);
                    ManageThread thread = new ManageThread(socket, mCallback);
                    msg.what = CONNECTED;
                    msg.obj = thread;
                    mCallback.sendMessage(msg);
                    threadList.add(thread);
                    thread.run();

                    try {
                        mServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }

            }

        }

        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private final BluetoothAdapter mBTAdapter;
        private Handler mCallback;
        private ManageThread mManageThread;

        public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, UUID uuid, Handler callback) {
            BluetoothSocket tmp = null;
            this.mDevice = device;
            this.mBTAdapter = adapter;
            this.mCallback = callback;
            try {
                if (adapter.getBondedDevices().contains(device)) {//페어링된 장치, 확인 메세지 없이 연결시도
                    tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
                } else {

                    tmp = device.createRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = tmp;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            Log.e("test", "client interrupted");
            cancel();
        }

        @Override
        public void run() {
            mBTAdapter.cancelDiscovery();//디바이스에 연결하면 탐색 취소
            try {
                mSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
            if (mSocket.isConnected()) {
                BluetoothDevice device = mSocket.getRemoteDevice();
//                mCallback.sendEmptyMessage(CONNECTED);
                Log.d("test", "connected:::" + device.getName());
                mManageThread = new ManageThread(mSocket, mCallback);
                mManageThread.run();
            } else {
                Log.d("test", "not connected");
            }

        }

        public void sendMsg(String msg) {
            mManageThread.write(msg);

        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ManageThread extends Thread {
        private BluetoothSocket mSocket;
        private Handler mCallback;
        private OutputStream mOutputStream;

        public ManageThread(BluetoothSocket socket, Handler callback) {
            this.mSocket = socket;
            this.mCallback = callback;
        }

        @Override
        public void interrupt() {
            super.interrupt();

        }

        @Override
        public void run() {
            super.run();
            InputStream is = null;
            while (mSocket.isConnected()) {
                byte[] buffer = new byte[1024];
                int leng = -1;

                try {
                    is = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();
                    leng = is.read(buffer);
                    mCallback.obtainMessage(MSG_READ, leng, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (mOutputStream != null) {
                            mOutputStream.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void write(String msg) {
            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void sendmsg(String msg) {
        if (runningTread != null && runningTread instanceof ConnectThread) {
            ((ConnectThread) runningTread).sendMsg(msg);
        }
    }

}
