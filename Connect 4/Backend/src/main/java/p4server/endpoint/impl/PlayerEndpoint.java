package p4server.endpoint.impl;

import com.google.gson.JsonElement;

import jakarta.servlet.annotation.WebServlet;
import p4server.UserSession;
import p4server.core.Player;
import p4server.endpoint.AbstractEndpoint;

@WebServlet("/player")
public class PlayerEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = -453432847655996652L;
	
	@Override
	protected Object doPost(UserSession session, JsonElement req) {
		redirectToGame(session);
		
		String newName = req.getAsString();
		Player player = new Player(newName);
		session.player(player);
		return player;
	}
	
	@Override
	protected Object doGet(UserSession session, JsonElement req) {
		return session.playerNow();
	}
}	
