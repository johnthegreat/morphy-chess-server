/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011, 2017  http://code.google.com/p/morphy-chess-server/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package morphy.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import morphy.game.request.Request;
import morphy.user.UserSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestService implements Service {
	private enum RequestType { ADD,REMOVE; }
	private enum RequestUser { FROM, TO; }
	
	protected static Log LOG = LogFactory.getLog(RequestService.class);
	private static RequestService singletonInstance = new RequestService();
	
	public static RequestService getInstance() {
		return singletonInstance;
	}
	
	protected int stackSize = 0;
	protected Stack<Integer> stack = new Stack<Integer>();
	
	private HashMap<UserSession,List<Request>> fromMap;
	private HashMap<UserSession,List<Request>> toMap;

	public RequestService() {
		fromMap = new HashMap<UserSession,List<Request>>();
		toMap = new HashMap<UserSession,List<Request>>();
	}
	
	public int getNextAvailableNumber() {
		if (stack.empty()) {
			stackSize++;
			stack.push(stackSize);
		}
		return stack.pop();
	}
	
	public void addRequest(UserSession from,UserSession to,Request req) {
		req.setRequestNumber(getNextAvailableNumber());
		if (!fromMap.containsKey(from)) {
			fromMap.put(from,new ArrayList<Request>(10));
		}
		fromMap.get(from).add(req);
		
		if (!toMap.containsKey(to)) {
			toMap.put(to,new ArrayList<Request>(10));
		}
		toMap.get(to).add(req);
		
		this.notifyUsersPendInfo(req, RequestType.ADD);
	}
	
	// convenience method for sending pendinfo lines to both to and from users
	private void notifyUsersPendInfo(Request req, RequestType requestType) {
		this.notifyUserPendInfo(req,requestType,req.getFrom(),RequestUser.FROM);
		this.notifyUserPendInfo(req,requestType,req.getTo(),RequestUser.TO);
	}
	
	// method to send pendinfo to one specific individual
	private void notifyUserPendInfo(Request req, RequestType requestType, UserSession user, RequestUser userType) {
		// Wondering if I could have this code somewhere else. This service so far is pretty clean maybe it could stay that way.
		
		if (requestType == RequestType.ADD) {
		
			/* Also we will need the pendinfo line included with a previous message in some cases, e.g. partnership.
			 * But in most cases (match, abort, pause), it is sent by itself.
			 * Note: # is actually what is sent for p= line for partnership, abort, pause requests.
			 */
			
			// <pt> 3 w=GuestQMSS t=match p=GuestPVGC (----) GuestQMSS (----) unrated blitz 5 0
			if (userType == RequestUser.FROM && isPendinfoSet(user)) {
				final String toUsername = req.getTo().getUser().getUserName();
				String pendInfoLine = String.format("<pt> %d w=%s t=%s p=%s", req.getRequestNumber(), toUsername, req.getRequestIdentifier(), req.getExtraInfo());
				user.send(pendInfoLine);
			}
			
			// <pf> 3 w=GuestPVGC t=match p=GuestPVGC (----) GuestQMSS (----) unrated blitz 5 0
			if (userType == RequestUser.TO && isPendinfoSet(user)) {
				final String fromUsername = req.getFrom().getUser().getUserName();
				String pendInfoLine = String.format("<pf> %d w=%s t=%s p=%s", req.getRequestNumber(), fromUsername, req.getRequestIdentifier(), req.getExtraInfo());
				user.send(pendInfoLine);
			}
			
		} else if (requestType == RequestType.REMOVE) {
			// when offer is withdrawn, <pr> is sent as separate prompt.
			// when offer is declined, <pr> is sent as same prompt as "You decline the..."
			
			// same message sent to both from and to users
			if (isPendinfoSet(user)) {
				user.send(String.format("<pr> %d", req.getRequestNumber()));
			}
		}
	}
	
	private boolean isPendinfoSet(UserSession userSession) {
		String pendInfo = userSession.getUser().getUserVars().getIVariables().get("pendinfo");
		return pendInfo != null && pendInfo.equals("1");
	}
	
	public Request getRequestFrom(UserSession userSession,int id) {
		Collection<List<Request>> collection = fromMap.values();
		for(List<Request> list : collection) {
			for(Request r : list) {
				if (r.getRequestNumber() == id)
					return r;
			}
		}
		return null;
	}
	
	public Request getRequestTo(UserSession userSession,int id) {
		Collection<List<Request>> collection = toMap.values();
		for(List<Request> list : collection) {
			for(Request r : list) {
				if (r.getRequestNumber() == id)
					return r;
			}
		}
		return null;
	}
	
	public List<Request> getRequestsFrom(UserSession userSession) {
		if (fromMap.containsKey(userSession)) {
			return fromMap.get(userSession);
		}
		return null;
	}
	
	public List<Request> getRequestsTo(UserSession userSession) {
		if (toMap.containsKey(userSession)) {
			return toMap.get(userSession);
		}
		return null;
	}
	
	public List<Request> findAllFromRequestsByType(UserSession userSession,Class<? extends Request> type) {
		if (!fromMap.containsKey(userSession)) return null;
		
		final Request[] rList = fromMap.get(userSession).toArray(new Request[0]);
		List<Request> copy = new ArrayList<Request>();
		for(int i=0;i<rList.length;i++) {
			Request r = rList[i]; 
			if (r.getClass() == type) { copy.add(r); }
		}
		return copy;
	}
	
	public List<Request> findAllToRequestsByType(UserSession userSession,Class<? extends Request> type) {
		if (!toMap.containsKey(userSession)) return null;
		
		final Request[] rList = toMap.get(userSession).toArray(new Request[0]);
		List<Request> copy = new ArrayList<Request>();
		for(int i=0;i<rList.length;i++) {
			Request r = rList[i];
			if (r.getClass() == type) { copy.add(r); }
		}
		return copy;
	}
	
	public void removeRequest(Request request) {
		this.removeRequestFrom(request.getFrom(),request);
		this.removeRequestTo(request.getTo(),request);
		this.recycleRequestNumber(request.getRequestNumber());
		this.notifyUsersPendInfo(request, RequestType.REMOVE);
	}
	
	/** This method removes all requests of type "type" from outgoing offers. */
	protected void removeRequestsFrom(UserSession userSession,Class<? extends Request> type) {
		if (!fromMap.containsKey(userSession)) return;
		
		List<Request> rList = fromMap.get(userSession);
		for(int i=0;i<rList.size();i++) {
			Request r = rList.get(i); 
			if (r.getClass() == type) {
				rList.remove(i--);
			}
		}
	}
	
	protected void removeRequestsTo(UserSession userSession,Class<? extends Request> type) {
		if (!toMap.containsKey(userSession)) return;
		
		List<Request> rList = toMap.get(userSession);
		for(int i=0;i<rList.size();i++) {
			Request r = rList.get(i); 
			if (r.getClass() == type) {
				rList.remove(i--);
			}
		}
	}
	
	protected void removeRequestFrom(UserSession userSession,Request instance) {
		if (!fromMap.containsKey(userSession)) return;
		
		List<Request> rList = fromMap.get(userSession);
		rList.remove(instance);
	}
	
	protected void removeRequestTo(UserSession userSession,Request instance) {
		if (!toMap.containsKey(userSession)) return;
		
		List<Request> rList = toMap.get(userSession);
		rList.remove(instance);
	}
	
	protected void removeAllRequestsTo(UserSession userSession) {
		if (!toMap.containsKey(userSession)) return;
		
		List<Request> rList = toMap.get(userSession);
		for(Request request : rList) {
			this.removeRequestTo(userSession, request);
		}
	}
	
	public void recycleRequestNumber(int num) {
		stack.push(num);
	}
	
	public void dispose() {
		fromMap.clear();
		toMap.clear();
		stack.clear();
		
		if (LOG.isInfoEnabled()) {
			LOG.info("RequestService Disposed.");
		}
	}

}
