package p4server.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class EndpointUtils {
	private static final String COMMAND = "command";
	private static final String DATA = "data";
	
	static final Gson GSON = new Gson();
	static final JsonObject INTERNAL_ERROR = jsonOf(Commands.ERROR, "Internal Server Error");
	enum Commands {OK, JOIN_GAME, SET_NAME, ERROR, MENU }
	
	static JsonObject jsonOf(Commands status, Object data) {
		JsonObject json = new JsonObject();
		
		if (data == null) 
			{/*Do nothing*/}
		else if (data instanceof Number n)
			json.addProperty(DATA, n);
		else if (data instanceof Boolean b)
			json.addProperty(DATA, b);
		else if (data instanceof Character c)
			json.addProperty(DATA, c);
		else if (data instanceof String s)
			json.addProperty(DATA, s);
		else if (data instanceof JsonElement j)
			json.add(DATA, j);
		else try {
			json.add(DATA, GSON.toJsonTree(data));
		} catch(RuntimeException e) {
			return INTERNAL_ERROR;
		}
		json.addProperty(COMMAND, status.name());
		return json;
	}
}
