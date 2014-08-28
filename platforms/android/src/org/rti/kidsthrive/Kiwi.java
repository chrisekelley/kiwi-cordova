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

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;

import android.os.Bundle;
import android.util.Log;
import android.os.Build;

public class Kiwi extends CordovaActivity 
{
    
	private static final String TAG = "Kiwi";
     
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();
        
        // Set by <content src="index.html" /> in config.xml
        super.loadUrl(Config.getStartUrl());
        //super.loadUrl("file:///android_asset/www/index.html");
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

