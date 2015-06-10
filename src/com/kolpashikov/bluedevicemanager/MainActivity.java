package com.kolpashikov.bluedevicemanager;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.kolpashikov.bluedevicemanager.MainFragment.OnItemClickNotifier;

public class MainActivity extends Activity implements OnItemClickNotifier{
	static final String LOG = "mLogs";
	static final String LIFECYCLE = "LifeCycle";
	static final String BROADCAST_ACTION = "com.kolpashikov.bluedevicemanager";
	static final String CONNECTION_STATE = "connectionstate";
	
	static final String SADDR = "sAddr";
	static final String SNAME = "sName";
	
	final int CONNECTION_ABORTED = 1;
	final int CONNECTION_ESTABLISHED = 2;
	
	BTSwitchedOff fragBTOff;
	MainFragment fragBTList;
	FragmentTransaction fragTransaction;
	public static DetailFragment fragBTDetail;
	
	public BluetoothAdapter mAdapter;
	
	IntentFilter intentFilterState;
	BroadcastReceiver brStateConnection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fragBTOff = new BTSwitchedOff();
		fragBTList = new MainFragment();
		
		mAdapter = BluetoothAdapter.getDefaultAdapter();	
		fragTransaction = getFragmentManager().beginTransaction();		
		if(mAdapter.isEnabled()){
			fragTransaction.add(R.id.frameLayout, fragBTList);
//			startService(new Intent(this, BTService.class));
		} else {
			fragTransaction.add(R.id.frameLayout, fragBTOff);
		}
		fragTransaction.commit();
		
		brStateConnection = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				int state = intent.getIntExtra(CONNECTION_STATE, 0);
				Log.d(LOG, "brStateConnection.onReceiver: " + state);
			}			
		};
		
		IntentFilter intFltr = new IntentFilter(BROADCAST_ACTION);
		registerReceiver(brStateConnection, intFltr);
		Log.d(LIFECYCLE, "MainActivity.onCreate");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);		
	
		Log.d(LIFECYCLE, "requestCode = "+requestCode+ ", resultCode = "+ resultCode);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch(id){
		case R.id.action_start_discovery:
			fragBTList.startDiscovering();
			Log.d(LOG, "MainActivity: discovering started...");
			//return true;		
			break;
			
		case R.id.action_settings:			
			break;
			
		case R.id.action_help:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Log.d(LIFECYCLE, "MainActivity.onStart");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		intentFilterState = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mBluetoothActionReceiver, intentFilterState);
		Log.d(LIFECYCLE, "MainActivity.onResume");
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		Log.d(LIFECYCLE, "MainActivity.onPause");
		
		unregisterReceiver(mBluetoothActionReceiver);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		Log.d(LIFECYCLE, "MainActivity.onStop");
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
//		stopService(new Intent(this, BTService.class));
		unregisterReceiver(brStateConnection);
		Log.d(LIFECYCLE, "MainActivity.onDestroy");
	}
	
	@Override
	public void onClickItemEvent(String sAddr, String sName){
		
		Intent intent = new Intent(this, DetailActivity.class);
		intent.putExtra(SADDR, sAddr);
		intent.putExtra(SNAME, sName);
		startActivity(intent);
		/*
		fragBTDetail = new DetailFragment(sAddr, sName);
		fragTransaction = getFragmentManager().beginTransaction();
		fragTransaction.replace(R.id.frameLayout, fragBTDetail);
		fragTransaction.addToBackStack(null);
		fragTransaction.commit(); */
	}
//-----------------------------------------------------------------------------------------	
	BroadcastReceiver mBluetoothActionReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				
				fragTransaction = getFragmentManager().beginTransaction();
				switch(state){
				case BluetoothAdapter.STATE_ON:					
					fragTransaction.replace(R.id.frameLayout, fragBTList);	
//					startService(new Intent(MainActivity.this, BTService.class));
					Log.d(LOG, "Receiver: STATE_ON");
					break;
					
				case BluetoothAdapter.STATE_OFF:
					fragTransaction.replace(R.id.frameLayout, fragBTOff);
					Log.d(LOG, "Receiver: STATE_OFF");
//					stopService(new Intent(MainActivity.this, BTService.class));
					break;				
				} // ����� (state)...
				fragTransaction.commit();
			}// if ... ACTION_STATE_CHANGED....
			
		}
		
	};
	
	public interface onEventListener{
		public void fragEvents(Message msg);
	}
}
