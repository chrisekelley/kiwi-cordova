# Kiwi

Kiwi is an Android application for managing patient health encounters. It uses the [SecugenPlugin] (https://github.com/chrisekelley/SecugenPlugin)
to enable fingerprint scanning for identification. This plugin works with the Secugen Hamster Plus fingerprint scanner.

# Development

This application was built using Cordova.

To deploy to tablet:

    cordova run android

## Updating the Secugen Plugin

Use platforms/android/bakeapp.sh to refresh the plugin when you change it.

## Fingerprint Scanning Service Codes:

- Status Code 0 = Invalid form sent
- Status Code 1 = Success
- Status Code 2 = Invalid key
- Status Code 3 = Invalid ISO template

## Configuration


Change the url and paths in res/values/strings.xml

    <string name="app_name">Kiwi</string>
    <string name="templatePath">/sdcard/Download/fprints/</string>
    <string name="serverUrl">http://somewhere.com/</string>
    <string name="serverUrlFilepath">api/Person/Enroll</string>
    <string name="serverKey">authorizationKey</string>
    <string name="templateFormat">TEMPLATE_FORMAT_ISO19794</string>
    <string name="APP_VERSION">1</string>
    <string name="WIPE_CACHE">1</string>

## Updates

Increment APP_VERSION and set WIPE_CACHE = 1 to force the app to wipe the app cache and reset the whole app. 

Increment version="1.0.9"  and android:versionCode="11" in config.xml. 

Read the [coconut README](https://github.com/chrisekelley/coconut/blob/coconut-pouch/README.md#how-do-i-handle-application-updates) for more information on updates.

## Debugging

Since the fingerprint scanner uses the device's USB port, you must use wifi debugging. Here are some useful commands:

    adb kill-server
    adb start-server
    adb tcpip 5555
    adb shell ip -f inet addr show wlan0 | grep -Po 'inet \K[\d.]+'
    adb connect 192.168.0.101

    ./installapp.sh

Kudos for the one-liner to get the ip address: http://unix.stackexchange.com/a/87470

The platforms/android/bakeapp.sh script is also useful during development when removing/installing the plugin.

## Viewing logs

adb logcat | grep `adb shell ps | grep org.rti.kidsthrive | cut -c10-15`

kudos: http://stackoverflow.com/a/9869609


