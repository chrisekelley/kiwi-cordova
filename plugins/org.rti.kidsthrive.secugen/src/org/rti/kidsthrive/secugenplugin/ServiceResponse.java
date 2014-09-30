package org.rti.kidsthrive.secugenplugin;

import org.json.JSONObject;

public class ServiceResponse extends JSONObject {
	
	private JSONObject scannerPayload;
	private JSONObject serviceMessage;
	
	public JSONObject getScannerPayload() {
		return scannerPayload;
	}
	public void setScannerPayload(JSONObject scannerPayload) {
		this.scannerPayload = scannerPayload;
	}
	public JSONObject getServiceMessage() {
		return serviceMessage;
	}
	public void setServiceMessage(JSONObject serviceMessage) {
		this.serviceMessage = serviceMessage;
	}

}
