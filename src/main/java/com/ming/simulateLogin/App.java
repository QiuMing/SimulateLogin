package com.ming.simulateLogin;

public class App {
	
	public static void main(String[] args) {
		if(SimulateLogin.dataPreparation())
			SimulateLogin.login();
		else
			System.err.println("登陆数据准备失败");
	}
}
