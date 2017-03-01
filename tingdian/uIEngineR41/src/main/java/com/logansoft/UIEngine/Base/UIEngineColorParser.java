package com.logansoft.UIEngine.Base;

import java.util.HashMap;

import android.graphics.Color;


public class UIEngineColorParser {
	private static final HashMap<String,Integer> colorMap;
	static{
		colorMap=new HashMap<String,Integer>();
		colorMap.put("red",Color.RED);
		colorMap.put("black", Color.BLACK);
		colorMap.put("white", Color.WHITE);
		colorMap.put("yellow", Color.YELLOW);
		colorMap.put("blue", Color.BLUE);
		colorMap.put("green", Color.GREEN);
		colorMap.put("magenta", Color.MAGENTA);
		colorMap.put("gray", Color.GRAY);
		colorMap.put("ltgray", Color.LTGRAY);
		colorMap.put("cyan", Color.CYAN);
		colorMap.put("clear", Color.TRANSPARENT);
	}
	public static void putColor(String colorName,int color){
		colorMap.put(colorName, color);
	}
    public static int getColor(String colorString) {
    	if(colorString==null||colorString.length()==0)
         	return -2;
        Integer color=colorMap.get(colorString);
        if(color!=null)
        	return color;
        
        int c1 = -2;
        int colorlength=colorString.length();
        if (colorString.charAt(0) == '#' &&(colorlength==7||colorlength==9)) {
        	int []ac=new int[4];
        	int index=0;
        	for(int i=1;i<colorlength;i+=2){
        		char first=colorString.charAt(i);
        		int a=0;
        		if(first>='0' && first<='9') a=first-'0';
        		else{
        			first|=32;
        			if(first>='a' && first<='f') a=first-'a'+10;
        		}
        		
                char secound=colorString.charAt(i+1);
                int b=0;
                if(secound>='0' && secound<='9') b=secound-'0';
                else {
                	secound|=32;
                	if(secound>='a' && secound<='f')b=secound-'a'+10;
                }
                ac[index]=a*16+b;
                index++;
        	}
        	if (colorlength==7)
        		c1=Color.rgb(ac[0],ac[1],ac[2]);
        	else if(colorlength==9)
        		c1=Color.argb(ac[0],ac[1],ac[2],ac[3]);
        }
        return c1;
    }

}
