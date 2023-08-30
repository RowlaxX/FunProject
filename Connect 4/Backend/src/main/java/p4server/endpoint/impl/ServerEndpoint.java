package p4server.endpoint.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jakarta.servlet.annotation.WebServlet;
import p4server.App;
import p4server.UserSession;
import p4server.core.Game;
import p4server.core.GameDescription;
import p4server.endpoint.AbstractEndpoint;

@WebServlet("/server")
public class ServerEndpoint extends AbstractEndpoint {
	private static final long serialVersionUID = 4158292240793734472L;
	
	private Gson gson = new Gson();
	
	@Override
	protected Object doGet(UserSession session, JsonElement req) {
		redirectToGame(session);
		
		return App.getInstance().getGames();
	}
	
	@Override
	protected Object doPost(UserSession session, JsonElement req) {
		needGame(session, false);
		
		GameDescription.Request request = gson.fromJson(req, GameDescription.Request.class);
		Game game = App.getInstance().createGame(request);

		game.join(session);
		redirectToGame(session);
		return game.description();
	}
}
