package com.kolpashikov.bluedevicemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BTService extends Service {
	final String uuid = "993d463c-d711-11e4-b9d6-1681e6b88ec1";
	final String LOG = "mLogs";
	final String LIFECYCLE = "LifeCycle";
	static final String BROADCAST_ACTION = "com.kolpashikov.bluedevicemanager";
	static final String CONNECTION_STATE = "connectionstate";
		
	public static final int CONNECTION_ABORTED = 1;
	public static final int CONNECTION_ESTABLISHED = 2;
	
	BluetoothAdapter btAdapter;
	BluetoothServerSocket btServerSocket;
	BluetoothSocket btSocket;
	int mStartId;
	MainThread t;
	
	public boolean isServiceRunning = false; 
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		String s = "SERVICE: onBind";
		Log.d(LIFECYCLE, s);
		
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		String s = "Service: onCreate";
		Log.d(LIFECYCLE, s);
		btAdapter = BluetoothAdapter.getDefaultAdapter();		
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		String s = "Service: onStartCommand";
		
		Log.d(LIFECYCLE, s);
		mStartId = startId;
		
//		PendingIntent pi = intent.getParcelableExtra(PARAM_PENDING);
		
//		t = new MainThread(startId, uuid, pi);
		t = new MainThread(startId, uuid);
		t.start();
		isServiceRunning = true;
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDestroy(){
		super.onDestroy();
		String s = "Service: onDestroy";
		Log.d(LIFECYCLE, s);
		isServiceRunning = false;
		t.stopThread();
		//stopSelf(mStartId);		
	}

/***********************************************************
 *  
 * Вложенные классы, MainThread extends Thread
 * 
**************************************************************/
	public class MainThread extends Thread{
		
		int startId;
		UUID uuid;
		String deviceName;
				
		InputStream inStream;
		OutputStream outStream;
		int bytes;
		byte[] buffer;
		
		private boolean isRunning = true;
		
//		public MainThread(int _startId, String _uuid, PendingIntent _pi){
		public MainThread(int _startId, String _uuid){
			startId = _startId;
			uuid = UUID.fromString(_uuid);
//			pi = _pi;
			
			BluetoothServerSocket tmp = null;
			try{
				tmp = btAdapter.listenUsingRfcommWithServiceRecord("AndroidBluetoothAdmin", uuid);
				btServerSocket = tmp;
				buffer = new byte[1024];
				Log.d(LOG, "from thread: constructor");
				isRunning = true;			
			}catch(IOException e){
				isRunning = false;
			}
			
			deviceName = btAdapter.getName();

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(LOG, "From thread: start run()");
			while(isRunning){
				try{
					btSocket = btServerSocket.accept();
					Log.d(LOG, deviceName + " btServerSocket accepted");
				}catch(IOException e){ }
				Log.d(LOG, deviceName + "BTService: socket accepted");
				
				try{
					inStream = btSocket.getInputStream();
					outStream = btSocket.getOutputStream();
					while(isRunning){
						bytes = inStream.read(buffer);
						String s = new String(buffer, 0, bytes);
						Log.d(LOG, deviceName + "From BTService: " + s);
					}				
				}catch(IOException e){
					// TODO: Реализовать посылку сообщения о разрыве коннекта в MainActivity
					Intent intent = new Intent(BROADCAST_ACTION);
					intent.putExtra(CONNECTION_STATE, CONNECTION_ABORTED);
//					try{
//						pi.send(BTService.CONNECTION_ABORTED);
//					}catch(CanceledException e1){ 
//						Log.d(LOG, "BTService exception: " + e1.getMessage());
//					}
				}		
			}// while(true)...
			stopSelf();
			Log.d(LOG, deviceName + "BTService from thread: run() stop");
		}// public void run()
		
		public void write(byte[] buffer){
			try{
				outStream.write(buffer);
			}catch(IOException e){}
			Log.d(LOG, deviceName + "BTService from thread: write() bytes");
		}
		
		public void stopThread(){			
			isRunning = false;
			try{
				btServerSocket.close();
			}catch(IOException e){
				Log.d(LOG, deviceName + " IOException: " + e.getMessage());
			}
		}
		
	}

}