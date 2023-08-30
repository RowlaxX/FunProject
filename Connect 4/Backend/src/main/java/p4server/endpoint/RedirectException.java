package p4server.endpoint;

import com.google.gson.JsonObject;

import p4server.endpoint.EndpointUtils.Commands;

class RedirectException extends RuntimeException {
	private static final long serialVersionUID = -6345816804993743263L;

	private Commands command;
	private Object data;
	
	RedirectException(Commands command, Object data) {
		this.command = command;
		this.data = data;
	}
	
	public Object data() {
		return data;
	}
	
	public Commands command() {
		return command;
	}
	
	public JsonObject asJson() {
		return EndpointUtils.jsonOf(command, data);
	}
	
}
