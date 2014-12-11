package com.scispike.logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class Logger extends CordovaPlugin {
	
	private final static String TAG = Logger.class.getSimpleName();
	
	// actions
	private static final String GET_LOGS = "getLogs";	
	
	private CallbackContext getLogsCallback;
	
	@Override
	public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
		Log.d(TAG, "action = " + action);
		
		if (action.equals(GET_LOGS)) {
			getLogsCallback = callbackContext;
			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);			
			getLogsCallback.sendPluginResult(pluginResult);
			
			final String searchTerm = args.getString(0);
			final int numLogs = args.getInt(1);
			
			getLogs(callbackContext, searchTerm, numLogs);
			return true;

		} else {
			callbackContext.error("Invalid action");
			return false;
		}
	}
	
	private void getLogs(final CallbackContext callbackContext, final String searchTerm, final int numLogs) {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					Process process = Runtime.getRuntime().exec(new String[]{"logcat", "-v", "threadtime", "-t", String.valueOf(numLogs)});
					BufferedReader reader = 
					    new BufferedReader(new InputStreamReader(process.getInputStream()));
					
					int count = 0;
					String separator = System.getProperty("line.separator");
					StringBuilder buffer = new StringBuilder();
					String nextLine=null;
					Log.d(TAG, "searching for: "+ searchTerm);
					while (count < numLogs) {
						nextLine = reader.readLine();
						if (nextLine != null 
								//&& (searchTerm== null
								//|| nextLine.contains(searchTerm))
								) {
							buffer.append(nextLine).append(separator);
						}				        						
						count++;
					}
					String result = buffer.toString();
					
					Log.d(TAG, "read: "+ count + "; ending in: "+ nextLine+";; included: "+result.indexOf(nextLine));
					callbackContext.success(result);								
					
				} catch (Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
					callbackContext.error(e.getMessage());
				}
			}
		});			
	}
}
