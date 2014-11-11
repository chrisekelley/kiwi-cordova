package org.apache.cordova.plugin.version;

import org.json.JSONArray;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

public class Version extends CordovaPlugin {

    private static final String TAG = "VersionCordovaPlugin";
    public final String ACTION_GET_VERSION_NAME = "GetVersionName";
    public final String ACTION_GET_VERSION_CODE = "GetVersionCode";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        //Log.d(TAG,"Inside Version plugin.");
        boolean result = false;
        PackageManager packageManager = this.cordova.getActivity().getPackageManager();
        if(action.equals(ACTION_GET_VERSION_CODE)) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0);
                result = true;
                callbackContext.success(packageInfo.versionCode);
            }
            catch (NameNotFoundException nnfe) {
                result = false;
                Log.d(TAG,"NameNotFoundException: ACTION_GET_VERSION_CODE");
                callbackContext.success(nnfe.getMessage());
            }
        }
        else if(action.equals(ACTION_GET_VERSION_NAME)) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0);
                result = true;
                callbackContext.success(packageInfo.versionName);
            }
            catch (NameNotFoundException nnfe) {
                result = false;
                Log.d(TAG,"NameNotFoundException: ACTION_GET_VERSION_NAME");
                callbackContext.success(nnfe.getMessage());
            }
        
        }
        
        return result;
    }
}