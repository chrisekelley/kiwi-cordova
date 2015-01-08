package org.rti.kidsthrive.secugenplugin;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class SecugenPlugin extends CordovaPlugin {
	
	static final String TAG = "SecuGen USB";
	private static String templatePath = "/sdcard/Download/fprints/";
	private static String serverUrl = "";
	private static String serverUrlFilepath = "";
	private static String serverKey = "";
	private static String projectName = "";
	private static String templateFormat = "";
	
	// actions
    private static final String ACTION_REQUEST_PERMISSION = "requestPermission";
    private static final String COOLMETHOD = "coolMethod";
    private static final String REGISTER = "register";
    private static final String IDENTIFY = "identify";
    private static final String CAPTURE = "capture";
    private static final String BLINK = "blink";
    private static final String VERIFY = "verify";
    private static final String SCAN = "scan";
    private byte[] mRegisterImage;
    private byte[] mVerifyImage;
    private byte[] mRegisterTemplate;
	private int[] mMaxTemplateSize;
	private int mImageWidth;
	private int mImageHeight;
    private boolean mLed;
   
    private JSGFPLib sgfplib;
    
    private Context context;
    
	long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
	// UsbManager instance to deal with permission and opening
    private UsbManager manager;
   
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
		LOG.d(TAG,"this.cordova.getActivity().getPackageName(): " + this.cordova.getActivity().getPackageName());
		int id = context.getResources().getIdentifier("templatePath", "string", this.cordova.getActivity().getPackageName());
    	LOG.d(TAG,"templatePath id: " + id);
    	String translatedValue = context.getResources().getString(id);
    	LOG.d(TAG,"translatedValue: " + translatedValue);
    	File templatePathFile = new File(templatePath);
    	templatePathFile.mkdirs();
    	SecugenPlugin.setTemplatePath(translatedValue);
    	id = context.getResources().getIdentifier("serverUrl", "string", this.cordova.getActivity().getPackageName());
    	LOG.d(TAG,"serverUrl id: " + id);
    	String serverUrl = context.getResources().getString(id);
    	LOG.d(TAG,"serverUrl: " + serverUrl);
    	SecugenPlugin.setServerUrl(serverUrl);
    	id = context.getResources().getIdentifier("serverKey", "string", this.cordova.getActivity().getPackageName());
    	String serverKey = context.getResources().getString(id);
    	LOG.d(TAG,"serverKey: " + serverKey);
    	SecugenPlugin.setServerKey(serverKey);
    	id = context.getResources().getIdentifier("projectName", "string", this.cordova.getActivity().getPackageName());
    	String projectName = context.getResources().getString(id);
    	LOG.d(TAG,"projectName: " + projectName);
    	SecugenPlugin.setProjectName(projectName);
    	id = context.getResources().getIdentifier("templateFormat", "string", this.cordova.getActivity().getPackageName());
    	String templateFormat = context.getResources().getString(id);
    	LOG.d(TAG,"templateFormat: " + templateFormat);
    	SecugenPlugin.setTemplateFormat(templateFormat);
    	id = context.getResources().getIdentifier("serverUrlFilepath", "string", this.cordova.getActivity().getPackageName());
    	LOG.d(TAG,"serverUrlFilepath id: " + id);
    	String serverUrlFilepath = context.getResources().getString(id);
    	LOG.d(TAG,"serverUrlFilepath: " + serverUrlFilepath);
    	SecugenPlugin.setServerUrlFilepath(serverUrlFilepath);
	}
	
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    	mMaxTemplateSize = new int[1];

    	LOG.d(TAG, "action = " + action);

    	boolean validAction = true;
    	// request permission
    	if (action.equals(ACTION_REQUEST_PERMISSION)) {
    		requestPermission(callbackContext);
    		return true;
    	} else if (action.equals(COOLMETHOD)) {
    		String message = args.getString(0);
    		this.coolMethod(message, callbackContext);
    		//            return true;
    		validAction = true;

    	} else if (action.equals(REGISTER)) {
    		cordova.getActivity().runOnUiThread(new Runnable() {
    			public void run() {
    				register(callbackContext);
    			}
    		});
    		return true;
    	} else if (action.equals(IDENTIFY)) {
    		cordova.getActivity().runOnUiThread(new Runnable() {
    			public void run() {
    				identify(callbackContext);
    			}
    		});
    		return true;
    	} else if (action.equals(SCAN)) {
    		cordova.getActivity().runOnUiThread(new Runnable() {
    			public void run() {
    				scan(callbackContext);
    			}
    		});
    		return true;
    	} else if (action.equals(CAPTURE)) {
    		capture(callbackContext);
    	} else if (action.equals(BLINK)) {
    		blink(callbackContext);
    	} else if (action.equals(VERIFY)) {
    		verify(callbackContext);
    	} 
    	return validAction;
    }
    

	 /**
     * Request permission the the user for the app to use the USB/serial port
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void requestPermission(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                // get UsbManager from Android
                manager = (UsbManager) cordova.getActivity().getSystemService(Context.USB_SERVICE);
                sgfplib = new JSGFPLib((UsbManager)context.getSystemService(Context.USB_SERVICE));
        		debugMessage("jnisgfplib version: " + sgfplib.Version() + "\n");
        		mLed = false;
        		//		        	sgfplib.writeData((byte)5, (byte)0);
        		
        		long error = sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO);
        		if (error != SGFDxErrorCode.SGFDX_ERROR_NONE){
        			String message = "Fingerprint device initialization failed!";
        			if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND) {
        				message = "Error: Either a fingerprint device is not attached or the attached fingerprint device is not supported.";
        			}
    				debugMessage(message);     
        			PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
                	result.setKeepCallback(true);
                	callbackContext.sendPluginResult(result);
        		}
//                if (!availableDrivers.isEmpty()) {
        		else {
                    // get the first one as there is a high chance that there is no more than one usb device attached to your android
//                    driver = availableDrivers.get(0);
//                    UsbDevice device = driver.getDevice();
        			UsbDevice usbDevice = sgfplib.GetUsbDevice();
        			if (usbDevice == null){
        				String message = "Error: Fingerprint sensor not found!";
        				debugMessage(message);
        				PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
                    	result.setKeepCallback(true);
                    	callbackContext.sendPluginResult(result);
        			}
                    // create the intent that will be used to get the permission
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, new Intent(UsbBroadcastReceiver.USB_PERMISSION), 0);
                    // and a filter on the permission we ask
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(UsbBroadcastReceiver.USB_PERMISSION);
                    // this broadcast receiver will handle the permission results
                    UsbBroadcastReceiver usbReceiver = new UsbBroadcastReceiver(callbackContext, cordova.getActivity());
                    cordova.getActivity().registerReceiver(usbReceiver, filter);
                    // finally ask for the permission
                    manager.requestPermission(usbDevice, pendingIntent);
                    initDeviceSettings();
                }
//                else {
//                    // no available drivers
//                    Log.d(TAG, "No device found!");
//                    callbackContext.error("No device found!");
//                }
            }
        });
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
        int[] templateSize;
		try {
			try {
				templateSize = captureImageTemplate();
				Log.d(TAG, "templateSize: " + templateSize[0]);
				if (templateSize == new int[0]) {
					Log.d(TAG, "captureImageTemplate Error: templateSize is 0 sized.");
					callbackContext.error("captureImageTemplate Error: templateSize is 0 sized.");
				}
				String urlServer = SecugenPlugin.getServerUrl() + SecugenPlugin.getServerUrlFilepath() + "Enroll";
				buildUploadMessage(callbackContext, templateSize, urlServer);
		        createImageFile(callbackContext);
			} catch (Exception e) {
				Log.d(TAG, "Caught from the exception: captureImageTemplate Error" + e);
				callbackContext.error("captureImageTemplate Error: " + e);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			callbackContext.error("captureImageTemplate Error: " + e);
		}
    }
    
    private void identify(final CallbackContext callbackContext) {
    	debugMessage("Clicked identify\n");
    	int[] templateSize;
		try {
			templateSize = captureImageTemplate();
			if (templateSize.length == 0) {
				String msg = "Scan failed: Unable to capture fingerprint. Please kill the app in the Task Manager and restart the app.";
				Log.d(TAG, msg);
				PluginResult result = new PluginResult(PluginResult.Status.ERROR, msg);
	        	result.setKeepCallback(true);
	        	callbackContext.sendPluginResult(result);
			} else {
				Log.d(TAG, "templateSize: " + templateSize[0]);
//				UUID uuid = UUID.randomUUID();
		    	String urlServer = SecugenPlugin.getServerUrl() + SecugenPlugin.getServerUrlFilepath() + "Identify";
		    	buildUploadMessage(callbackContext, templateSize, urlServer);
		    	createImageFile(callbackContext);
			}
		} catch (Exception e) {
			e.printStackTrace();
//			callbackContext.error("captureImageTemplate Error: " + e);
			PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Scan failed: Try again.");
        	result.setKeepCallback(true);
        	callbackContext.sendPluginResult(result);
		}
    }
    
    private void scan(final CallbackContext callbackContext) {
    	debugMessage("Clicked scan\n");
    	int[] templateSize;
    	try {
    		templateSize = captureImageTemplate();
    		if (templateSize.length == 0) {
    			String msg = "Scan failed: Unable to capture fingerprint. Please kill the app in the Task Manager and restart the app.";
    			Log.d(TAG, msg);
    			PluginResult result = new PluginResult(PluginResult.Status.ERROR, msg);
    			result.setKeepCallback(true);
    			callbackContext.sendPluginResult(result);
    		} else {
    			Log.d(TAG, "templateSize: " + templateSize[0]);
    			String templateString;
    			byte[] newTemplate = Arrays.copyOfRange(mRegisterTemplate, 0, templateSize[0]);
    			templateString = Utils.encodeHexToString(newTemplate, Utils.DIGITS_UPPER);
    			Log.d(TAG, "templateString: " + templateString);
    			PluginResult result = new PluginResult(PluginResult.Status.OK, templateString);
	        	result.setKeepCallback(true);
	        	callbackContext.sendPluginResult(result);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
//			callbackContext.error("captureImageTemplate Error: " + e);
    		PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Scan failed: Try again.");
    		result.setKeepCallback(true);
    		callbackContext.sendPluginResult(result);
    	}
    }

	/**
	 * @return
	 * @throws Exception 
	 */
	public int[] captureImageTemplate() throws Exception {
		int[] templateSize = new int[1];
		
		SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
		sgfplib.GetDeviceInfo(deviceInfo);
        mImageWidth = deviceInfo.imageWidth;
		mImageHeight= deviceInfo.imageHeight;
		int dpi = deviceInfo.imageDPI;
		debugMessage("mImageWidth: " + mImageWidth);
		debugMessage("mImageHeight: " + mImageHeight);
		debugMessage("dpi: " + dpi);
		if (mRegisterImage != null)
        	mRegisterImage = null;
        mRegisterImage = new byte[mImageWidth*mImageHeight];
//    	this.mCheckBoxMatched.setChecked(false);
        dwTimeStart = System.currentTimeMillis();          
        long result = sgfplib.GetImage(mRegisterImage);
//        Utils.DumpFile("register" + System.currentTimeMillis() +".raw", mRegisterImage);
//		afis.setDpi(dpi);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        if (result == 0) {
        	dwTimeStart = System.currentTimeMillis();       
            // Create template from captured image
            SGFingerInfo finger_info = new SGFingerInfo();
            debugMessage("CreateTemplate() started \n");
            for (int i=0; i< mRegisterTemplate.length; ++i)
            	mRegisterTemplate[i] = 0;
            result = sgfplib.CreateTemplate( finger_info , mRegisterImage, mRegisterTemplate );
            sgfplib.GetTemplateSize(mRegisterTemplate, templateSize);
            debugMessage("templateSize: " + templateSize[0]);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("CreateTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
            if (result == 105) {
            	debugMessage("Error: Template Extraction failed! " );
            	templateSize = new int[0];
//            	throw new Exception("Error: Template Extraction failed" );
            }
        } else {
        	if (result == 2) {
        		debugMessage("Error: GetImage failed - Function call failed." );
        	}
        	templateSize = new int[0];
        }
        
//        String templatefileName = "register.template-" + System.currentTimeMillis() + ".txt";
//        Utils.DumpFile(templatefileName, mRegisterTemplate);
//        final String templatePath = SecugenPlugin.getTemplatePath() + templatefileName;
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
			jo.put("Name", SecugenPlugin.getProjectName());
			jo.put("Template", templateString);
			jo.put("Finger", 1);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		{"Key":"sample string 1","Template":"sample string 2","Finger":3}
		debugMessage("Send to the server: : " + jo.toString());

        
        final String uploadMessage = "";
			Thread thread = new Thread(new Runnable(){
			    @Override
			    public void run() {
			    	JSONObject serviceResponse = null;
			        try {
			        	dwTimeStart = System.currentTimeMillis();  
			        	serviceResponse = Utils.post(jo, url);
			        	String serviceResponseStr = serviceResponse.toString();
			        	Log.d(TAG, "Sending serviceResponseStr to PluginResult: " + serviceResponseStr);
						PluginResult result = new PluginResult(PluginResult.Status.OK, serviceResponseStr);
			        	result.setKeepCallback(true);
//						callbackContext.success(uploadMessage);
			        	callbackContext.sendPluginResult(result);
			        	dwTimeEnd = System.currentTimeMillis();
			            dwTimeElapsed = dwTimeEnd-dwTimeStart;
			            debugMessage("uploadMessage() ret:" + uploadMessage + " [" + dwTimeElapsed + "ms]\n");
			        } catch (Exception e) {
			            e.printStackTrace();
			            String message = "Upload Error: " + serviceResponse + " Error: " + e;
			            PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
                    	result.setKeepCallback(true);
                    	callbackContext.sendPluginResult(result);
			            dwTimeEnd = System.currentTimeMillis();
			            dwTimeElapsed = dwTimeEnd-dwTimeStart;
			            debugMessage("uploadMessage() ret:" + uploadMessage + " [" + dwTimeElapsed + "ms]\n");
			        }
			    }
			});
			thread.start(); 
		
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
    
    private void debugMessage(String message) {
    	//      this.mEditLog.append(message);
    	//      this.mEditLog.invalidate(); //TODO trying to get Edit log to update after each line written
    	Log.d(TAG, message);
    }

    //This broadcast receiver is necessary to get user permissions to access the attached USB device
    private static final String ACTION_USB_PERMISSION = "org.rti.kidsthrive.secugenplugin.USB_PERMISSION";
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
//    	try {
//    		Log.e(TAG, "unregisterReceiver mUsbReceiver");
//			context.unregisterReceiver(mUsbReceiver);
//		} catch (Exception e1) {
//			Log.e(TAG, "", e1);
//		}  finally {
//		}
    	sgfplib.CloseDevice();
    	mRegisterImage = null;
    	mVerifyImage = null;
    	mRegisterTemplate = null;
    	sgfplib.Close();
        super.onDestroy();
    }
    
/**
	 * 
	 */
	public void initDeviceSettings() {
		long error;
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
		sgfplib.writeData((byte)5, (byte)1);
	}

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

	public static String getProjectName() {
		return projectName;
	}

	public static void setProjectName(String projectName) {
		SecugenPlugin.projectName = projectName;
	}

}
