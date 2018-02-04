package com.example.testservice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

public class CommunicationService extends Service {
	protected static final String ACTION = "com.example.testbroadcastreceiver";
	private static final String TAG = "MainActivity";
	// 当地的蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	// 成员对象的聊天服务
	private BluetoothChatService mChatService = null;
	// Intent需要 编码
	public static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// 类型的消息发送从bluetoothchatservice处理程序
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// 返回数据类型
	public static final int STYLE_ANGLE_H = 0;
	public static final int STYLE_ANGLE_V = 1;
	public static final int STYLE_DISTANCE = 2;
	public static final int STYLE_MESSAGE = 3;
	public static final int DEVICE_NONE = 10;
	public static final int DEVICE_CONNECTED = 11;

	// 键名字从收到的bluetoothchatservice处理程序
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// [start]需要覆写的方法
	@Override
	public IBinder onBind(Intent intent) {
		// 如果本程序启动时蓝牙没有打开，要求它启用。 setupchat()将被称为在onactivityresult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// startActivity(enableIntent);
			enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(enableIntent);
		}
		if (mChatService != null) {// 如果没有连接到蓝牙则直接连接蓝牙
			if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
				linkBluetooth();
			}
		} else {
			linkBluetooth();
		}
		setentity();// 临时设置通讯参数
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		// 如果本程序启动时蓝牙没有打开，要求它启用。 setupchat()将被称为在onactivityresult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// startActivity(enableIntent);
			enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(enableIntent);
		}
		if (mChatService != null) {// 如果没有连接到蓝牙则直接连接蓝牙
			if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
				linkBluetooth();
			}
		} else {
			linkBluetooth();
		}
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopBluetooth();
		return super.onUnbind(intent);
	}

	// [end]

	private myBinder mBinder = new myBinder();

	public class myBinder extends Binder {
		public CommunicationService getService() {
			return CommunicationService.this;
		}
	}

	// [start]蓝牙设备地址获取
	private String bAddress;
	// DeviceListActivity返回的蓝牙设备地址通过BroadCast发送，myBroadCastReceiver接收之后通过sHandler方法传送到此，
	private Handler sHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (mChatService != null) {// 如果已经连接到蓝牙则直接退出
				if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
					return;
				}
			}
			Bundle b = msg.getData();
			String add = b.getString("address");
			if (add != null) {
				bAddress = add;
				Log.i(TAG, "address=" + add);
				// 把蓝牙设备对象
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(add);
				// 初始化bluetoothchatservice执行蓝牙连接
				mChatService = new BluetoothChatService(getApplication(), mHandler);
				// 试图连接到装置
				mChatService.connect(device);
				// 解除BroadcastReceiver注册
				unregisterReceiver();
			}
		}
	};
	// 定义BroadCast
	private myBroadCastReceiver myBCR;

	// 注册BroadCastReceiver
	private void registerReceiver() {
		if (myBCR == null) {
			myBCR = new myBroadCastReceiver();
			myBCR.setHandler(sHandler);
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION);
			registerReceiver(myBCR, filter);
		}
	}

	// 注销BroadCastReceiver
	private void unregisterReceiver() {
		if (myBCR != null) {
			unregisterReceiver(myBCR);
			myBCR = null;
		}
	}

	// 启动获取蓝牙设备地址得窗体
	public void linkBluetooth() {
		if (mChatService != null) {// 如果已经连接到蓝牙则直接退出
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				return;
			}
		}
		registerReceiver();// 注册BroadCastReceiver，
		Intent dialogIntent = new Intent(getBaseContext(), DeviceListActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(dialogIntent);

	}

	// 断开蓝牙连接
	public void stopBluetooth() {
		if (mChatService != null) {
			mChatService.stop();
		}
	}

	// 获取蓝牙状态
	public int getBluetoothState() {
		if (mChatService != null) {
			return mChatService.getState();
		} else {
			return BluetoothChatService.STATE_NONE;
		}
	}

	// 发送数据
	public void sendMessage(String msg) {
		Log.i(TAG, "sendmsg=" + msg);
		if (mChatService != null) {
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				String str = msg + "\r\n";
				byte[] b = str.getBytes();
				mChatService.write(b);
			}
		}
	}

	// [end]
	// [start]接收蓝牙返回的信息
	int n;
	byte[] buffer = new byte[1024];
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					aHandler.obtainMessage(CommunicationService.DEVICE_CONNECTED).sendToTarget();
					Log.i(TAG, "MESSAGE_STATE_CHANGE: STATE_CONNECTED");
					break;
				case BluetoothChatService.STATE_CONNECTING:
					Log.i(TAG, "MESSAGE_STATE_CHANGE: STATE_CONNECTING");
					break;
				case BluetoothChatService.STATE_LISTEN:
					Log.i(TAG, "MESSAGE_STATE_CHANGE: STATE_LISTEN");
					break;
				case BluetoothChatService.STATE_NONE:
					aHandler.obtainMessage(CommunicationService.DEVICE_NONE).sendToTarget();
					;
					Log.i(TAG, "MESSAGE_STATE_CHANGE: STATE_NONE");
					break;
				}
				break;
			case MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// 构建一个字符串缓冲区
				// String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				int i;
				for (i = 0; i < msg.arg1; i++) {
					buffer[n] = readBuf[i];
					n += 1;
				}
				Log.i(TAG, "接接收到返回信息");
				if (n > 1) {
					if (buffer[n - 1] == 10 && buffer[n - 2] == 13) {
						String readMsg = new String(buffer, 0, n);
						aHandler.obtainMessage(3, -1, -1, readMsg).sendToTarget();
						Log.i(TAG, "返回信息=" + readMsg);
						n = 0;
					}
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// 保存该连接装置的名字
				String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Log.i(TAG, "MESSAGE_DEVICE_NAME: " + mConnectedDeviceName);
				if (mConnectedDeviceName != null) {
					Toast.makeText(getApplicationContext(), "已连接到 " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				}
				break;
			case MESSAGE_TOAST:
				String str = msg.getData().getString(TOAST);
				Log.i(TAG, "MESSAGE_TOAST: " + str);
				if (str != null) {
					Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	};
	// 用来向调用者返回数据
	private Handler aHandler;

	// 设置用来向调用CommService的Activity返回数据使用的Handler
	public void setHandler(Handler handler) {
		aHandler = handler;
	}

	// [end]
	// [start]与仪器通讯相关的
	/*
	 * 以TOPCON GTS-602全站仪为例
	 * 
	 * NAME@TOPCON GTS-602
	 * 
	 * OPEN@9600,N,8,1
	 * 
	 * MODEANGLE@"Z20088"+3
	 * 
	 * MODEDIST@"Z34093"+3
	 * 
	 * PRINT@"C067"+3 PRINT@"C067"+3
	 * 
	 * DISTANCE@-------55555666-3334444-1112222-----------------------
	 * 
	 * ANGLE@------3334444-1112222---------
	 */
	public static String InstrumentName = "TOPCON GTS-602";
	public static String RS232Parament = "9600,N,8,1";
	public static String ModeAngle = "Z20088";
	public static String ModeDistance = "Z34093";
	public static String CommandSurvey = "C067";
	public static String StyleDistance = "------55555666-3334444-1112222-----------------------";
	public static String StyleAngle = "-----3334444-1112222---------";
	public static String EndChar = "";

	String dist = "006?+00001060m0905356+3545105d+00001060t60+00+00096";
	// ------55555666-3334444-1112222-----------------------
	String angle = "006<0905356+3545105-0032d080";

	// -----3334444-1112222---------

	// 临时用来设置测试数据
	private void setentity() {
		SocketEntity.valueMap.put("NAME", InstrumentName);
		SocketEntity.valueMap.put("OPEN", RS232Parament);
		SocketEntity.valueMap.put("MODEANGLE", ModeAngle);
		SocketEntity.valueMap.put("MODEDIST", ModeDistance);
		SocketEntity.valueMap.put("PRINT", CommandSurvey);
		SocketEntity.valueMap.put("DISTANCE", StyleDistance);
		SocketEntity.valueMap.put("ANGLE", StyleAngle);
	}

	// 解析水平角度
	private String parseAngleH(String dat) {
		String str = SocketEntity.valueMap.get("ANGLE");
		int start1 = str.indexOf("1");
		int end1 = str.lastIndexOf("1") + 1;
		int start2 = str.indexOf("2");
		int end2 = str.lastIndexOf("2") + 1;
		if (dat.length() > end2) {
			String Value = Integer.parseInt(dat.substring(start1, end1)) + ".";
			Value += dat.substring(start2, end2);
			return Value;
		} else {
			return "";
		}
	}

	// 解析垂直角度
	private String parseAngleV(String dat) {
		String str = SocketEntity.valueMap.get("ANGLE");
		int start3 = str.indexOf("3");
		int end3 = str.lastIndexOf("3") + 1;
		int start4 = str.indexOf("4");
		int end4 = str.lastIndexOf("4") + 1;
		if (dat.length() > end4) {
			String Value = Integer.parseInt(dat.substring(start3, end3)) + ".";
			Value += dat.substring(start4, end4);
			return Value;
		} else {
			return "";
		}
	}

	// 解析距离
	private String parseDistance(String dat) {
		String str = SocketEntity.valueMap.get("DISTANCE");
		int start5 = str.indexOf("5");
		int end5 = str.lastIndexOf("5") + 1;
		int start6 = str.indexOf("6");
		int end6 = str.lastIndexOf("6") + 1;
		if (dat.length() > end6) {
			String Value = Integer.parseInt(dat.substring(start5, end5)) + ".";
			Value += dat.substring(start6, end6);
			return Value;
		} else {
			return "";
		}
	}

	public void parseData(int mode) {
		String str = "";
		switch (mode) {
		case 0:
			str = parseAngleH(angle);
			break;
		case 1:
			str = parseAngleV(angle);
			break;
		case 2:
			str = parseDistance(dist);
			break;
		default:
			break;
		}
		int what = mode;
		int arg1 = -1;
		int arg2 = -1;
		Object obj = str;
		aHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
	}
	// [end]
}
