package com.example.sunsoo.bluetoothtest.view;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.sunsoo.bluetoothtest.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sunsoo on 2015-05-12.
 */


public class MyAdapter extends ArrayAdapter {
    public class viewHolder {
        String deviceName;
        String addr;
        int state;
        public BluetoothClass classType;
        public TextView text_type;
        public TextView text_name;

    }

    ArrayList<Object> list = new ArrayList<>();
    private Context mContext = null;

    public MyAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    @Override
    public void notifyDataSetInvalidated() {
        list.clear();

        super.notifyDataSetInvalidated();
    }

    @Override
    public void add(Object object) {
        if (!list.contains(object)) {
            list.add(object);
        }
    }

    @Override
    public void addAll(Collection collection) {
        list.addAll(collection);
    }

    @Override
    public int getCount() {
        if (list == null) {
            return -1;
        }
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder = null;
        BluetoothDevice devices = null;
        Object item = list.get(position);
        if (convertView == null) {
            convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.listview_item, null);
            holder = new viewHolder();
            holder.text_type = (TextView) convertView.findViewById(R.id.device_type);
            holder.text_name = (TextView) convertView.findViewById(R.id.device_name);
        } else {
            holder = (viewHolder) convertView.getTag();
        }
        if (item instanceof BluetoothDevice) {
            devices = (BluetoothDevice) item;
            holder.deviceName = devices.getName();
            holder.state = devices.getBondState();
            holder.addr = devices.getAddress();
            holder.classType = devices.getBluetoothClass();
            String name = devices.getName();
            if (devices.getUuids() != null) {
                name += " [" + devices.getUuids().toString() + "] ";
            }
            holder.text_type.setText(name);
            holder.text_name.setText( "[" + devices.getAddress() + "]");
        } else {
            String name = "";
            BluetoothGattService service = (BluetoothGattService) item;
            if (service.getUuid().toString().equals("137f1331-200a-11e0-ac64-0800200c9a66")) {
                name += " [MY SVC] ";
            }
            name += service.getUuid().toString();
            holder.text_type.setText(name);
            StringBuilder builder = new StringBuilder();
            for (BluetoothGattCharacteristic ch : service.getCharacteristics()) {
                builder.append("uuid ").append(ch.getUuid()).append("\n");
            }
            holder.text_name.setText("character >>" + builder);

        }


        convertView.setTag(holder);
        return convertView;
    }

}