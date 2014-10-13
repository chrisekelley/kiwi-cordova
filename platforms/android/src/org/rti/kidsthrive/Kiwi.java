/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package org.rti.kidsthrive;

import java.io.File;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.os.Build;

public class Kiwi extends CordovaActivity 
{
    
	private static final String TAG = "Kiwi";
	private static final String APP_VERSION = "APP_VERSION";
     
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
		int id = this.getApplicationContext().getResources().getIdentifier("APP_VERSION", "string", this.getActivity().getPackageName());
//		Log.d(TAG, "APP_VERSION id: " + id);
    	String appVersion = this.getApplicationContext().getResources().getString(id);
    	Log.d(TAG, "appVersion: " + appVersion);
    	SharedPreferences sharedPreferences = this.getPreferences(this.getApplicationContext().MODE_PRIVATE);
    	int appVersionPref = Integer.parseInt(sharedPreferences.getString("APP_VERSION", "0"));
        Log.d(TAG, "appVersionPref: " + appVersionPref);
		if ((appVersionPref != Integer.parseInt(appVersion))) {
            // you are updating
	        Log.d(TAG, "Updating the app.");
	        // check if we need to wipe the cache
	        id = this.getApplicationContext().getResources().getIdentifier("WIPE_CACHE", "string", this.getActivity().getPackageName());
	        Log.d(TAG, "WIPE_CACHE id: " + id);
	    	String wipeCache = this.getApplicationContext().getResources().getString(id);
	    	Log.d(TAG, "wipeCache: " + wipeCache);
	    	if (Integer.parseInt(wipeCache) == 1) {
	    		Log.d(TAG, "wiping the cache: ");
//	    		super.clearCache(); // just add This Line
	    		clearApplicationData();
	    	}
            // set APP_VERSION = 2 and your new values
	        SharedPreferences.Editor editor = sharedPreferences.edit();
	        editor.putString("APP_VERSION", appVersion);
	        editor.commit();
        } else {
            // you are not updating
        	Log.d(TAG, "Not updating the app.");
        }
    	
		super.onCreate(savedInstanceState);
        super.init();
        
        // Set by <content src="index.html" /> in config.xml
        super.loadUrl(Config.getStartUrl());
        //super.loadUrl("file:///android_asset/www/index.html");
    }
	
	public void clearApplicationData() {
	       File cache = getCacheDir();
	       File appDir = new File(cache.getParent());
	       Log.d(TAG, "appDir: " + appDir.getAbsolutePath());
	       if (appDir.exists()) {
	           String[] children = appDir.list();
	           for (String s : children) {
	               if (!s.equals("lib")) {
	                   deleteDir(new File(appDir, s));
	                   Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
	           }
	       }
	       }
	   }

	   public static boolean deleteDir(File dir) {
	       if (dir != null && dir.isDirectory()) {
	           String[] children = dir.list();
	           for (int i = 0; i < children.length; i++) {
	               boolean success = deleteDir(new File(dir, children[i]));
	               if (!success) {
	                   return false;
	           }
	       }
	       }
	           return dir.delete();
	   }

     @Override
    public void onReceivedError( int errorCode, String description, String failingUrl)
    {
        if (failingUrl.contains("#")) {
            Log.v("LOG", "Config.getStartUrl():"+ Config.getStartUrl());
            Log.v("LOG", "failing url:"+ failingUrl);
            final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion > Build.VERSION_CODES.GINGERBREAD) {
                String[] temp;
                temp = failingUrl.split("#");
                Log.v("LOG", "load page without internal link:"+ temp[0]);
                super.loadUrl(temp[0]); // load page without internal link
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
            Log.v("LOG", "try again failing url:"+ failingUrl);
            super.loadUrl(failingUrl);  // try again
        } else {
             Log.v("LOG", "failing url does not contain #, loading regular url.");
             super.loadUrl(Config.getStartUrl());
        }
    }
}

