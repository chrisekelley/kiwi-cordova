# Cordova 3.x plugin for Secugen fingerprint scanners

This plugin is for Phonegap/Cordova 3.x.

I'm not distributing the FDxSDKProAndroid.jar and its supporting armeabi dir - you must add those yourself and un-comment the relevant code.

This version uses a REST client to upload fingerprint images for templating and identification.

[Kiwi-Cordova](https://github.com/chrisekelley/kiwi-cordova) is a Cordova project that implements this plugin.

# Installation

I have not yet uploaded this to NPM, so you must clone this project and install it using the following commands:

    plugman install --platform android --project /pathToCordovaApp/platforms/android --plugin /Users/user/source/SecugenPlugin  
    rm -rf ../../plugins/org.rti.kidsthrive.secugen
    cp -Rf cordova/plugins/org.rti.kidsthrive.secugen ../../plugins    

# Configuration

In your Cordova project's platforms/android/res/values/strings.xml, set the following values:

    <string name="templatePath">/sdcard/Download/fprints/</string>
    <string name="serverUrl">http://somewhere.com/</string>
    <string name="serverUrlFilepath">api/Person/Enroll</string>
    <string name="serverKey">authenticationKey</string>
    <string name="templateFormat">TEMPLATE_FORMAT_ISO19794</string>
    <string name="projectName">projectName</string>

# Development

Code for updating the plugin in your Cordova project:

    cordova build  
    plugman uninstall --platform android --project /pathToCordovaApp/platforms/android --plugin org.rti.kidsthrive.secugen   
    plugman install --platform android --project /pathToCordovaApp/platforms/android --plugin /Users/user/source/SecugenPlugin  
    rm -rf ../../plugins/org.rti.kidsthrive.secugen
    cp -Rf cordova/plugins/org.rti.kidsthrive.secugen ../../plugins
    cordova run android
