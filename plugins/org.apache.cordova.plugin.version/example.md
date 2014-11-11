# Example Update App #

This example code is for Cordova 3.x. It is geared for apps that do not rely on the Google App Store - it downloads the files directly from a server.

## Plugins ##

Use the following commands to install the needed plugins:

Notifications:

    cordova plugin add https://git-wip-us.apache.org/repos/asf/cordova-plugin-vibration.git
    cordova plugin add https://git-wip-us.apache.org/repos/asf/cordova-plugin-dialogs.git

To download files you must add both of the following:

    cordova plugin add https://git-wip-us.apache.org/repos/asf/cordova-plugin-file.git
    cordova plugin add https://git-wip-us.apache.org/repos/asf/cordova-plugin-file-transfer.git

WebIntents:

    cordova plugin add https://github.com/chrisekelley/cordova-webintent.git


## version.xml ##

Upload this file to your server.

    <version>
        <v_code>2</v_code>
        <v_name>1.0</v_name>
    </version>

## The business ##

There is some really helpful code from gcatalfamo in [issue 2](https://github.com/gcatalfamo/Version/issues/2) that I
adapted for my use case.

    function checkVersion() {
      console.log("Checking for new version of app.");
      $.ajax({ type: "GET", url: "https://dl.dropboxusercontent.com/s/smoochka/version.xml?token_hash=bippity-boop&dl=1", dataType: "xml",
        success: function(xml) {
          console.log("xml: " + xml);
          $(xml).find('version').each(function(){
            var vcode = $(this).find('v_code').text(); //get the v_code in the xml file
            console.log("Remote version: " + vcode);
            window.plugins.version.getVersionCode(
                function(version_code) {
                  console.log("Installed version: " + version_code);
                  if(version_code != vcode){
                    console.log("Upgrade app!");
                    navigator.notification.confirm(
                        'A new version is out! Get it now!',  // message
                        onVersion,            // callback to invoke with index of button pressed
                        'Update available',                 // title
                        'Update now!, Maybe later'     // buttonLabels
                    );
                  }
                },
                function(errorMessage) {
                  console.log("Error while downloading update: " + errorMessage);
                }
            );
          });
        }
      });
    }

    function onVersion(button) {
      if(button == 1){
        downloadFile()
      }
    }

    //kudos: http://stackoverflow.com/questions/11455323/how-to-download-apk-within-phonegap-app
    //http://www.raymondcamden.com/index.cfm/2013/5/1/Using-the-Progress-event-in-PhoneGap-file-transfers
    function downloadFile(){
      var fileSystem;
      console.log("downloading file.")

      window.requestFileSystem(LocalFileSystem.TEMPORARY, 0,
          function(fs) {
            fileSystem = fs;
            var ft = new FileTransfer();
            var uri = encodeURI("https://dl.dropbox.com/s/hum-dinger/Awesome.apk?dl=1");
            var downloadPath = fileSystem.root.fullPath + "/Awesome.apk";
            navigator.notification.progressStart("Application Update", "Initiating download...");
            ft.onprogress = function(progressEvent) {
              if (progressEvent.lengthComputable) {
                var perc = Math.floor(progressEvent.loaded / progressEvent.total * 100);
                //statusDom.innerHTML = perc + "% loaded...";
                navigator.notification.progressValue(perc);
              }
            };
            ft.download(uri, downloadPath,
                function(theFile) {
                  navigator.notification.progressStop();
                  console.log("download complete: " + theFile.toURL());
                  window.plugins.webintent.startActivity({
                        action: window.plugins.webintent.ACTION_VIEW,
                        url: 'file://' + theFile.fullPath,
                        type: 'application/vnd.android.package-archive'
                      },
                      function() {},
                      function() {
                        alert('Failed to open URL via Android Intent.');
                        console.log("Failed to open URL via Android Intent. URL: " + theFile.fullPath)
                      }
                  );
                },
                function(error) {
                  alert("download error: " + JSON.stringify(e));
                  console.log("download error: " + JSON.stringify(e));
                });
          }, function(e) {
            alert('failed to get fs: ' + JSON.stringify(e));
            console.log("failed to get fs: " + JSON.stringify(e));
          });
    }