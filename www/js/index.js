/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
    	document.addEventListener('deviceready', this.onDeviceReady, false);
    	// add event listener to register button
    	var el = document.getElementById("register");
    	el.addEventListener("click", register, false);
    	// add event listener to capture button
    	var el = document.getElementById("capture");
    	el.addEventListener("click", capture, false);
    	// add event listener to blink button
    	var el = document.getElementById("blink");
    	el.addEventListener("click", blink, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady: function() {
    	app.receivedEvent('deviceready');
//  	console.log("gonna try to check bluetoothSerial now.");
//  	// check to see if Bluetooth is turned on.
//  	// this function is called only
//  	//if isEnabled(), below, returns success:
//  	var listPorts = function() {
//  	// list the available BT ports:
//  	bluetoothSerial.list(
//  	function(results) {
//  	app.display(JSON.stringify(results));
//  	},
//  	function(error) {
//  	app.display(JSON.stringify(error));
//  	}
//  	);
//  	}

    	// if isEnabled returns failure, this function is called:
    	var notEnabled = function() {
    		app.display("Bluetooth is not enabled.")
    	}

//  	// check if Bluetooth is on:
//  	bluetoothSerial.isEnabled(
//  	listPorts,
//  	notEnabled
//  	);
//  	console.log("gonna try to launch coolmethod now.");
//  	coolMethod('hey there!')
//  	console.log("gonna try to launch SecugenPlugin now.");
//  	SecugenPlugin('hey there!')
    	console.log("gonna try to launch SecugenPlugin.coolMethod now.");
    	cordova.plugins.SecugenPlugin.coolMethod('hey there!');
    	cordova.plugins.SecugenPlugin.requestPermission(
    			function(results) {
    				app.display(JSON.stringify(results),errorCallback);
    			});
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
//        var parentElement = document.getElementById(id);
//        var listeningElement = parentElement.querySelector('.listening');
//        var receivedElement = parentElement.querySelector('.received');
//
//        listeningElement.setAttribute('style', 'display:none;');
//        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },
    click: function () {
        app.receivedEvent('click');
    },
    /*
    appends @error to the message div:
     */
    showError: function(error) {
    	app.display(error);
    },

    /*
    appends @message to the message div:
     */
    display: function(message) {
    	var display = document.getElementById("message"), // the message div
    	lineBreak = document.createElement("br"),     // a line break
    	label = document.createTextNode(message);     // create the label

    	display.appendChild(lineBreak);          // add a line break
    	display.appendChild(label);              // add the message node
    },
    /*
    clears the message div:
     */
    clear: function() {
    	var display = document.getElementById("message");
    	display.innerHTML = "";
    }

};

   
   var register = function() {
   	console.log('Register clicked ');
   	cordova.plugins.SecugenPlugin.register(function(results) {
           app.display(JSON.stringify(results));
       });
   }
   var capture = function() {
	   console.log('Capture clicked ');
	   cordova.plugins.SecugenPlugin.capture(function(results) {
		   app.display(JSON.stringify(results));
	   });
   }
   var blink = function() {
	   console.log('Blink clicked ');
	   cordova.plugins.SecugenPlugin.blink(function(results) {
		   app.display(JSON.stringify(results),errorCallback);
	   });
   }
   
   var errorCallback = function(message) {
	    alert('Error: ' + message);
	};
