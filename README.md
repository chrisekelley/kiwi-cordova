# Kiwi

Kiwi is an Android application for managing patient health encounters. It uses the [SecugenPlugin] (https://github.com/chrisekelley/SecugenPlugin)
to enable fingerprint scanning for identification. This plugin works with the Secugen Hamster Plus fingerprint scanner.

# Development

This application was built using Cordova. 

To deploy to tablet:

    cordova run android
    
## Debugging
    
Since the fingerprint scanner uses the device's USB port, you must use wifi debugging. Here are some useful commands:

    adb kill-server      
    adb start-server   
    adb tcpip 5555
    adb connect 192.168.0.101:5555
    
    
    
