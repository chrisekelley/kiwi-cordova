package org.rti.kidsthrive.secugenplugin;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import sourceafis.simple.AfisEngine;
import sourceafis.simple.Person;
import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFingerInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.test.AndroidTestCase;
import android.hardware.usb.UsbManager;
import android.util.Log;

//public class IdentifyTest extends AndroidTestCase {

@RunWith(RobolectricTestRunner.class)
public class IdentifyTest  {
	
	private static final String TAG = "IdentifyTest";

//	SecugenPlugin sctest;

//    protected void setUp () throws Exception {
//        sctest = new SecugenPlugin();       
//        super.setUp();
//    }

	@Test
	public final void testIdentify() {
		/*
	     * Create AFIS Engine and set the Threshold
	     */
		AfisEngine afis = new AfisEngine();
		afis.setThreshold(12);
		
		ScanProperties props = new ScanProperties(260, 300);
		File register = new File("/Users/chrisk/Downloads/scans3/register-sourceafis.txt");
		byte[] registerByte = null;
		try {
			registerByte = FileUtils.readFileToByteArray(register);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File verify = new File("/Users/chrisk/Downloads/scans3/verify-sourceafis.txt");
		byte[] verifyByte = null;
		try {
			verifyByte = FileUtils.readFileToByteArray(verify);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Person registerPerson = SourceAfisUtils.newPersonFromTemplate(afis, props, registerByte, null);
		Person verifyPerson = SourceAfisUtils.newPersonFromTemplate(afis, props, verifyByte, null);
//		boolean isMatched = SourceAfisUtils.identify(afis, props, registerByte, verifyByte, null);
		ArrayList<Person> database = new ArrayList<Person>();
		database.add(registerPerson);
		Iterable<Person> matches=afis.identify(verifyPerson, database);

		int i=0;
		for(Person match:matches){
			System.out.println("Matched::"+match.getId());
			i++;
		}
		boolean isMatched = false;
		if (i > 0) {
			isMatched = true;
		}
		assertTrue("Register and verify templates match.", isMatched);
	}
	
	@Test
	public final void populateDb() {
		/*
	     * Create AFIS Engine and set the Threshold
	     */
		AfisEngine afis = new AfisEngine();
		afis.setThreshold(12);
		
		ScanProperties props = new ScanProperties(260, 300);
		ArrayList<Person> database = new ArrayList<Person>();
		
		 File dir = new File("/sdcard/Download/scans6");
		 File[] directoryListing = dir.listFiles();
		 
		  if (directoryListing != null) {
		    for (File file : directoryListing) {
				System.out.println("file::"+ file.getName());
//				if (!file.getName().equals(".DS_Store")) {
				if (".DS_Store".equals(file.getName())) {
				} else {
					byte[] fileByte = null;
					try {
						fileByte = FileUtils.readFileToByteArray(file);
//						File verify = new File("/Users/chrisk/Downloads/scans3/verify-sourceafis.txt");
//						fileByte = FileUtils.readFileToByteArray(verify);
						Person person = SourceAfisUtils.newPersonFromTemplate(afis, props, fileByte, file.getName());
						database.add(person);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		    }
		  }
		  
		  File verify = new File("/sdcard/Download/verify-sourceafis1400062805904.txt");
			byte[] verifyByte = null;
			try {
				verifyByte = FileUtils.readFileToByteArray(verify);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Person verifyPerson = SourceAfisUtils.newPersonFromTemplate(afis, props, verifyByte, null);
//			Iterable<Person> matches=afis.identify(verifyPerson, database);

//			int i=0;
//			for(Person match:matches){
//				System.out.println("Matched::"+match.getId());
//				i++;
//			}
			
			int i=0;
			for(Person candidate:database){
				float match=afis.verify(verifyPerson, candidate);
//						identify(verifyPerson, database);
				System.out.println("Matched:"+match);
				i++;
			}
			boolean isMatched = false;
			if (i > 0) {
				isMatched = true;
			}
			assertTrue("Register and verify templates match.", isMatched);
	}
	
	@Test
	public final void testTemplateConcat() {
		byte[] fileByte = null;
		String filePath = "/Users/chrisk/Downloads/register.template-1411727377663.txt";
		File file = new File(filePath);
		try {
			fileByte = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int templateSize = 96;
		int fileSize = fileByte.length;
		System.out.println("fileByte: " + fileSize);
		byte[] newTemplate = Arrays.copyOfRange(fileByte, 0, templateSize);
		System.out.println("newTemplate: " + newTemplate.length);
		String templateString = Utils.encodeHexToString(newTemplate, Utils.DIGITS_UPPER);
		System.out.println("templateString: " + templateString);
//		SGFingerInfo finger_info = new SGFingerInfo();
//		byte[] mRegisterTemplate;
//		for (int i=0; i< mRegisterTemplate.length; ++i)
//        	mRegisterTemplate[i] = 0;
//		JSGFPLib sgfplib = new JSGFPLib((UsbManager)context.getSystemService(Context.USB_SERVICE));
//		byte[] mRegisterImage = new byte[mImageWidth*mImageHeight];
//        sgfplib.CreateTemplate( finger_info , mRegisterImage, mRegisterTemplate );
//        int[] templateSize = null;
//        sgfplib.GetTemplateSize(mRegisterTemplate, templateSize);

		
	}
	
	@Test
	public final void testImages() {
		/*
		 * Create AFIS Engine and set the Threshold
		 */
		AfisEngine afis = new AfisEngine();
		afis.setThreshold(12);
		
		ArrayList<Person> database = new ArrayList<Person>();
		
		File dir = new File("/sdcard/Download/tifs");
		File[] directoryListing = dir.listFiles();
		
		if (directoryListing != null) {
			for (File file : directoryListing) {
				System.out.println("file::"+ file.getName());
//				if (!file.getName().equals(".DS_Store")) {
				if (".DS_Store".equals(file.getName())) {
				} else {
					byte[] fileByte = null;
					try {
						fileByte = FileUtils.readFileToByteArray(file);
//						File verify = new File("/Users/chrisk/Downloads/scans3/verify-sourceafis.txt");
//						fileByte = FileUtils.readFileToByteArray(verify);
						String path = file.getAbsolutePath();
						System.out.println("file path:"+ path);
						InputStream in = null;
						try {
							in = new BufferedInputStream(new FileInputStream(file));
							Bitmap  bmp = BitmapFactory.decodeStream(in);
							int width          = bmp.getWidth();
							int height         = bmp.getHeight();
							ScanProperties props = new ScanProperties(width, height);
							Person person = SourceAfisUtils.generateTemplate(afis, props, fileByte, file.getName() + ".txt");
							database.add(person);
						}
						finally {
							if (in != null) {
								in.close();
							}
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
//		File verify = new File("/sdcard/Download/verify-sourceafis1400062805904.txt");
//		File file = new File("/sdcard/Download/tifs/101_2.tif");
//		File file = new File("/sdcard/Download/tifs/register-1400078056518.png");
		File file = new File("/sdcard/Download/tifs/register-1400078905157.png");
		byte[] verifyByte = null;
		try {
			verifyByte = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ScanProperties props = null;
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			Bitmap  bmp = BitmapFactory.decodeStream(in);
			int width          = bmp.getWidth();
			int height         = bmp.getHeight();
			props = new ScanProperties(width, height);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
//		Person verifyPerson = SourceAfisUtils.newPersonFromTemplate(afis, props, verifyByte, null);
		Person verifyPerson = SourceAfisUtils.generateTemplate(afis, props, verifyByte, file.getName() + ".txt");
//			Iterable<Person> matches=afis.identify(verifyPerson, database);
		
//			int i=0;
//			for(Person match:matches){
//				System.out.println("Matched::"+match.getId());
//				i++;
//			}
		
		int i=0;
		for(Person candidate:database){
			float match=afis.verify(verifyPerson, candidate);
//						identify(verifyPerson, database);
			System.out.println("Matched:"+match);
			i++;
		}
		boolean isMatched = false;
		if (i > 0) {
			isMatched = true;
		}
		assertTrue("Register and verify templates match.", isMatched);
	}
	
	

}
