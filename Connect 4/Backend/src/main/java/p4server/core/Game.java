package p4server.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.annotations.Expose;

import p4server.MyRuntimeException;
import p4server.UserSession;
import p4server.websocket.GameStreamSession;

public class Game {
	private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(4);
	private static final int WAITING_FOR_PLAYER_TIME = 2*60;
	private static final int DESTROY_AFETR = 10*60;
	
	@Expose private final GameDescription description;
	@Expose private final byte[][] field;
	@Expose private final LinkedList<Move> moveHistory = new LinkedList<>();
	@Expose private final List<UserSession> players = new ArrayList<>(2);
	@Expose private byte playing;
	@Expose private int turn = 0;
	
	private final GameHandler handler;
	private ScheduledFuture<?> task;
	private GameStreamSession stream = new GameStreamSession(this);
	
	public Game(GameDescription.Request request, GameHandler handler) {
		this.description = new GameDescription(request);
		this.field = new byte[description.column()][description.line()];
		for (byte[] column : field)
			Arrays.fill(column, (byte)-1);
		this.handler = handler;
		this.handler.onCreated(this);
		execute(() -> terminate(TerminaisonCause.NOT_ENOUGH_PLAYER), WAITING_FOR_PLAYER_TIME);
	}
	
	public Player playingPlayer() { return players.get(playing).playerNow(); }
	public Player waitingPlayer() { return players.get(playing == 0 ? 1 : 0).playerNow(); }
	public List<Move> moveHistory() { return Collections.unmodifiableList(moveHistory); }
	public Move lastMove() { return moveHistory.getLast(); }
	public GameDescription description() { return description; }
	public int turn() { return turn; }
	public GameStreamSession stream() { return stream; }
	
	private void execute(Runnable r, int delay) {
		if (task != null)
			task.cancel(false);
		task = r == null ? null : EXECUTOR.schedule(r, delay, TimeUnit.SECONDS);
	}
	
	private void terminate(TerminaisonCause cause) {
		description.terminate(switch (cause) {
			case WIN -> playingPlayer() + " won the match";
			case NOT_ENOUGH_PLAYER -> "Not enough player";
			case PLAYER_TAKING_TOO_LONG -> playingPlayer() + " time's expired";
			case PLAYER_LEFT -> playingPlayer() + " left the match";
			default -> throw new IllegalArgumentException("Unexpected value: " + cause);
		});
		
		handler.onFinished(this);
		players.forEach(s -> s.game(null));
		execute(() -> handler.destroy(this), DESTROY_AFETR);
	}
	
	private void start() {
		description.start();
		playing = (byte)(Math.random() > 0.5 ? 1 : 0);
		handler.onStarted(this);
		execute(() -> terminate(TerminaisonCause.PLAYER_TAKING_TOO_LONG), description.timePerMove());
	}
	
	public synchronized void join(UserSession session) {
		if (session.isPlaying())
			throw new MyRuntimeException("This session is already playing");
		if (description.status() != GameStatus.WAITING_FOR_PLAYERS)
			throw new MyRuntimeException("This game has already started");
		if (session.player().isEmpty())
			return;
		
		this.players.add(session);
		session.game(this);
		
		if (players.size() >= 2)
			start();
	}
	
	public synchronized void leave(UserSession session) {
		if (description.isFinished())
			return;
		
		if (session != null && players.contains(session)) {
			session.game(null);
			if (description.status() == GameStatus.WAITING_FOR_PLAYERS)
				players.remove(session);
			else
				terminate(TerminaisonCause.PLAYER_LEFT);
		}
	}
	
	public void play(UserSession session, int column) {
		if (session == null || !players.contains(session))
			throw new MyRuntimeException("You can't play in this game");
		play(session.playerNow(), column);
	}
	
	private synchronized void play(Player player, int column) {
		if (description.isFinished())
			throw new MyRuntimeException("This game is finished");
		if (player != playingPlayer())
			throw new MyRuntimeException("Not your turn");
		
		boolean win = place(column);
		execute(null, 0);
		moveHistory.addLast(new Move(player, column));
		playing = (byte)(playing == 0 ? 1 : 0);
		handler.onMove(this);

		if (win)
			terminate(TerminaisonCause.WIN);
		else 
			execute(() -> terminate(TerminaisonCause.PLAYER_TAKING_TOO_LONG), column);
	}
	
	private boolean place(int column) {
		if (column < 0 || column >= description.column())
			throw new MyRuntimeException("Column must be between 0 and " + (description.column() - 1) );
		
		byte[] data = field[column];
		if (data[description.line() - 1] != -1)
			throw new MyRuntimeException("This column is full");
		
		int line = 0;
		while (data[line++] != -1);
		
		data[line] = playing;
		return checkWin(column, line);
	}
	
	private boolean checkWin(int column, int line) {
		//horizontal
		int startH = column < 3 ? 0 : column - 3;
		int endH = column + 3 >= description.column() ? description.column() - 1 : column + 3;
		
		int m = 1;
		for (int i = startH + 1 ; i <= endH ; i++) {
			m = field[i][line] == field[i - 1][line] ? m + 1 : 1;
			if (m >= 4)
				return true;
		}
		
		//vertical
		int startV = line < 3 ? 0 : line - 3;
		int endV = line + 3 >= description.line() ? description.line() - 1 : line + 3;
		
		m = 1;
		for (int i = startV + 1 ; i <= endV ; i++) {
			m = field[column][i] == field[column][i - 1] ? m + 1 : 1;
			if (m >= 4)
				return true;
		}
		
		//diagonal
		int startD = Math.min(line - startV, column - startH);
		int endD = Math.min(endV - line, endH - column);
		
		m = 1;
		for (int i = startD + 1 ; i <= endD ; i++) {
			m = field[column + i][line + i] == field[column + i - 1][line + i - 1] ? m + 1 : 1;
			if (m >= 4)
				return true;
		}
		
		return false;
	}
}
