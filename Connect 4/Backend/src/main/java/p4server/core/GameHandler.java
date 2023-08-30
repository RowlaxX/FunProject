package p4server.core;

public interface GameHandler {

	default void onCreated(Game game) {}
	default void onFinished(Game game) {}
	default void onStarted(Game game) {}
	default void onMove(Game game) {}
	default void destroy(Game game) {}
	
}
