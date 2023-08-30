package p4server.endpoint.impl;

import java.util.UUID;

import com.google.gson.JsonElement;

import jakarta.servlet.annotation.WebServlet;
import p4server.App;
import p4server.UserSession;
import p4server.core.Game;
import p4server.endpoint.AbstractEndpoint;

@WebServlet("/join")
public class JoinGameEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = -7173698245811601034L;

	@Override
	protected Object doPost(UserSession session, JsonElement req) {
		redirect(session);
		
		UUID uuid = UUID.fromString(req.getAsString());
		Game game = App.getInstance().getGame(uuid);
		
		game.join(session);
		redirectToGame(session);
		return null;
	}
	
}
