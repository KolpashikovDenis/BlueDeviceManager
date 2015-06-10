package com.kolpashikov.bluedevicemanager;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BluetoothDeviceAdapter extends BaseAdapter {
	
	Context mContext;
	LayoutInflater lInflater;
	ArrayList<BluetoothDevice> mObjects;
	
	public BluetoothDeviceAdapter(Context context, ArrayList <BluetoothDevice> _devices){
		mContext = context;
		mObjects = _devices;
		lInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mObjects.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = convertView;
		if(view == null){
			view = lInflater.inflate(R.layout.list_item, parent, false);
		}
		
		BluetoothDevice bd = getDevice(position);
		((TextView)view.findViewById(R.id.tvName)).setText(bd.getName());
		((TextView)view.findViewById(R.id.tvAddress)).setText(bd.getAddress());
		
		switch(bd.getBondState()){
		case BluetoothDevice.BOND_NONE:
			((TextView)view.findViewById(R.id.tvDevClass)).setText(R.string.bt_not_bonded);
			break;
			
		case BluetoothDevice.BOND_BONDING:
		case BluetoothDevice.BOND_BONDED:
			((TextView)view.findViewById(R.id.tvDevClass)).setText(R.string.bt_bonded);
		}		
		
		return view;
	}
	
	BluetoothDevice getDevice(int position){
		return ((BluetoothDevice)getItem(position));
	}

}

