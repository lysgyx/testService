package com.example.testservice;

import java.util.HashMap;
import java.util.Map;

public class SocketEntity {
	// 用来存储关于通讯信息的txt信息
	public static Map<String, String> valueMap = new HashMap<String, String>();
	// 用来判断当前的模式 0 是未打开任何模式 1 测距模式 2 测角模式
	public static int CEMOSHI = 0;// 默认是未打开任何模式
	// 用来判断是否已经存在和全站仪通讯的仪器 存在为 true 不存在为false 默认为false
	public static boolean ISCUNZAI = false;
	
}
