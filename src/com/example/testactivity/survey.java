package com.example.testactivity;

import com.example.testservice.CommunicationService;
import com.example.testservice.R;
import com.example.testservice.CommunicationService.myBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class survey extends Activity {
	protected static final String TAG = "survey";
	CheckBox chkBox1;
	TextView tv1;
	Button btn1;
	EditText et1, et2, et3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		chkBox1 = (CheckBox) findViewById(R.id.checkBox1);
		tv1 = (TextView) findViewById(R.id.textView1);
		btn1 = (Button) findViewById(R.id.button1);
		et1 = (EditText) findViewById(R.id.et1);
		et2 = (EditText) findViewById(R.id.et2);
		et3 = (EditText) findViewById(R.id.et3);
		chkBox1.setOnCheckedChangeListener(chkboxListener);
		btn1.setOnClickListener(btnClickListener);
		et1.setOnFocusChangeListener(onEditFocusChanged);
		et2.setOnFocusChangeListener(onEditFocusChanged);
		et3.setOnFocusChangeListener(onEditFocusChanged);
	}

	@Override
	protected void onPause() {
		unBindService();
		super.onPause();
	}

	EditText currentET = null;
	OnFocusChangeListener onEditFocusChanged = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			currentET = (EditText) arg0;
			int mod = 0;
			switch (arg0.getId()) {
			case R.id.et1:
				mod = CommunicationService.STYLE_ANGLE_H;
				break;
			case R.id.et2:
				mod = CommunicationService.STYLE_ANGLE_V;
				break;
			case R.id.et3:
				mod = CommunicationService.STYLE_DISTANCE;
				break;
			}
			if (mService != null) {
				if (isConnected) {
					mService.parseData(mod);
				}
			}
		}
	};

	OnCheckedChangeListener chkboxListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			CheckBox chkbox = (CheckBox) arg0;
			if (chkbox.isChecked()) {
				bindService();
			} else {
				unBindService();
			}
		}
	};
	int nn = 0;
	OnClickListener btnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			mService.parseData(nn);
			nn++;
			if (nn > 2)
				nn = 0;
		}
	};

	// [start]这部分需要拷贝到Activity中实现蓝牙传输功能
	boolean isConnected;

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
			Intent intent = new Intent(survey.this, CommunicationService.class);
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
			str = "what=" + msg.what + "  msg=" + (String) msg.obj;
			Log.i(TAG, "msg.what=" + msg.what);
			// if (msg.what == CommunicationService.MESSAGE_READ) {
			// }
			switch (msg.what) {
			case CommunicationService.STYLE_ANGLE_H:
				str = "angleH\n" + str;
				break;
			case CommunicationService.STYLE_ANGLE_V:
				str = "angleV\n" + str;
				break;
			case CommunicationService.STYLE_DISTANCE:
				str = "distance\n" + str;
				break;
			case CommunicationService.STYLE_MESSAGE:
				str = "allData=" + str;
				break;
			case CommunicationService.DEVICE_NONE:
				chkBox1.setChecked(false);
				break;
			default:
				break;
			}
			if(msg.what!=CommunicationService.STYLE_MESSAGE){
				if (currentET != null) {
					if (currentET.hasFocus()) {
						currentET.setText(((String) msg.obj));
					}
				}
			}
		}

	};
	// [end]
}
