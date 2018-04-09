package edu.nju.encryptdroid.utils;

import java.io.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

public class EncDec {
	
	public static void encrypt(String key, String input, String output) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
	    // Here you read the cleartext.
	    FileInputStream fis = new FileInputStream(input);
	    // This stream write the encrypted text. This stream will be wrapped by another stream.
	    FileOutputStream fos = new FileOutputStream(output);

	    // Length is 16 byte
	    SecretKeySpec sks = new SecretKeySpec(key.getBytes(), "AES");
	    // Create cipher
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    
	    byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	    IvParameterSpec ivspec = new IvParameterSpec(iv);
	    
	    cipher.init(Cipher.ENCRYPT_MODE, sks, ivspec);
	    // Wrap the output stream
	    CipherOutputStream cos = new CipherOutputStream(fos, cipher);
	    // Write bytes
	    int b;
	    byte[] d = new byte[8];
	    while((b = fis.read(d)) != -1) {
	        cos.write(d, 0, b);
	    }
	    // Flush and close streams.
	    cos.flush();
	    cos.close();
	    fis.close();
	}
	
	public static void decrypt(String key, String input, String output) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
	    FileInputStream fis = new FileInputStream(input);

	    FileOutputStream fos = new FileOutputStream(output);
	    SecretKeySpec sks = new SecretKeySpec(key.getBytes(), "AES");
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	    IvParameterSpec ivspec = new IvParameterSpec(iv);
	    cipher.init(Cipher.DECRYPT_MODE, sks, ivspec);
	    CipherInputStream cis = new CipherInputStream(fis, cipher);
	    int b;
	    byte[] d = new byte[8];
	    while((b = cis.read(d)) != -1) {
	        fos.write(d, 0, b);
	    }
	    fos.flush();
	    fos.close();
	    cis.close();
	}

	static void decryptStream(String key, InputStream fis, FileOutputStream fos) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
	    //FileInputStream fis = new FileInputStream(input);
	    //FileOutputStream fos = new FileOutputStream(output);

	    SecretKeySpec sks = new SecretKeySpec(key.getBytes(), "AES");
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	    IvParameterSpec ivspec = new IvParameterSpec(iv);
	    cipher.init(Cipher.DECRYPT_MODE, sks, ivspec);
	    CipherInputStream cis = new CipherInputStream(fis, cipher);
	    int b;
	    byte[] d = new byte[8];
	    while((b = cis.read(d)) != -1) {
	        fos.write(d, 0, b);
	    }
	    fos.flush();
	    //fos.close();
	    //cis.close();
	}
	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			

		encrypt("rutgers!rutgers!", "amazed.apk", "amazed_enc.apk");
		decrypt("rutgers!rutgers!", "amazed_enc.apk", "amazed_dec.apk");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	*/

}
