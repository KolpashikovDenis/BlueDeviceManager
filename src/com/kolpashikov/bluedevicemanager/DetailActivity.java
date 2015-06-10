package com.kolpashikov.bluedevicemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.kolpashikov.bluedevicemanager.MainActivity.onEventListener;

public class DetailActivity extends Activity implements onEventListener{
	final static String LOG = "mLogsDetail";
	static final String SADDR = "sAddr";
	static final String SNAME = "sName";
	final static String uuid = "993d463c-d711-11e4-b9d6-1681e6b88ec1";
	
	public final int BTN_CLICK_GET_CONTACTS = 0x1;
	public final int BTN_CLICK_GET_HISTORY  = 0x2;
	public final int BTN_CLICK_SCREEN_BLINK = 0x3;
	public final int BTN_CLICK_VIBRATE		= 0x4;
	public final int BTN_CLICK_BEEP         = 0x5;
	
	public static final int HANDLE_MSG_CONNECTED = 0x1;
	public static final int HANDLE_MSG_NOTCONNECTED = 0x2;

	String addr;
	
	DetailFragment fragDetail;
	FragmentConnecting fragConnecting;
	FragmentTransaction ft;
	static Handler handler;
	public static ConnectThread connectThread;
	BluetoothSocket socket;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		String sAddr = getIntent().getExtras().getString(SADDR);
		String sName = getIntent().getExtras().getString(SNAME);
//		fragDetail = new DetailFragment(connectThread);
		fragConnecting = new FragmentConnecting();
		ft = getFragmentManager().beginTransaction();
		ft.add(R.id.frameDetailLayout, fragConnecting);
		ft.commit();
		Toast.makeText(this, sAddr + "-"+sName, Toast.LENGTH_SHORT).show();
		
		handler = new Handler(){
			
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){	
				case HANDLE_MSG_CONNECTED:
					socket = (BluetoothSocket)msg.obj;
					fragDetail = new DetailFragment(socket);
					ft = getFragmentManager().beginTransaction();
					ft.replace(R.id.frameDetailLayout, fragDetail);
					ft.commit();
					break;
					
				case HANDLE_MSG_NOTCONNECTED:
					Toast.makeText(DetailActivity.this, "Cannot connect device", 
							Toast.LENGTH_SHORT).show();
					finish();
					break;
				}
			}
			
		};
		
		connectThread = new ConnectThread(sAddr);
		connectThread.start();
		
	}	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		connectThread.stopThread();
	}
	
	public void btnsClick(Message msg){
		switch(msg.what){
		
		}
		
	}
	
	@Override
	public void fragEvents(Message msg) {
		// TODO Auto-generated method stub
		String s = null;
		switch(msg.what){
		case BTN_CLICK_GET_CONTACTS:
			s = "BTN_CLICK_GET_CONTACTS"; break;
		case BTN_CLICK_GET_HISTORY:
			s= "BTN_CLICK_GET_HISTORY";	break;
		case BTN_CLICK_SCREEN_BLINK:
			s = "BTN_CLICK_SCREEN_BLINK"; break;
		case BTN_CLICK_VIBRATE:
			s = "BTN_CLICK_VIBRATE"; break;
		case BTN_CLICK_BEEP:
			s = "BTN_CLICK_BEEP"; break;
		}
		Toast.makeText(this, "DetailActivity: " + s, Toast.LENGTH_SHORT).show();
	}
/*
 *      SUB CLASSES	
 */
	
	public static class ConnectThread extends Thread {
		String address;
		BluetoothDevice btDevice;
		BluetoothSocket btSocket;
		boolean isRunning = false;
		InputStream inStream;
		OutputStream outStream;
		int bytesRead;
		byte[] buf;
		Message msg;
		
		public ConnectThread(String _address){
			address = _address;
			// TODO: получить socket, если получится подключится, то отправляем инфу
			// Handler'у, который
			btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);			
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRunning = true;
			try{
				btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
				btSocket.connect();
				Log.d(LOG, "Detail: thread run - socket connected");
				// TODO: Заебца-а-а, подключились, информируем об этом активность, иначе..... 
				// смотри в ексепшене
				msg = handler.obtainMessage(HANDLE_MSG_CONNECTED, 0, 0, btSocket);
				handler.sendMessage(msg);

				
				inStream = btSocket.getInputStream();
				outStream = btSocket.getOutputStream();
				buf = new byte[1024];
				while(isRunning){
					while((bytesRead = inStream.read(buf)) != 0){
						String s = new String(buf, 0, bytesRead);
						s = "DetailFragment read from socket: " + s;
						Log.d(LOG, s);
					}
				}
			} catch( IOException e){ 
				try{
					Log.d(LOG, "Detail: thread run - socket connection failed");
					handler.sendEmptyMessage(HANDLE_MSG_NOTCONNECTED);
					btSocket.close();					
				/*
				 *  TODO: .... тут мы не подключились, поэтому закрываем эту активность
				 *  и выходим из нее...
				 */
					
				}catch(IOException e2){
					Log.d(LOG, "IOExcpetion close socket: "+ e2.getMessage());
				}
				Log.d(LOG, "IOException BtSocketRunnable thread: " + e.getMessage());
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
				if(outStream != null){
					write("close");
				}
				btSocket.close();
			}catch(IOException e){ }
		}
	}

}



