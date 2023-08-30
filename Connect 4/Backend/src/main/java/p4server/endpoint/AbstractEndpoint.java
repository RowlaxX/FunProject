package p4server.endpoint;

import java.io.IOException;
import java.util.function.BiFunction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import p4server.App;
import p4server.MyRuntimeException;
import p4server.UserSession;
import p4server.endpoint.EndpointUtils.Commands;

public abstract class AbstractEndpoint extends HttpServlet {
	private static final long serialVersionUID = -3211945172500135779L;
	
	private void executeRest(HttpServletRequest req, HttpServletResponse resp, BiFunction<UserSession, JsonElement, Object> func) throws IOException {
		UserSession ses = App.getInstance().getSession(req);
		JsonObject jsonResp = new JsonObject();
		
		try{
			JsonElement jsonReq = JsonParser.parseReader(req.getReader());
			jsonResp = execute(ses, jsonReq, func);
		} catch(JsonSyntaxException e) {
			jsonResp = EndpointUtils.jsonOf(Commands.ERROR, e.getMessage());
		}
		
		resp.addHeader("Connection", "keep-alive");
		resp.addHeader("ContentType", "application/json");
		resp.getWriter().write(jsonResp.toString());
	}
	
	private JsonObject execute(UserSession session, JsonElement req, BiFunction<UserSession, JsonElement, Object> func) {
		try {		
			return EndpointUtils.jsonOf(Commands.OK, func.apply(session, req));
		} catch(MethodNotSupportedException e) {
			throw e;
		} catch(RedirectException e) {
			return e.asJson();
		} catch(EndpointException | MyRuntimeException e) {
			return EndpointUtils.jsonOf(Commands.ERROR, e.getMessage());
		} catch (RuntimeException re) {
			re.printStackTrace();
			return EndpointUtils.INTERNAL_ERROR;
		}
	}
	
	protected static void redirectToGame(UserSession session) throws RedirectException {
		if (session.isPlaying())
			throw new RedirectException(Commands.JOIN_GAME, session.gameNow().description().uuid().toString());
	}
	
	protected static void redirectToName(UserSession session) throws RedirectException {
		if (session.player().isEmpty())
			throw new RedirectException(Commands.SET_NAME, null);
	}
	
	protected static void redirectToMenu(UserSession session) throws RedirectException {
		throw new RedirectException(Commands.MENU, null);
	}
	
	protected static void redirect(UserSession session) throws RedirectException {
		redirectToGame(session);
		redirectToName(session);
	}
	
	protected static void needGame(UserSession session, boolean need) throws RedirectException {
		if (need && session.isPlaying())
			throw new RedirectException(Commands.ERROR, "You need to be in no current game");
		else if (!need && !session.isPlaying())
			throw new RedirectException(Commands.ERROR, "You need to be in a current game");
	}
	
	protected static void needPlayer(UserSession session, boolean need) throws RedirectException {
		if (!need && session.player().isPresent())
			throw new RedirectException(Commands.ERROR, "You need to have no name");
		else if (need && session.player().isEmpty())
			throw new RedirectException(Commands.ERROR, "You need to have a name");
	}
	
	protected static void need(UserSession session, boolean needGame, boolean needPlayer) throws RedirectException {
		needGame(session, needGame);
		needPlayer(session, needPlayer);
	}
	
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			executeRest(req, resp, this::doGet);
		}catch(MethodNotSupportedException e) {
			super.doGet(req, resp);
		}
	}
	
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			executeRest(req, resp, this::doPost);
		}catch(MethodNotSupportedException e) {
			super.doPost(req, resp);
		}
	}
	
	@Override
	protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			executeRest(req, resp, this::doPut);
		}catch(MethodNotSupportedException e) {
			super.doPut(req, resp);
		}
	}
	
	@Override
	protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			executeRest(req, resp, this::doDelete);
		}catch(MethodNotSupportedException e) {
			super.doDelete(req, resp);
		}
	}

	protected Object doPost(UserSession session, JsonElement req) { 
		throw new MethodNotSupportedException();
	}
	
	protected Object doPut(UserSession session, JsonElement req) { 
		throw new MethodNotSupportedException();
	}
	
	protected Object doDelete(UserSession session, JsonElement req) { 
		throw new MethodNotSupportedException();
	}
	
	protected Object doGet(UserSession session, JsonElement req) { 
		throw new MethodNotSupportedException();
	}
}
