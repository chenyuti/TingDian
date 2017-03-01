package com.logansoft.UIEngine.keyboard;

import javax.xml.parsers.DocumentBuilderFactory;

public class DocumentFactory {
	static DocumentBuilderFactory factory =null;
	public static DocumentBuilderFactory getInstance()
	{
		if(factory == null)
		 factory= DocumentBuilderFactory.newInstance();
		return factory;
	}
	
}
