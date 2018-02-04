package com.example.testservice;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class myBroadCastReceiver extends BroadcastReceiver {
	private static final String TAG = "myBroadCastReceiver";
	private Handler mHandler;

	public myBroadCastReceiver() {
		// Log.i(TAG, "999999999");
	}

	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (mHandler != null) {
			// Log.i(TAG, "1234567890");
			Message msg = mHandler.obtainMessage();
			// 数据是通过Bundle传递的
			msg.setData(intent.getExtras());
			mHandler.sendMessage(msg);
		}

	}

}
