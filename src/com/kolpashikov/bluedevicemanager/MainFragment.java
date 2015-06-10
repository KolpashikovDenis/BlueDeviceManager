package com.kolpashikov.bluedevicemanager;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment {
	final String LOG = "mLogs";
	final String LIFECYCLE = "LifeCycle";
	
	BluetoothAdapter bluetoothAdapter;
	BluetoothDeviceAdapter btAdapter;
	ArrayList<BluetoothDevice> devices;
	DetailFragment frag;
	FragmentTransaction ft;
	
	IntentFilter intentFilterStart;
	IntentFilter intentFilterStop;
	IntentFilter intentFilterFound;	
	
	ListView lvDeviceList;
	ProgressBar pbDiscovering;
	int endBondedItem = 0;
	
	OnItemClickNotifier clickNotifier;
//--------------------------------------------------------------------------------------
	public interface OnItemClickNotifier {
		public void onClickItemEvent(String sAddr, String sName);
	}
//--------------------------------------------------------------------------------------	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try{
			clickNotifier = (OnItemClickNotifier)activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement OnItemCLickNotiifier");
		}
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		devices = new ArrayList<BluetoothDevice>();
		Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
		for(BluetoothDevice device: bondedDevices){
			devices.add(device);
		}
		
		Log.d(LIFECYCLE, "MainFragment.onAttach");
		endBondedItem = devices.size();		
	}
	
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){		
		View v = inflater.inflate(R.layout.main_fragment, null);
		lvDeviceList = (ListView)v.findViewById(R.id.mainDeviceList);
		btAdapter = new BluetoothDeviceAdapter(getActivity(), devices);
		lvDeviceList.setAdapter(btAdapter);
		lvDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String sAddr = ((TextView)view.findViewById(R.id.tvAddress)).getText().toString();
				String sName = ((TextView)view.findViewById(R.id.tvName)).getText().toString();
				// Посылаем сообщение в активность
				clickNotifier.onClickItemEvent(sAddr, sName);
				
			}// public void onItemClick( .......
			
		});
		
		pbDiscovering = (ProgressBar)v.findViewById(R.id.pbDiscovering);
		
		Log.d(LIFECYCLE, "MainFragment.onCreateView");
		return v;		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
	}
	
	public void onStart(){
		super.onStart();
		
		intentFilterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilterStop  = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		getActivity().registerReceiver(mReceiverBluetoothAction, intentFilterStart);
		getActivity().registerReceiver(mReceiverBluetoothAction, intentFilterStop);
		getActivity().registerReceiver(mReceiverBluetoothAction, intentFilterFound);
		Log.d(LIFECYCLE, "MainFragment.onStart");
	}
	
	public void onResume(){
		super.onResume();
		Log.d(LIFECYCLE, "MainFragment.onResume");
	}
	
	public void onPause(){
		super.onPause();
		Log.d(LIFECYCLE, "MainFragment.onPause");
	}
	
	public void onStop(){
		super.onStop();
		getActivity().unregisterReceiver(mReceiverBluetoothAction);
		Log.d(LIFECYCLE, "MainFragment.onStop");
	}
	
	public void startDiscovering(){
		Log.d(LOG, "MainFragment: starting discovery");
		if(endBondedItem != 0){
			for(int i = endBondedItem; i < devices.size(); i++){
				devices.remove(i);
				btAdapter.notifyDataSetChanged();				
			}
			endBondedItem = 0;
			Log.d(LOG, "MainFragment: startDiscovering()");
		} //if(endBondedItem != 0){...
		
		if(bluetoothAdapter.isDiscovering()){
			bluetoothAdapter.cancelDiscovery();
		}
		bluetoothAdapter.startDiscovery();
	}
	
//-------------------------------------------------------------------------------
	BroadcastReceiver mReceiverBluetoothAction = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
				Toast.makeText(getActivity(), R.string.bt_start_discovering, Toast.LENGTH_SHORT).show();
				Log.d(LOG, "MainFragment: scanning start...");
				pbDiscovering.setVisibility(View.VISIBLE);
			} else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
				Toast.makeText(getActivity(), R.string.bt_stop_discovering, Toast.LENGTH_SHORT).show();
				Log.d(LOG, "Scanning stopped.");
				pbDiscovering.setVisibility(View.INVISIBLE);
			} else if(action.equals(BluetoothDevice.ACTION_FOUND)){
				BluetoothDevice tmpDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(!devices.contains(tmpDevice)){
					devices.add(tmpDevice);
					btAdapter.notifyDataSetChanged();
				}
				Log.d(LOG, "MainFragment: device found - " + tmpDevice.getName() + 
						" " + tmpDevice.getAddress());
			}
		}
		
	};

}
