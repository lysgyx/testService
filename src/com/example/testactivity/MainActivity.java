package com.example.testactivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testservice.CommunicationService;
import com.example.testservice.R;
import com.example.testservice.CommunicationService.myBinder;

public class MainActivity extends Activity {
	protected static final String ACTION = "com.example.testbroadcastreceiver";
	private static final String TAG = "MainActivity";
	boolean isConnected;
	Button btn1, btn2, btn3, btn4, btn5, btn6;
	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

//	private myBroadCastReceiver myBCR;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btn1 = (Button) findViewById(R.id.btn_bindservice);
		btn2 = (Button) findViewById(R.id.btn_unbindservice);
		btn3 = (Button) findViewById(R.id.btn_launchactivity);
		btn4 = (Button) findViewById(R.id.btn_stopbluetooth);
		btn5 = (Button) findViewById(R.id.btn_sendmessage);
		btn6 = (Button) findViewById(R.id.launch1);
		btn1.setOnClickListener(btnListener);
		btn2.setOnClickListener(btnListener);
		btn3.setOnClickListener(btnListener);
		btn4.setOnClickListener(btnListener);
		btn5.setOnClickListener(btnListener);
		btn6.setOnClickListener(btnListener);
	}

	OnClickListener btnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_bindservice:
				bindService();
				break;
			case R.id.btn_unbindservice:
				unBindService();
				break;
			case R.id.btn_launchactivity:
				if (mService != null) {
					mService.linkBluetooth();
				}
				break;
			case R.id.btn_stopbluetooth:
				if (mService != null) {
					mService.stopBluetooth();
				}
				break;
			case R.id.btn_sendmessage:
				if (mService != null) {
					mService.sendMessage("好事成双");
				}
				break;
			case R.id.launch1:
				Uri uri = Uri.parse("http://www.baidu.com");
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(it);

//				Intent intent = new Intent(MainActivity.this, survey.class);
//				startActivity(intent);
				break;
			}
		}
	};
	@Override
	protected void onPause() {
		unBindService();
		super.onPause();
	}

//	private void registerReceiver() {
//		if (myBCR == null) {
//			myBCR = new myBroadCastReceiver();
//			myBCR.setHandler(mHandler);
//			IntentFilter filter = new IntentFilter();
//			filter.addAction(ACTION);
//			registerReceiver(myBCR, filter);
//		}
//	}
//
//	private void unregisterReceiver() {
//		if (myBCR != null) {
//			unregisterReceiver(myBCR);
//			myBCR = null;
//		}
//	}

	private void unBindService() {
		if (isConnected) {
			Log.i(TAG, "unBindService");
			unbindService(conn);
			isConnected = false;
		}
	}

	private void bindService() {
		if (!isConnected) {
			Log.i(TAG, "bindService");
			Intent intent = new Intent(MainActivity.this, CommunicationService.class);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
		}
	}

	private CommunicationService mService;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isConnected = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myBinder mBinder = (myBinder) service;
			mService = mBinder.getService();
			mService.setHandler(mHandler);
			isConnected = true;
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String str = "";
			Log.i(TAG, "msg.what=" + msg.what);
			if (msg.what == CommunicationService.MESSAGE_READ) {
				str = (String) msg.obj;
			}
			TextView tv = (TextView) findViewById(R.id.tv1);
			tv.setText("message=" + str);
		}

	};



}
