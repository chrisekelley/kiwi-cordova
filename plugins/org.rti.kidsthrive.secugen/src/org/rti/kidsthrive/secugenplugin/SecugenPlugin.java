package org.rti.kidsthrive.secugenplugin;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGFingerPosition;
import SecuGen.FDxSDKPro.SGImpressionType;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.EditText;

/**
 * This class echoes a string called from JavaScript.
 */
public class SecugenPlugin extends CordovaPlugin {
	
	static final String TAG = "SecuGen USB";
	private static String templatePath = "/sdcard/Download/fprints/";
	private static String serverUrl = "";
	private static String serverUrlFilepath = "";
	private static String serverKey = "";
	private static String templateFormat = "";
	
	// actions
    private static final String ACTION_REQUEST_PERMISSION = "requestPermission";
    private static final String CONNECT = "connect";
    private static final String CONNECT_INSECURE = "connectInsecure";
    private static final String DISCONNECT = "disconnect";
    private static final String WRITE = "write";
    private static final String AVAILABLE = "available";
    private static final String READ = "read";
    private static final String READ_UNTIL = "readUntil";
    private static final String SUBSCRIBE = "subscribe";
    private static final String UNSUBSCRIBE = "unsubscribe";
    private static final String IS_ENABLED = "isEnabled";
    private static final String IS_CONNECTED = "isConnected";
    private static final String CLEAR = "clear";
    private static final String COOLMETHOD = "coolMethod";
    private static final String REGISTER = "register";
    private static final String IDENTIFY = "identify";
    private static final String CAPTURE = "capture";
    private static final String BLINK = "blink";
    private static final String VERIFY = "verify";
    
//	private Button mCapture;
//    private Button mButtonRegister;
//    private Button mButtonMatch;
//    private Button mButtonLed;
//    private Button mSDKTest;
    private EditText mEditLog;
//    private android.widget.TextView mTextViewResult;
//    private android.widget.CheckBox mCheckBoxMatched;
//    private android.widget.CheckBox mCheckBoxSCEnabled;
    private PendingIntent mPermissionIntent;
//    private ImageView mImageViewFingerprint;
//    private ImageView mImageViewRegister;
//    private ImageView mImageViewVerify;
    private byte[] mRegisterImage;
    private byte[] mVerifyImage;
    private byte[] mRegisterTemplate;
    private byte[] mVerifyTemplate;
	private int[] mMaxTemplateSize;
	private int mImageWidth;
	private int mImageHeight;
	private int[] grayBuffer;
    private Bitmap grayBitmap;
    private boolean mLed;
   
    private JSGFPLib sgfplib;
    
    private Context context;
    
	long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
	// UsbManager instance to deal with permission and opening
    private UsbManager manager;
    
//    static ArrayList<Person> database = null;
//    private AfisEngine afis;
    private ScanProperties props;

   
	/**
	 * Initialize the Plugin, Cordova handles this.
	 * 
	 * @param cordova	Used to get register Handler with the Context accessible from this interface 
	 * @param view		Passed straight to super's initialization.
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView view)
	{
		super.initialize(cordova, view);
		
		context = cordova.getActivity().getBaseContext();
//
//	    Properties properties = new Properties();
//
//		try {
//			InputStream rawResource = context.getResources().getValue("");
////					openRawResource(R.values.strings);
//			properties.load(rawResource);
//			System.out.println("The properties are now loaded");
//			System.out.println("properties: " + properties);
//		} catch (Resources.NotFoundException e) {
//			System.err.println("Did not find raw resource: " + e);
//		} catch (IOException e) {
//			System.err.println("Failed to open property file");
//		}
	    
//	    	String templatePathTemp = properties.getProperty("templatePath");
//		System.out.println("context.getApplicationInfo().className: " + context.getApplicationInfo().className);
		System.out.println("this.cordova.getActivity().getPackageName(): " + this.cordova.getActivity().getPackageName());
//		int id = context.getResources().getIdentifier("strings", "values", this.cordova.getActivity().getPackageName());
		int id = context.getResources().getIdentifier("templatePath", "string", this.cordova.getActivity().getPackageName());
    	System.out.println("templatePath id: " + id);
    	String translatedValue = context.getResources().getString(id);
    	System.out.println("translatedValue: " + translatedValue);
    	File templatePathFile = new File(templatePath);
    	templatePathFile.mkdirs();
    	SecugenPlugin.setTemplatePath(translatedValue);
    	id = context.getResources().getIdentifier("serverUrl", "string", this.cordova.getActivity().getPackageName());
    	System.out.println("serverUrl id: " + id);
    	String serverUrl = context.getResources().getString(id);
    	System.out.println("serverUrl: " + serverUrl);
    	SecugenPlugin.setServerUrl(serverUrl);
    	id = context.getResources().getIdentifier("serverKey", "string", this.cordova.getActivity().getPackageName());
    	String serverKey = context.getResources().getString(id);
    	System.out.println("serverKey: " + serverKey);
    	SecugenPlugin.setServerKey(serverKey);
    	id = context.getResources().getIdentifier("templateFormat", "string", this.cordova.getActivity().getPackageName());
    	String templateFormat = context.getResources().getString(id);
    	System.out.println("templateFormat: " + templateFormat);
    	SecugenPlugin.setTemplateFormat(templateFormat);
    	
    	id = context.getResources().getIdentifier("serverUrlFilepath", "string", this.cordova.getActivity().getPackageName());
    	System.out.println("serverUrlFilepath id: " + id);
    	String serverUrlFilepath = context.getResources().getString(id);
    	System.out.println("serverUrlFilepath: " + serverUrlFilepath);
    	SecugenPlugin.setServerUrlFilepath(serverUrlFilepath);
	}
	
//	if (message != null && message.length() > 0) {
//        callbackContext.success(message);
//    } else {
//        callbackContext.error("Expected one non-empty string argument.");
//    }
	


    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    	mMaxTemplateSize = new int[1];

    	LOG.d(TAG, "action = " + action);

    	boolean validAction = true;

    	//USB Permissions
    	mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
    	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    	context.registerReceiver(mUsbReceiver, filter);

    	// request permission
    	if (action.equals(ACTION_REQUEST_PERMISSION)) {
    		debugMessage("action: " + action);
    		this.requestPermission(callbackContext);
    		return true;
    	} else if (action.equals(COOLMETHOD)) {
    		String message = args.getString(0);
    		this.coolMethod(message, callbackContext);
    		//            return true;
    		validAction = true;

    	} else if (action.equals(REGISTER)) {

    		//          listBondedDevices(callbackContext);
    		//        	register(callbackContext);

    		cordova.getActivity().runOnUiThread(new Runnable() {
    			public void run() {
    				register(callbackContext);
    			}
    		});
    		return true;
    		
    	} else if (action.equals(IDENTIFY)) {
    		
    		//          listBondedDevices(callbackContext);
    		//        	register(callbackContext);
    		
    		cordova.getActivity().runOnUiThread(new Runnable() {
    			public void run() {
    				identify(callbackContext);
    			}
    		});
    		return true;

    	} else if (action.equals(CAPTURE)) {

    		//          boolean secure = true;
    		//          connect(args, secure, callbackContext);
    		capture(callbackContext);

    	} else if (action.equals(BLINK)) {

    		//          boolean secure = true;
    		//          connect(args, secure, callbackContext);
    		blink(callbackContext);

    	} else if (action.equals(VERIFY)) {

    		//          boolean secure = true;
    		//          connect(args, secure, callbackContext);
    		verify(callbackContext);

    	} else if (action.equals(CONNECT_INSECURE)) {

    		// see Android docs about Insecure RFCOMM http://goo.gl/1mFjZY
    		//          boolean secure = false;
    		//          connect(args, false, callbackContext);

    	} else if (action.equals(DISCONNECT)) {

    		//          connectCallback = null;
    		//          bluetoothSerialService.stop();
    		//          callbackContext.success();

    	} else if (action.equals(WRITE)) {

    		//          String data = args.getString(0);
    		//          bluetoothSerialService.write(data.getBytes());
    		//          callbackContext.success();

    	} else if (action.equals(AVAILABLE)) {

    		//          callbackContext.success(available());

    	} else if (action.equals(READ)) {

    		//          callbackContext.success(read());

    	} else if (action.equals(READ_UNTIL)) {

    		//          String interesting = args.getString(0);
    		//          callbackContext.success(readUntil(interesting));

    	} else if (action.equals(SUBSCRIBE)) {

    		//          delimiter = args.getString(0);
    		//          dataAvailableCallback = callbackContext;
    		//
    		//          PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    		//          result.setKeepCallback(true);
    		//          callbackContext.sendPluginResult(result);

    	} else if (action.equals(UNSUBSCRIBE)) {

    		//          delimiter = null;
    		//          dataAvailableCallback = null;
    		//
    		//          callbackContext.success();

    	} else if (action.equals(IS_ENABLED)) {

    		//          if (bluetoothAdapter.isEnabled()) {
    		//              callbackContext.success();                
    		//          } else {
    		//              callbackContext.error("Bluetooth is disabled.");
    		//          }            

    	} else if (action.equals(IS_CONNECTED)) {

    		//          if (bluetoothSerialService.getState() == BluetoothSerialService.STATE_CONNECTED) {
    		//              callbackContext.success();                
    		//          } else {
    		//              callbackContext.error("Not connected.");
    		//          }

    	} else if (action.equals(CLEAR)) {

    		//          buffer.setLength(0);
    		//          callbackContext.success();

    	} else {

    		//          validAction = false;

    	}

//    	PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
//    	result.setKeepCallback(true);
//    	callbackContext.sendPluginResult(result);

    	return validAction;
    }
    

	/**
	 * @param usbDevice
	 */
	public void requestPermission(final CallbackContext callbackContext) {
		debugMessage("requestPermission.");
		
		debugMessage("Getting ACTION_USB_PERMISSION \n");
        sgfplib = new JSGFPLib((UsbManager)context.getSystemService(Context.USB_SERVICE));
//    	this.mCheckBoxSCEnabled.setChecked(true);
		debugMessage("jnisgfplib version: " + sgfplib.Version() + "\n");
		mLed = false;
		//		        	sgfplib.writeData((byte)5, (byte)0);
		
		long error = sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO);
		if (error != SGFDxErrorCode.SGFDX_ERROR_NONE){
			if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND) {
				String message = "Either a fingerprint device is not attached or the attached fingerprint device is not supported.";
				debugMessage(message);
				callbackContext.error(message);
			} else {
				String message = "Fingerprint device initialization failed!";
				debugMessage(message);     
				callbackContext.error(message);
			}
		}
		else {
			final UsbDevice usbDevice = sgfplib.GetUsbDevice();
			if (usbDevice == null){
				String message = "SDU04P or SDU03P fingerprint sensor not found!";
				debugMessage(message);
				callbackContext.error(message);
			}
			cordova.getThreadPool().execute(new Runnable() {
	            public void run() {
	            	long error;
//	            	sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
	            	// get UsbManager from Android
	                manager = (UsbManager) cordova.getActivity().getSystemService(Context.USB_SERVICE);
	             // finally ask for the permission
	                debugMessage("Requesting Permission from UsbManager.");
                    manager.requestPermission(usbDevice, mPermissionIntent);
	    			error = sgfplib.OpenDevice(0);
	    			debugMessage("OpenDevice() ret: " + error + "\n");
	    			SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
	    			error = sgfplib.GetDeviceInfo(deviceInfo);
	    			debugMessage("GetDeviceInfo() ret: " + error + "\n");
	    			mImageWidth = deviceInfo.imageWidth;
	    			mImageHeight= deviceInfo.imageHeight;
	    			debugMessage("Setting props: mImageWidth: " + mImageWidth + " mImageHeight: " + mImageHeight);
	    			props = new ScanProperties(mImageWidth, mImageHeight);
//	    			sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
//	    			sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
	    			Field fieldName;
					try {
						fieldName = SGFDxTemplateFormat.class.getField(SecugenPlugin.getTemplateFormat());
		    			short templateValue = fieldName.getShort(null);
		    			debugMessage("templateValue: " + templateValue);
						sgfplib.SetTemplateFormat(templateValue);
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
	    			debugMessage("mMaxTemplateSize: " + mMaxTemplateSize[0] + "\n");
	    			mRegisterTemplate = new byte[mMaxTemplateSize[0]];
	    			mVerifyTemplate = new byte[mMaxTemplateSize[0]];
	    			sgfplib.writeData((byte)5, (byte)1);
	    			callbackContext.success("Fingerprint scanner initialised.");

	            }
			 });
			
		}
	}

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    private void register(final CallbackContext callbackContext) {
        debugMessage("Clicked REGISTER\n");
        int[] templateSize = captureImageTemplate();
//		UUID uuid = UUID.randomUUID();
		String urlServer = SecugenPlugin.getServerUrl() + SecugenPlugin.getServerUrlFilepath() + "Enroll";
		buildUploadMessage(callbackContext, templateSize, urlServer);
        createImageFile(callbackContext);
    }
    
    private void identify(final CallbackContext callbackContext) {
    	debugMessage("Clicked identify\n");
    	int[] templateSize = captureImageTemplate();
//		UUID uuid = UUID.randomUUID();
    	String urlServer = SecugenPlugin.getServerUrl() + SecugenPlugin.getServerUrlFilepath() + "Identify";
    	buildUploadMessage(callbackContext, templateSize, urlServer);
    	createImageFile(callbackContext);
    }

	/**
	 * @return
	 */
	public int[] captureImageTemplate() {
		if (mRegisterImage != null)
        	mRegisterImage = null;
        mRegisterImage = new byte[mImageWidth*mImageHeight];
//    	this.mCheckBoxMatched.setChecked(false);
        dwTimeStart = System.currentTimeMillis();          
        long result = sgfplib.GetImage(mRegisterImage);
        Utils.DumpFile("register" + System.currentTimeMillis() +".raw", mRegisterImage);
        SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
		sgfplib.GetDeviceInfo(deviceInfo);
        mImageWidth = deviceInfo.imageWidth;
		mImageHeight= deviceInfo.imageHeight;
		int dpi = deviceInfo.imageDPI;
		debugMessage("mImageWidth: " + mImageWidth);
		debugMessage("mImageHeight: " + mImageHeight);
		debugMessage("dpi: " + dpi);
//		afis.setDpi(dpi);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        dwTimeStart = System.currentTimeMillis();
        
        // Create template from captured image
        SGFingerInfo finger_info = new SGFingerInfo();
        debugMessage("CreateTemplate() started \n");
        for (int i=0; i< mRegisterTemplate.length; ++i)
        	mRegisterTemplate[i] = 0;
        result = sgfplib.CreateTemplate( finger_info , mRegisterImage, mRegisterTemplate );
        int[] templateSize = new int[1];
        sgfplib.GetTemplateSize(mRegisterTemplate, templateSize);
        debugMessage("templateSize: " + templateSize[0]);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("CreateTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        String templatefileName = "register.template-" + System.currentTimeMillis() + ".txt";
        Utils.DumpFile(templatefileName, mRegisterTemplate);
        final String templatePath = SecugenPlugin.getTemplatePath() + templatefileName;
		return templateSize;
	}

	/**
	 * @param callbackContext
	 * @param templateSize
	 * @param url TODO
	 */
	public void buildUploadMessage(final CallbackContext callbackContext,
			int[] templateSize, final String url) {
		String templateString;
		byte[] newTemplate = Arrays.copyOfRange(mRegisterTemplate, 0, templateSize[0]);
		templateString = Utils.encodeHexToString(newTemplate, Utils.DIGITS_UPPER);
		Log.d(TAG, "templateString: " + templateString);
		
		final JSONObject jo = new JSONObject();
		try {
			jo.put("Key", SecugenPlugin.getServerKey());
			jo.put("Name", "Test CK");
			jo.put("Template", templateString);
			jo.put("Finger", 1);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		{"Key":"sample string 1","Template":"sample string 2","Finger":3}
		debugMessage("Send to the server: : " + jo.toString());

        dwTimeStart = System.currentTimeMillis();  
        final String uploadMessage = "";
			Thread thread = new Thread(new Runnable(){
			    @Override
			    public void run() {
			    	JSONObject serviceResponse = null;
			        try {
			        	serviceResponse = Utils.post(jo, url);
			        	String serviceResponseStr = serviceResponse.toString();
//			        	Log.d(TAG, "serviceResponseStr: " + serviceResponseStr);
						PluginResult result = new PluginResult(PluginResult.Status.OK, serviceResponseStr);
			        	result.setKeepCallback(true);
//						callbackContext.success(uploadMessage);
			        	callbackContext.sendPluginResult(result);
			        } catch (Exception e) {
			            e.printStackTrace();
			            callbackContext.error("Upload Error: " + serviceResponse + " Error: " + e);
			        }
			    }
			});
			thread.start(); 
		dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("uploadMessage() ret:" + uploadMessage + " [" + dwTimeElapsed + "ms]\n");
	}

	/**
	 * @param callbackContext
	 */
	public void createImageFile(final CallbackContext callbackContext) {
        ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
		Bitmap b = Bitmap.createBitmap(mImageWidth,mImageHeight, Bitmap.Config.ARGB_8888);
        byteBuf.put(mRegisterImage);
        int[] intbuffer = new int[mImageWidth*mImageHeight];
        for (int i=0; i<intbuffer.length; ++i)
        	intbuffer[i] = (int) mRegisterImage[i];
        b.setPixels(intbuffer, 0, mImageWidth, 0, 0, mImageWidth, mImageHeight); 
        //DEBUG Log.d(TAG, "Show Register image");
//        mImageViewFingerprint.setImageBitmap(this.toGrayscale(b));  
        final String registrationFile = "register-" + System.currentTimeMillis();
		final String outputFilename = Utils.saveImageFile(callbackContext, b, registrationFile);
	}

	/**
	 * @param b
	 * @return
	 */
	public ScanProperties createScanProperties(Bitmap b) {
		int width          = b.getWidth();
		int height         = b.getHeight();
		ScanProperties props = new ScanProperties(width, height);
		return props;
	}
    
    private void capture(CallbackContext callbackContext) {
    	
    	Log.d(TAG, "Pressed CAPTURE");
//    	this.mCheckBoxMatched.setChecked(false);
        byte[] buffer = new byte[mImageWidth*mImageHeight];
        ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
        dwTimeStart = System.currentTimeMillis();          
        long result = sgfplib.GetImage(buffer);
        Utils.DumpFile("capture.raw", buffer);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("getImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
//    	mTextViewResult.setText("getImage() ret: " + result + " [" + dwTimeElapsed + "ms]\n");  
        Bitmap b = Bitmap.createBitmap(mImageWidth,mImageHeight, Bitmap.Config.ARGB_8888);
        b.setHasAlpha(false);
//    	if(false)
        byteBuf.put(buffer);
        int[] intbuffer = new int[mImageWidth*mImageHeight];
        for (int i=0; i<intbuffer.length; ++i)
        	intbuffer[i] = (int) buffer[i];
        b.setPixels(intbuffer, 0, mImageWidth, 0, 0, mImageWidth, mImageHeight);
        //DEBUG Log.d(TAG, "Show image");
//        mImageViewFingerprint.setImageBitmap(this.toGrayscale(b));  
        Utils.saveImageFile(callbackContext, b, "current-" + System.currentTimeMillis());
    }

	private void blink(CallbackContext callbackContext) {
//    	this.mCheckBoxMatched.setChecked(false);
    	mLed = !mLed;
        dwTimeStart = System.currentTimeMillis();          
        long result = sgfplib.SetLedOn(mLed);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("setLedOn(" + mLed +") ret:" + result + " [" + dwTimeElapsed + "ms]\n");
		callbackContext.success("Blink LED:" + result);
    }
    
    private void verify(CallbackContext callbackContext) {
    	//DEBUG Log.d(TAG, "Clicked MATCH");
        debugMessage("Clicked MATCH\n");
        if (mVerifyImage != null)
        	mVerifyImage = null;
        mVerifyImage = new byte[mImageWidth*mImageHeight];
        ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
        dwTimeStart = System.currentTimeMillis();          
        long result = sgfplib.GetImage(mVerifyImage);
        Utils.DumpFile("verify.raw", mVerifyImage);
        SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
		sgfplib.GetDeviceInfo(deviceInfo);
        mImageWidth = deviceInfo.imageWidth;
		mImageHeight= deviceInfo.imageHeight;
		int dpi = deviceInfo.imageDPI;
		debugMessage("mImageWidth: " + mImageWidth);
		debugMessage("mImageHeight: " + mImageHeight);
		debugMessage("dpi: " + dpi);
//		afis.setDpi(dpi);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        Bitmap b = Bitmap.createBitmap(mImageWidth,mImageHeight, Bitmap.Config.ARGB_8888);
        byteBuf.put(mVerifyImage);
        int[] intbuffer = new int[mImageWidth*mImageHeight];
        for (int i=0; i<intbuffer.length; ++i)
        	intbuffer[i] = (int) mVerifyImage[i];
        b.setPixels(intbuffer, 0, mImageWidth, 0, 0, mImageWidth, mImageHeight); 
        String verifyFilename = "verify-" + System.currentTimeMillis();
        final String outputFilename = Utils.saveImageFile(callbackContext, b, verifyFilename);
        String uploadMessage = "";
		try {
			uploadMessage = Utils.upload(outputFilename);
			callbackContext.success("Match Results" + uploadMessage);
	        debugMessage("Match Results" + uploadMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			callbackContext.error("Match Results" + uploadMessage + " Error: " + e);
		}
        
    }

//	/*
//	 * Utility function to create a person from finger print template.
//	 */
//	static Person getPerson(int id,byte[][] template) throws IOException {
//		Fingerprint arrFp[] = new Fingerprint[template.length];
//		for(int x=0;x<template.length;x++){	
//			arrFp[x] = new Fingerprint();
//			arrFp[x].setIsoTemplate(template[x]);
//		}
//		Person p=new Person(arrFp);
//		p.setId(id);
//		return p;
//	}
//	
//	static Person getPerson(int id,byte[] template) throws IOException {
//		Fingerprint arrFp[] = new Fingerprint[1];
//		arrFp[0] = new Fingerprint();
//		arrFp[0].setIsoTemplate(template);
//		Person p=new Person(arrFp);
//		p.setId(id);
//		return p;
//	}
    
    private void debugMessage(String message) {
    	//      this.mEditLog.append(message);
    	//      this.mEditLog.invalidate(); //TODO trying to get Edit log to update after each line written
    	Log.d(TAG, message);
    }

    //This broadcast receiver is necessary to get user permissions to access the attached USB device
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		//DEBUG Log.d(TAG,"Enter mUsbReceiver.onReceive()");
    		if (ACTION_USB_PERMISSION.equals(action)) {
    			synchronized (this) {
    				UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
    					if(device != null){
    						//DEBUG Log.d(TAG, "Vendor ID : " + device.getVendorId() + "\n");
    						//DEBUG Log.d(TAG, "Product ID: " + device.getProductId() + "\n");
    						debugMessage("Vendor ID : " + device.getVendorId() + "\n");
    						debugMessage("Product ID: " + device.getProductId() + "\n");
    					}
    					else
    						Log.e(TAG, "mUsbReceiver.onReceive() Device is null");    						
    				} 
    				else
    					Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device " + device);    				
    			}
    		}
    	}
    };  
    
    protected void finalize() throws Throwable {
    	try {
    		Log.e(TAG, "finalize");
			context.unregisterReceiver(mUsbReceiver);
		} catch (Exception e1) {
			Log.e(TAG, "", e1);
		}  finally {
	        super.finalize();
		}
    }
    
    @Override
    public void onDestroy() {
        debugMessage("onDestroy.");
    	try {
    		Log.e(TAG, "unregisterReceiver mUsbReceiver");
			context.unregisterReceiver(mUsbReceiver);
		} catch (Exception e1) {
			Log.e(TAG, "", e1);
		}  finally {
		}
    	sgfplib.CloseDevice();
    	mRegisterImage = null;
    	mVerifyImage = null;
    	mRegisterTemplate = null;
    	mVerifyTemplate = null;
    	sgfplib.Close();
        super.onDestroy();
    }
    
//    public static ArrayList<Person> getDatabase() {
//		return database;
//	}
//
//	public static void setDatabase(ArrayList<Person> database) {
//		SecugenPlugin.database = database;
//	}

	public static String getTemplatePath() {
		return templatePath;
	}

	public static void setTemplatePath(String templatePath) {
		SecugenPlugin.templatePath = templatePath;
	}

	public static String getServerUrl() {
		return serverUrl;
	}

	public static void setServerUrl(String serverUrl) {
		SecugenPlugin.serverUrl = serverUrl;
	}

	public static String getServerUrlFilepath() {
		return serverUrlFilepath;
	}

	public static void setServerUrlFilepath(String serverUrlFilepath) {
		SecugenPlugin.serverUrlFilepath = serverUrlFilepath;
	}

	public static String getServerKey() {
		return serverKey;
	}

	public static void setServerKey(String serverKey) {
		SecugenPlugin.serverKey = serverKey;
	}

	public static String getTemplateFormat() {
		return templateFormat;
	}

	public static void setTemplateFormat(String templateFormat) {
		SecugenPlugin.templateFormat = templateFormat;
	}

}
