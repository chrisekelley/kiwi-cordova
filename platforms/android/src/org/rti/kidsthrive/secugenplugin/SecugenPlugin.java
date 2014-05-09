package org.rti.kidsthrive.secugenplugin;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * This class echoes a string called from JavaScript.
 */
public class SecugenPlugin extends CordovaPlugin {
	
	private static final String TAG = "SecuGen USB";
	private static final String dirPath = "/sdcard/Download/";
	
	
	
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
    private static final String CAPTURE = "capture";
    private static final String BLINK = "blink";
    
	
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
	}
	
//	if (message != null && message.length() > 0) {
//        callbackContext.success(message);
//    } else {
//        callbackContext.error("Expected one non-empty string argument.");
//    }
	

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
                    manager.requestPermission(usbDevice, mPermissionIntent);
                    
                    
	    			error = sgfplib.OpenDevice(0);
	    			debugMessage("OpenDevice() ret: " + error + "\n");
	    			SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
	    			error = sgfplib.GetDeviceInfo(deviceInfo);
	    			debugMessage("GetDeviceInfo() ret: " + error + "\n");
	    			mImageWidth = deviceInfo.imageWidth;
	    			mImageHeight= deviceInfo.imageHeight;
	    			debugMessage("mImageWidth: " + mImageWidth);
	    			debugMessage("mImageHeight: " + mImageHeight);
	    			sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
	    			sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
	    			debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");
	    			mRegisterTemplate = new byte[mMaxTemplateSize[0]];
	    			mVerifyTemplate = new byte[mMaxTemplateSize[0]];
	    			sgfplib.writeData((byte)5, (byte)1);
	    			callbackContext.success("Fingerprint scanner initialised.");

	            }
			 });
			
		}
	}

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	
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
        	register(callbackContext);

      } else if (action.equals(CAPTURE)) {

//          boolean secure = true;
//          connect(args, secure, callbackContext);
    	  	capture(callbackContext);
    	  	
      } else if (action.equals(BLINK)) {
    	  
//          boolean secure = true;
//          connect(args, secure, callbackContext);
    	  	blink(callbackContext);

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

      return validAction;
  }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    private void register(CallbackContext callbackContext) {
        debugMessage("Clicked REGISTER\n");
        if (mRegisterImage != null)
        	mRegisterImage = null;
        mRegisterImage = new byte[mImageWidth*mImageHeight];
//    	this.mCheckBoxMatched.setChecked(false);
        ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
        dwTimeStart = System.currentTimeMillis();          
        long result = sgfplib.GetImage(mRegisterImage);
        DumpFile("register.raw", mRegisterImage);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        Bitmap b = Bitmap.createBitmap(mImageWidth,mImageHeight, Bitmap.Config.ARGB_8888);
        byteBuf.put(mRegisterImage);
        int[] intbuffer = new int[mImageWidth*mImageHeight];
        for (int i=0; i<intbuffer.length; ++i)
        	intbuffer[i] = (int) mRegisterImage[i];
        b.setPixels(intbuffer, 0, mImageWidth, 0, 0, mImageWidth, mImageHeight); 
        //DEBUG Log.d(TAG, "Show Register image");
//        mImageViewFingerprint.setImageBitmap(this.toGrayscale(b));  
        dwTimeStart = System.currentTimeMillis();          
        result = sgfplib.SetTemplateFormat(SecuGen.FDxSDKPro.SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("SetTemplateFormat(SG400) ret:" +  result + " [" + dwTimeElapsed + "ms]\n");
        SGFingerInfo fpInfo = new SGFingerInfo();
        for (int i=0; i< mRegisterTemplate.length; ++i)
        	mRegisterTemplate[i] = 0;
        dwTimeStart = System.currentTimeMillis();          
        result = sgfplib.CreateTemplate(fpInfo, mRegisterImage, mRegisterTemplate);
        DumpFile("register.min", mRegisterTemplate);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("CreateTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
//        this.mImageViewRegister.setImageBitmap(this.toGrayscale(b));  
//    	mTextViewResult.setText("Click Verify");
        callbackContext.success("Fingerprint registered.");
    }
    
    private void capture(CallbackContext callbackContext) {
    	
    	Log.d(TAG, "Pressed CAPTURE");
//    	this.mCheckBoxMatched.setChecked(false);
        byte[] buffer = new byte[mImageWidth*mImageHeight];
        ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
        dwTimeStart = System.currentTimeMillis();          
        long result = sgfplib.GetImage(buffer);
        DumpFile("capture.raw", buffer);
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
        FileOutputStream out = null;
        try {
        	Log.d(TAG, "Saving file to "+ dirPath + "capture.png");
        	out = new FileOutputStream(dirPath + "capture.png");
        	b.compress(Bitmap.CompressFormat.PNG, 90, out);
        	callbackContext.success("Fingerprint scan saved.");
        } catch (Exception e) {
        	Log.d(TAG, "Error saving file to "+ dirPath + "capture.png. Message: " + e);
        	e.printStackTrace();
        } finally {
        	try{
        		out.close();
        	} catch(Throwable ignore) {}
        }
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
    
    public void DumpFile(String fileName, byte[] buffer)
    {
    	//Uncomment section below to dump images and templates to SD card
    	
        try {
            File myFile = new File("/sdcard/Download/" + fileName);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            fOut.write(buffer,0,buffer.length);
            fOut.close();
        } catch (Exception e) {
            debugMessage("Exception when writing file" + fileName);
        }
       
    } 

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
}
