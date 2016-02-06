package com.ming.simulateLogin.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertieUtil {
	 
	public static boolean isExistProperties(String filePath) {
		File file = new File(filePath);
		if (file.exists())
			return true;
		else
			return false;
	}

	public static boolean createPropertiesIfNotExist(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	// 根据Key读取Value
	public static String getValueByKey(String filePath, String key) {
		Properties pps = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			pps.load(in);
			String value = pps.getProperty(key);
			return value;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 读取Properties的全部信息
	public static void getAllProperties(String filePath) throws IOException {
		Properties pps = new Properties();
		InputStream in = new BufferedInputStream(new FileInputStream(filePath));
		pps.load(in);
		Enumeration en = pps.propertyNames(); // 得到配置文件的名字
		System.out.println("读取"+filePath);
		while (en.hasMoreElements()) {
			String strKey = (String) en.nextElement();
			String strValue = pps.getProperty(strKey);
			//System.out.println("    "+strKey + "=" + strValue);
		}

	}

	public static Map<String, String> getProperties(String filePath) {
		Map<String,String>  map = new HashMap<String, String>();
		Properties pps = new Properties();
		InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(filePath));
			pps.load(in);
			Enumeration en = pps.propertyNames(); // 得到配置文件的名字

			while (en.hasMoreElements()) {
				String strKey = (String) en.nextElement();
				String strValue = pps.getProperty(strKey);
				//System.out.println("	"+strKey + "=" + strValue);
				map.put(strKey, strValue);
				
			}
			return map;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// 写入Properties信息
	public static void writeProperties(String filePath, String pKey,
			String pValue) throws IOException {
		Properties pps = new Properties();

		InputStream in = new FileInputStream(filePath);
		// 从输入流中读取属性列表（键和元素对）
		pps.load(in);
		// 调用 Hashtable 的方法 put。使用 getProperty 方法提供并行性。
		// 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
		OutputStream out = new FileOutputStream(filePath);
		pps.setProperty(pKey, pValue);
		// 以适合使用 load 方法加载到 Properties 表中的格式，
		// 将此 Properties 表中的属性列表（键和元素对）写入输出流
		pps.store(out, "Update " + pKey + " name");
	}

	public static void main(String[] args) throws IOException {
		// String value = GetValueByKey("Test.properties", "name");
		// System.out.println(value);
		//getAllProperties("name.properties");
		Map<String,String> map = PropertieUtil.getProperties("baseConf.properties");
		for (Map.Entry<String, String> entry : map.entrySet()) {
		   System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
		}
		// WriteProperties("Test.properties", "long", "212");
	}
}