package com.kolpashikov.bluedevicemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kolpashikov.bluedevicemanager.MainActivity.onEventListener;

public class DetailFragment extends Fragment implements View.OnClickListener {
	final String LOG = "mLogs";
	final String LIFECYCLE = "LifeCycle";
	
	public final int BTN_CLICK_GET_CONTACTS = 0x1;
	public final int BTN_CLICK_GET_HISTORY  = 0x2;
	public final int BTN_CLICK_SCREEN_BLINK = 0x3;
	public final int BTN_CLICK_VIBRATE		= 0x4;
	public final int BTN_CLICK_MIC         = 0x5;
	
	onEventListener eventListener;
	
	final String uuid = "993d463c-d711-11e4-b9d6-1681e6b88ec1";
//	private String sAddress, sName;

	Button btnContacts;
	Button btnVibrate;
	Button btnFindDev;
	Button btnMicrophone;
	InputStream inStream = null;
	OutputStream outStream = null;
	BluetoothSocket btSocket;
	Message msg;
	
	DetailActivity.ConnectThread t;
	
	public DetailFragment(BluetoothSocket socket){
		btSocket = socket;
		try{
//			inStream = btSocket.getInputStream();
			outStream = btSocket.getOutputStream();
		}catch(IOException e){ }
	}
	
	public DetailFragment(){}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try{
			eventListener = (onEventListener)activity;
		}catch(ClassCastException e){ }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.detail_fragment, null);
		btnContacts = (Button)v.findViewById(R.id.btnGetContacts);
		btnContacts.setOnClickListener(this);
		
		btnVibrate = (Button)v.findViewById(R.id.btnVibrate);
		btnVibrate.setOnClickListener(this);
		
		btnFindDev = (Button)v.findViewById(R.id.btnGetHistory);
		btnFindDev.setOnClickListener(this);
		
		btnMicrophone = (Button)v.findViewById(R.id.btnMicrophone);
		btnMicrophone.setOnClickListener(this);
		
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.main, menu);
		menu.findItem(R.id.action_start_discovery).setVisible(false);
		getActivity().invalidateOptionsMenu();
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		//btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(sAddress);	
		
		Log.d(LIFECYCLE, "DetailFragment.onStart");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.d(LIFECYCLE, "DetailFragment.onResume");		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.d(LIFECYCLE, "DetailFragment.onPause");
	}
	
	@Override
	public void onStop(){
		super.onStop();	
		try{
			outStream.write("close".getBytes());
			btSocket.close();
		}catch(IOException e){ }
		Log.d(LIFECYCLE, "DetailFragment.onStop");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String s = "";
		msg = new Message();
//		OutputStream outStream = null;
		
		switch(v.getId()){
		case R.id.btnGetContacts:
			s = "GetContacts";
			msg.what = BTN_CLICK_GET_CONTACTS;
			break;
			
		case R.id.btnVibrate:
			s = "Vibrate";
			msg.what = BTN_CLICK_VIBRATE;
			break;
			
		case R.id.btnGetHistory:
			s = "GetHistory";
			msg.what = BTN_CLICK_GET_HISTORY;
			break;
			
		case R.id.btnMicrophone:
			s = "Beep";
			msg.what = BTN_CLICK_MIC;
			break;
		}
		Log.d(LOG, s);
		eventListener.fragEvents(msg);
		try{
			outStream.write(s.getBytes());
		}catch(IOException e){ }
	}
	/*
//==============================================================================
	class SocketThread extends Thread{
//		BluetoothSocket socket;
		private boolean isRunning = false;
		private InputStream inStream;
		private OutputStream outStream;
		private byte buf[];
		private int bytesRead;
		private StringBuilder sb = null;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRunning = true;
			try{
				btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
				btSocket.connect();
				
				inStream = btSocket.getInputStream();
				outStream = btSocket.getOutputStream();
				buf = new byte[1024];
//				sb = new StringBuilder();
//				sb.append("BtSocketThread InputStream: ");
				while(isRunning){
					while((bytesRead = inStream.read(buf)) != 0){
						String s = new String(buf, 0, bytesRead);
						s = "DetailFragment read from socket: " + s;
						Log.d(LOG, s);
					}
				}
			} catch( IOException e){ 
				try{
					btSocket.close();
					
				}catch(IOException e2){
					Log.d(LOG, "IOExcpetion close socket: "+ e2.getMessage());
				}
				Log.d(LOG, "IOException BtSocketRunnable thread: " + e.getMessage());
				//FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
				FragmentManager fm = getActivity().getFragmentManager();
				fm.popBackStack();				
			}
		}
		
		public void write(String msg){
			if(btSocket.isConnected()){		
				try{
					outStream.write(msg.getBytes());
				}catch(IOException e){ 
					Log.d(LOG, "IOExcepion SocketThread: "+e.getMessage());
				}
			}
		}
		
		public void stopThread(){
			isRunning = false;
			try{
				btSocket.close();
			}catch(IOException e){ }
		}
		
	} */
}




