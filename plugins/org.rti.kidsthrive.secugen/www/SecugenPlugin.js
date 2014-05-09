var exec = require('cordova/exec');

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "SecugenPlugin", "coolMethod", [arg0]);
};
exports.register = function(success, error) {
	exec(success, error, "SecugenPlugin", "register", []);
};
exports.capture = function(success, error) {
	exec(success, error, "SecugenPlugin", "capture", []);
};
exports.blink = function(success, error) {
	exec(success, error, "SecugenPlugin", "blink", []);
};
exports.requestPermission = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'SecugenPlugin', 'requestPermission', []);
};

