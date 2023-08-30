package p4server.websocket;

import java.io.IOException;
import java.util.UUID;

import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint.Async;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import p4server.App;
import p4server.core.Game;

@ServerEndpoint(value = "/game/{gameId}")
public class GameStream {

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") String gameId) throws IOException {
        Async a = session.getAsyncRemote();
        a.setSendTimeout(10_000l);
        
        GameStreamSession s = getSession(session, gameId);
        if (s != null)
        	s.add(session);
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("gameId") String gameId) throws IOException {
    	remove(session, gameId, null);
    }
    
    @OnError
    public void onError(Session session, @PathParam("gameId") String gameId) throws IOException {
    	remove(session, gameId, null);
    }
    
    @OnMessage
    public void onMessage(Session session, @PathParam("gameId") String gameId) throws IOException {
    	remove(session, gameId, new CloseReason(CloseCodes.CANNOT_ACCEPT, "cannot accept messages"));
    }
    
    private final void remove(Session session, String gameId, CloseReason close) throws IOException {
    	GameStreamSession s = getSession(session, gameId);
    	if (s != null)
    		s.remove(session);
    	if (close != null && session.isOpen())
    		session.close(close);
    }
    
    private final GameStreamSession getSession(Session session, String gameId) throws IOException {
    	try {
        	UUID uuid = UUID.fromString(gameId);
        	Game g = App.getInstance().getGame(uuid);
        	if (g == null)
        		throw new IllegalArgumentException();
        	
        	return g.stream();
        } catch(IllegalArgumentException e) {
        	session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "Bad game id"));
        	return null;
        }
    }	
}
