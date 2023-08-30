package p4server;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import p4server.core.Game;
import p4server.core.GameDescription;
import p4server.core.GameHandler;
import p4server.websocket.GameStreamSession.EventTypes;

public class App {
	private static final App instance = new App();
	private App() {}

	public static App getInstance() {
		return instance;
	}
	
	private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
	private final Map<UUID, Game> games = new ConcurrentHashMap<>();
	
	public UserSession getSession(HttpServletRequest req) {
		HttpSession hses = req.getSession(true);
		UserSession ses = sessions.get(hses.getId());
		if (ses == null || !Objects.equals(req.getRemoteAddr(), ses.address())) {
			ses = new UserSession(hses.getId(), req.getRemoteAddr());
			sessions.put(ses.id(), ses);
		}
		return ses;
	}
	
	public Game getGame(UUID id) {
		return games.get(id);
	}
	
	public List<GameDescription> getGames() {
		return Map.copyOf(games).values().stream()
			.map(Game::description)
			.toList();
	}
	
	public Game createGame(GameDescription.Request request) {
		return new Game(request, gameHandler);
	}
	
	private GameHandler gameHandler = new GameHandler() {
		@Override
		public void onCreated(Game game) {
			games.put(game.description().uuid(), game);
		}
		@Override
		public void destroy(Game game) {
			games.remove(game.description().uuid());
		}
		@Override
		public void onFinished(Game game) {
			game.stream().broadcast(EventTypes.FINISHED);
		}
		@Override
		public void onMove(Game game) {
			game.stream().broadcast(EventTypes.MOVE);
		}
		@Override
		public void onStarted(Game game) {
			game.stream().broadcast(EventTypes.START);
		}
	};
}
