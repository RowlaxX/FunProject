package p4server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.Session;
import p4server.core.Game;

public class GameStreamSession {
	public enum EventTypes { START, MOVE, FINISHED }
	private static Gson gson = new Gson();
	
	private final Game game;
	private final List<Session> sessions = new ArrayList<>();
	
	public GameStreamSession(Game game) {
		this.game = game;
	}
	
	void add(Session async) {
		synchronized (sessions) {
			sessions.add(async);
		}
	}
	
	void remove(Session async) {
		synchronized (sessions) {
			sessions.remove(async);
		}
	}
	
	public void broadcast(EventTypes type) {
		JsonObject json = new JsonObject();
		json.addProperty("eventType", type.name());
		json.add("game", gson.toJsonTree(game));
		String msg = json.toString();
		synchronized (sessions) {
			sessions.forEach(s -> s.getAsyncRemote().sendText(msg));
		}
	}
	
	public void close(String reason) {
		synchronized (sessions) {
			sessions.forEach(s -> {
				try {
					s.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, reason));
				} catch (IOException e) { /*ignored*/ }
			});
		}
	}
}
