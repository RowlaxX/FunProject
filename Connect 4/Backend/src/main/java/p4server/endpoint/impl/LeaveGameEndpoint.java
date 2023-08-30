package p4server.endpoint.impl;

import com.google.gson.JsonElement;

import jakarta.servlet.annotation.WebServlet;
import p4server.UserSession;
import p4server.core.Game;
import p4server.endpoint.AbstractEndpoint;

@WebServlet("/leave")
public class LeaveGameEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = 779707396981048418L;

	@Override
	protected Object doPost(UserSession session, JsonElement req) {
		needGame(session, true);
		
		Game game = session.gameNow();
		game.leave(session);
		redirectToMenu(session);
		return null;
	}
}
