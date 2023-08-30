package p4server.endpoint.impl;

import com.google.gson.JsonElement;

import jakarta.servlet.annotation.WebServlet;
import p4server.UserSession;
import p4server.core.Game;
import p4server.endpoint.AbstractEndpoint;

@WebServlet("/place")
public class PlaceEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = 162743430487518691L;

	@Override
	protected Object doPost(UserSession session, JsonElement req) {
		need(session, true, true);
		
		int column = req.getAsInt();
		Game game = session.gameNow();
		game.play(session, column);
		return null;
	}
}
