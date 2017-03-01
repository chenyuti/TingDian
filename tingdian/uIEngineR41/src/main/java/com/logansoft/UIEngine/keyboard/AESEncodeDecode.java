package com.logansoft.UIEngine.keyboard;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author wchun
 * 
 *         AES128 绠楁硶锛屽姞瀵嗘ā寮忎负ECB锛屽～鍏呮ā寮忎负 pkcs7锛堝疄闄呭氨鏄痯kcs5锛��
 * 
 * 
 */
public class AESEncodeDecode
{
	//static final String algorithmStr = "AES/ECB/PKCS5Padding";
	static final String algorithmStr = "AES/CBC/PKCS5Padding";

	static private KeyGenerator keyGen;

	static private Cipher cipher;

	static boolean isInited = false;

	//private static byte[] IV = "(%@)!#!$A^G&L*;?".getBytes();
	public static byte[] checkkey = "~;&^%$#@!(*',/".getBytes();

	private static byte[] IV = null;
	private static byte[] key1 = {47, -82, 115, 67, 33, -85, -26, -61, -7, -114, 82, -18, 54, -3, -2, -128};

	
	//private static byte[] key1 = "~;&^%$#@!(*',/".getBytes();
	// 鍒濆鍖��
	static private void init()
	{
		
		IV="0123456789ABCDEF".getBytes();
		// 鍒濆鍖杒eyGen
		try
		{
			keyGen = KeyGenerator.getInstance("AES");
			cipher = Cipher.getInstance(algorithmStr);
			keyGen.init(128);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}

		isInited = true;
	}

	public static byte[] GenKey()
	{
		if(!isInited)// 濡傛灉娌℃湁鍒濆鍖栬繃,鍒欏垵濮嬪寲
		{
			init();
		}
		return keyGen.generateKey().getEncoded();
	}

	public static byte[] AESEncode(byte[] content, byte[] keyBytes)
	{
		if(keyBytes==null){
			keyBytes=key1;
		}
		byte[] encryptedText = null;
		if(!isInited)// 涓哄垵濮嬪寲
		{
			init();
		}
		Key key = new SecretKeySpec(keyBytes, "AES");
		keyBytes=null;

		try
		{
			IvParameterSpec iv = new IvParameterSpec(IV);
			cipher.init(Cipher.ENCRYPT_MODE, key,iv);
			key=null;
			iv=null;
			encryptedText = cipher.doFinal(content);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return encryptedText;
	}
	public static byte[] AESEncodeByIv(byte[] content, byte[] privateBytes,byte[] publicBytes)
	{
		if(privateBytes==null){
			privateBytes=key1;
		}
		byte[] encryptedText = null;
		if(!isInited)// 涓哄垵濮嬪寲
		{
			init();
		}
		Key key = new SecretKeySpec(privateBytes, "AES");

		try
		{
			IvParameterSpec iv = new IvParameterSpec(publicBytes);
			cipher.init(Cipher.ENCRYPT_MODE, key,iv);
			encryptedText = cipher.doFinal(content);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return encryptedText;
	}

	// 瑙ｅ瘑涓篵yte[]
	public static byte[] AESDecode(byte[] content, byte[] keyBytes)
	{
		if(keyBytes==null)
		{
			keyBytes=key1;
		}
		byte[] originBytes = null;
		if(!isInited)
		{
			init();
		}

		Key key = new SecretKeySpec(keyBytes, "AES");
		try
		{
			IvParameterSpec iv = new IvParameterSpec(IV);//浣跨敤CBC妯″紡锛岄渶瑕佷竴涓悜閲廼v锛屽彲澧炲姞鍔犲瘑绠楁硶鐨勫己搴�� 
			cipher.init(Cipher.DECRYPT_MODE, key,iv);
			originBytes = cipher.doFinal(content);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return originBytes;
	}
	
	// 瑙ｅ瘑涓篵yte[]
		public static byte[] AESDecodeByIV(byte[] content, byte[] keyBytes,byte[] IVBytes)
		{
			if(keyBytes==null)
			{
				keyBytes=key1;
			}
			byte[] originBytes = null;
			if(!isInited)
			{
				init();
			}

			Key key = new SecretKeySpec(keyBytes, "AES");
			try
			{
				IvParameterSpec iv = new IvParameterSpec(IVBytes);//浣跨敤CBC妯″紡锛岄渶瑕佷竴涓悜閲廼v锛屽彲澧炲姞鍔犲瘑绠楁硶鐨勫己搴�� 
				cipher.init(Cipher.DECRYPT_MODE, key,iv);
				originBytes = cipher.doFinal(content);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return originBytes;
		}

}
