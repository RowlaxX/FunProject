package p4server.endpoint.impl;

import com.google.gson.JsonElement;

import jakarta.servlet.annotation.WebServlet;
import p4server.UserSession;
import p4server.endpoint.AbstractEndpoint;

@WebServlet("/game")
public class GameEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = 3974090699486089410L;

	@Override
	protected Object doGet(UserSession session, JsonElement req) {
		need(session, true, true);
		return session.gameNow();
	}
	
}
