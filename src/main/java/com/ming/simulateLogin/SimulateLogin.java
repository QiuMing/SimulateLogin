package com.ming.simulateLogin;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.helper.StringUtil;

import com.ming.simulateLogin.util.GetPageHtml;
import com.ming.simulateLogin.util.PropertieUtil;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class SimulateLogin {
	public static String filePath = "zhihu.properties";	
	public static String doLoginUrl = PropertieUtil.getValueByKey(filePath, "dologinUrl");
	public static String loginPage = PropertieUtil.getValueByKey(filePath, "loginPage");

	public static boolean dataPreparation(){
		if(!isBaseConfigRight())
			return false;
		
		String pageContent = GetPageHtml.getPageHtml(loginPage);
		if(StringUtil.isBlank(pageContent)){
			System.err.println("获取网页失败");
			return false;
		}
		 try {
			Map<String,String> map = PropertieUtil.getProperties(filePath);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
			   System.out.println("key= " + key + " and value= " + value);
			   if(StringUtil.isBlank(entry.getValue())){
				   String valueFormPage = GetPageHtml.getInputValue(pageContent, key);
				   PropertieUtil.writeProperties(filePath, key, valueFormPage);
			   }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
		String captchaUrl = PropertieUtil.getValueByKey(filePath, "captcha.url");
		if(!StringUtil.isBlank(captchaUrl)){
			GetPageHtml.getAndSaveCaptcha(captchaUrl);
			
			System.out.println("输入验证码的值");
			Scanner sc = new Scanner(System.in);
			String inputCatchaValue=sc.next().trim();
			System.out.println(inputCatchaValue);
			try {
				PropertieUtil.writeProperties(filePath, "captcha.value", inputCatchaValue);
				System.out.println("验证码数据写入配置文件成功");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("验证码值准备失败");
			}
		}
		else
			System.out.println("没有验证码属性，最终提交数据将不含验证码");
		
		return true;
	}
	
	public static void login(){
		 
		FormEncodingBuilder formBuilder = new FormEncodingBuilder();
		Map<String,String> map = PropertieUtil.getProperties(filePath);
		
		map.remove("dologinUrl");
		map.remove("loginPage");
		
		//如果有配置验证码路径，请求则带上验证码的值
		if(map.containsKey("captcha.url")){
			map.remove("captcha.url");
			String captchaName = map.get("captcha.name");
			String captchaValue = map.get("captcha.value");
			formBuilder = formBuilder.add(captchaName, captchaValue);
			System.out.println("key= " + captchaName + " and value= " + captchaValue);
			map.remove("captcha.name");
			map.remove("captcha.value");
		}
		
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println("key= " + key + " and value= " + value);
			formBuilder = formBuilder.add(key, value);
		}
		
		RequestBody formBody = formBuilder.build();
		Request login = new Request.Builder()
        .url(doLoginUrl)
        .post(formBody)
        .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
        .build();
		
		Response execute;
		try {
			execute = GetPageHtml.client.newCall(login).execute();
			System.out.println(execute.body().string());
		} catch (IOException e) {
			System.err.println("IO异常");
			e.printStackTrace();
		}
	}
	
	private static boolean isBaseConfigRight(){
		if(StringUtil.isBlank(loginPage)||StringUtil.isBlank(doLoginUrl)){
			System.err.println("基本配置数据不完整");
			return false;
		}
		return true;
	}
}
