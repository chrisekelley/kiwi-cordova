plugman uninstall --platform android --project /Users/chrisk/source/kiwi-cordova/platforms/android --plugin org.rti.kidsthrive.secugen   
plugman install --platform android --project /Users/chrisk/source/kiwi-cordova/platforms/android --plugin /Users/chrisk/source/SecugenPlugin  
rm -rf ../../plugins/org.rti.kidsthrive.secugen
cp -Rf cordova/plugins/org.rti.kidsthrive.secugen ../../plugins
cordova run android
adb logcat
