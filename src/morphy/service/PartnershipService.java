/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2011  http://code.google.com/p/morphy-chess-server/
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

import java.util.HashMap;
import java.util.Map;

import morphy.user.Partnership;
import morphy.user.UserSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PartnershipService implements Service {
	protected static Log LOG = LogFactory.getLog(PartnershipService.class);
	private static final PartnershipService singletonInstance = new PartnershipService();
	
	private Map<UserSession,Partnership> partnershipMap;
	
	public static PartnershipService getInstance() {
		return singletonInstance;
	}
	
	public PartnershipService() {
		partnershipMap = new HashMap<UserSession,Partnership>(5);
	}
	
	/** Adds a partnership service between two players. */
	public void addPartnership(UserSession a,UserSession b) {
		Partnership p = new Partnership(a,b);
		partnershipMap.put(a,p);
		partnershipMap.put(b,p);
	}
	
	/** Destroys a partnership, where <tt>userSession</tt> is one of the partners on the team. */
	public void removePartnership(UserSession userSession) {
		Partnership p = partnershipMap.get(userSession);
		
		partnershipMap.remove(p.a);
		partnershipMap.remove(p.b);
	}
	
	public void removePartnership(Partnership p) {
		removePartnership(p.a);
	}

	public void dispose() {
		partnershipMap.clear();
		if (LOG.isInfoEnabled()) {
			LOG.info("PartnershipService disposed.");
		}
	}

	public Map<UserSession, Partnership> getPartnershipMap() {
		return partnershipMap;
	}
}
