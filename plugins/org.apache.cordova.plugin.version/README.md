# Version plugin for Cordova 3.x #

Version allows you to get, display and use the version name and version code of your PhoneGap application.

This is for Cordova 3.x.  Use the version at https://github.com/gcatalfamo/Version for older versions of Cordova/Phonegap.

This plugin was born after gcatalfamo asked (and answered) this StackOverflow question:
http://stackoverflow.com/questions/15178468/phonegap-android-get-the-app-version-code

If you find this plugin useful, feel free to upvote the question so that more people will find it.

If you are seeking to use this version info to display a "Download now" notification to update your application, you are in luck!
There is some really helpful code from gcatalfamo in [issue 2](https://github.com/gcatalfamo/Version/issues/2) that scopes it out really well.
I adapted that code and added support for notifications and download from a non Google Play store server.
That code is in [example.md](https://raw.github.com/chrisekelley/Version/master/example.md).

Thanks a lot gcatalfamo!

## Adding the Plugin to your project ##

$ cordova plugin add https://github.com/chrisekelley/Version.git

## Using the plugin ##

The plugin creates the object `window.plugins.version`.  To use, call one of the following, available methods:

	window.plugins.version.getVersionCode(
		function(version_code) {
			//do something with version_code
			console.log(version_code);

		},
		function(errorMessage) {
			//do something with errorMessage
			console.log(errorMessage);

		}
	);

	window.plugins.version.getVersionName(
		function(version_name) {
			//do something with version_name
			console.log(version_name);

		},
		function(errorMessage) {
			//do something with errorMessage
			console.log(errorMessage);

		}
	);


## RELEASE NOTES ##

### Sept 3, 2013 ###

* Update to Cordova 3.0

### May 8th, 2013 ###

* Update to Cordova 2.7

### March 3rd, 2012 ###

* Initial release




## BUGS AND CONTRIBUTIONS ##

If you have a patch, fork my repo and send me a pull request. Submit bug reports on GitHub, please.

## LICENSE ##

The MIT License

Copyright (c) 2013 Giuseppe Catalfamo

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
