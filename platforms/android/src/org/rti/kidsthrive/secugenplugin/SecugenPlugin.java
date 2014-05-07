package org.rti.kidsthrive.secugenplugin;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import SecuGen.FDxSDKPro.JSGFPLib;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * This class echoes a string called from JavaScript.
 */
public class SecugenPlugin extends CordovaPlugin {
	
	private static final String TAG = "SecuGen USB";
	
	private Button mCapture;
    private Button mButtonRegister;
    private Button mButtonMatch;
    private Button mButtonLed;
    private Button mSDKTest;
    private EditText mEditLog;
    private android.widget.TextView mTextViewResult;
    private android.widget.CheckBox mCheckBoxMatched;
    private android.widget.CheckBox mCheckBoxSCEnabled;
    private PendingIntent mPermissionIntent;
    private ImageView mImageViewFingerprint;
    private ImageView mImageViewRegister;
    private ImageView mImageViewVerify;
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
	
	 private void debugMessage(String message) {
//       this.mEditLog.append(message);
//       this.mEditLog.invalidate(); //TODO trying to get Edit log to update after each line written
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
	

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	
//    	context = (Context)this.context;
    	
    	//USB Permissions
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
       	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
       	context.registerReceiver(mUsbReceiver, filter);   
		debugMessage("Getting ACTION_USB_PERMISSION \n");
        sgfplib = new JSGFPLib((UsbManager)context.getSystemService(Context.USB_SERVICE));
//    	this.mCheckBoxSCEnabled.setChecked(true);
        
		debugMessage("jnisgfplib version: " + sgfplib.Version() + "\n");

        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
