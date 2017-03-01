package com.logansoft.UIEngine.parse.xmlview;

import java.util.HashMap;

//暂时这样，ruunable必须被干掉，lua必须主线程，不然到处都是乱七八糟的界面更新时序
public class RunableHelper implements Runnable {
	protected boolean booleanValue;
	protected String stringValue;
	protected int intValue;
	protected Object objectValue;
	protected HashMap<String,Object> params;
	
	public RunableHelper(){
		
	}
	
	public RunableHelper(Object objectValue){
		this.objectValue=objectValue;
	}
	public RunableHelper( HashMap<String,Object> params){
		this.params=params;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
