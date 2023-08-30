package p4server.core;

import java.util.Objects;
import java.util.regex.Pattern;

import p4server.MyRuntimeException;

public record Player(String name) {
	private static Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9àâäąèéêëîôöùû_- ]{4,32}");
	
	public Player {
		Objects.requireNonNull(name, "name may not be null");
		
		if (!PATTERN.matcher(name).matches())
			throw new MyRuntimeException("Bad player name");
	}
}
