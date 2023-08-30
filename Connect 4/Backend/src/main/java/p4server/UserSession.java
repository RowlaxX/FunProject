package p4server;

import java.util.Objects;
import java.util.Optional;

import p4server.core.Game;
import p4server.core.Player;

public class UserSession {
	private final String id;
	private final String address;
	
	public UserSession(String id, String address) {
		this.id = Objects.requireNonNull(id, "id may not be null");
		this.address = Objects.requireNonNull(address);
	}
	
	private Optional<Player> player;
	private Optional<Game> game;
	
	
	public Optional<Player> player() {
		return player;
	}
	
	public Optional<Game> game(){
		return game;
	}
	
	public Player playerNow() {
		return player.orElseThrow(() -> new MyRuntimeException("No player for this session"));
	}
	
	public Game gameNow() {
		return game.orElseThrow(() -> new MyRuntimeException("No game for this session"));
	}
	
	public void game(Game game) {
		this.game = Optional.ofNullable(game);
	}
	
	public void player(Player player) {
		this.player = Optional.ofNullable(player);
	}
	
	public String id() {
		return id;
	}
	
	public String address() {
		return address;
	}
	
	public boolean isPlaying() {
		if (game.isEmpty())
			return false;
		return !game.get().description().isFinished();
	}
}
