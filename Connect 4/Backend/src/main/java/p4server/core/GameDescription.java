package p4server.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

import p4server.MyRuntimeException;

public class GameDescription {
	private static Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9àâäąèéêëîôöùû_- ]{4,32}");
	
	private static byte[] hash(String msg) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
		    md.update(msg.getBytes(StandardCharsets.UTF_8));
		    return md.digest();
		}catch(NoSuchAlgorithmException e) {
			throw new UnknownError();
		}
	}
	
	public static record Request(String name, String password, Integer timePerMove, Integer column, Integer line) {}
	
	private final byte[] password;
	@Expose private final int timePerMove;
	@Expose private final String name;
	@Expose private final boolean hasPassword;
	@Expose private final int column;
	@Expose private final int line;
	@Expose private final UUID uuid = UUID.randomUUID();
	@Expose private GameStatus status;
	@Expose private String endMessage;

	GameDescription(Request request) {
		this.name = Objects.requireNonNull(request.name);
		this.timePerMove = request.timePerMove == null ? 60 : request.timePerMove.intValue();
		this.hasPassword = request.password != null;
		this.password = hasPassword ? hash(request.password) : null;
		this.status = GameStatus.WAITING_FOR_PLAYERS;
		this.column = request.column == null ? 7 : request.column.intValue();
		this.line = request.line == null ? 6 : request.line.intValue();
		
		if (timePerMove < 3 || timePerMove > 300)
			throw new MyRuntimeException("Inactivity delay must be at least 3 and less or equals than 300");
		if (line < 4 || line > 16)
			throw new MyRuntimeException("Line must be between 4 and 16");
		if (column < 4 || column > 16)
			throw new MyRuntimeException("Column must be between 4 and 16");
		if (PATTERN.matcher(name).matches())
			throw new MyRuntimeException("Bad game name");
	}
	
	public String name() {
		return name;
	}
	
	public boolean hasPassword() {
		return hasPassword;
	}
	
	public GameStatus status() {
		return status;
	}
	
	void start() {
		if (status != GameStatus.WAITING_FOR_PLAYERS)
			throw new IllegalStateException("Already started");
		this.status = GameStatus.RUNNING;
	}
	
	void terminate(String message) {
		if (status == GameStatus.FINISHED)
			throw new IllegalStateException("Already terminated");
		this.endMessage = message;
		this.status = GameStatus.FINISHED;
	}
	
	public UUID uuid() {
		return uuid;
	}
	
	public boolean isFinished() {
		return status == GameStatus.FINISHED;
	}
	
	public boolean isStarted() {
		return status != GameStatus.WAITING_FOR_PLAYERS;
	}
	
	public boolean testPassword(String test) {
		if (!hasPassword)
			return true;
		return Arrays.equals(password, hash(test));
	}
	
	public int timePerMove() {
		return timePerMove;
	}
	
	public int column() {
		return column;
	}
	
	public int line() {
		return line;
	}
}
