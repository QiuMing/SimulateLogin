package com.ming.simulateLogin.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class GetPageHtml {
	
	//这里要设置cookieManager，以便每次访问都带上cookie
	public static OkHttpClient client =  new OkHttpClient().setCookieHandler(new CookieManager(new PersistentCookieStore(), CookiePolicy.ACCEPT_ALL));
	
	public static String getPageHtml(String url) {

		if(StringUtil.isBlank(url))
			return "";
		String result = null;
		try {
			Request request = new Request.Builder().url(url)
				    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
				    .build();
			Response response = client.newCall(request).execute();
			result = response.body().string(); 
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("IO 异常，获取网页失败");
		}
		return result;
	}

	public static void getAndSaveCaptcha(String url){
		System.out.println("验证码网址:" + url);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();
		try {
			Response code = client.newCall(request).execute();
			FileOutputStream fos = new FileOutputStream(new File("captcha.png"));
			
			fos.write(code.body().bytes());
			fos.close();
			System.out.println("获取验证码成功");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("获取验证码失败");
			e.printStackTrace();
		}
	}
	
	public static String getInputValue(String pageContent,String name){
		Document parse = Jsoup.parse(pageContent);
		String selectContent = "input[name="+name+"]";
		System.err.println("+++  "+selectContent);
        String result = parse.select(selectContent).get(0).attr("value").trim();
        return result;
	}

}
