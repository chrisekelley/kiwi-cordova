# Kiwi

Kiwi is an Android application for managing patient health encounters. It uses the [SecugenPlugin] (https://github.com/chrisekelley/SecugenPlugin)
to enable fingerprint scanning for identification. This plugin works with the Secugen Hamster Plus fingerprint scanner.

# Development

This application was built using Cordova. 

To deploy to tablet:

    cordova run android
    
## Updating the Secugen Plugin

Use platforms/android/bakeapp.sh to refresh the plugin when you change it.

## Configuration
    
Change the url and paths in res/values/strings.xml

    <string name="templatePath">/sdcard/Download/fprints/</string>
    <string name="serverUrl">http://192.168.128.239:8080/</string>
    <string name="serverUrlFilepath">files/</string>
    
## Debugging
    
Since the fingerprint scanner uses the device's USB port, you must use wifi debugging. Here are some useful commands:

    adb kill-server      
    adb start-server   
    adb tcpip 5555
    adb shell ip -f inet addr show wlan0  
    adb connect 192.168.0.101
    
    ./installapp.sh
    
    
    
