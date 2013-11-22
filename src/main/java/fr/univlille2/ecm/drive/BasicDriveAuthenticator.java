/*(C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the GNU Lesser General Public License
* (LGPL) version 2.1 which accompanies this distribution, and is available at
* http://www.gnu.org/licenses/lgpl.html
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* Contributors:
*     Nuxeo - initial API and implementation
*
*/
package fr.univlille2.ecm.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.ecm.platform.ui.web.auth.interfaces.NuxeoAuthenticationPlugin;



import sun.misc.BASE64Decoder;
/**
 * Overrides nuxeo basic authenticator as automation client can login with user readable username
 * instead of user uid
 * user schema has to be overriden as the readble login is stored in a field called "hrUsername"
 * @author acordier 24/07/2013
 *
 */
public class BasicDriveAuthenticator implements NuxeoAuthenticationPlugin {

	protected static final String REALM_NAME_KEY = "RealmName";
	protected static final String FORCE_PROMPT_KEY = "ForcePromptURL";
	protected static final String AUTO_PROMPT_KEY = "AutoPrompt";
	protected static final String PROMPT_URL_KEY = "PromptUrl";
	protected static final String DEFAULT_REALMNAME = "Nuxeo 5";
	protected static final String BA_HEADER_NAME  = "WWW-Authenticate";

	protected String realmName;

	protected Boolean autoPrompt = false;

	protected List<String> forcePromptURLs;
	private Log logger = LogFactory.getLog(BasicDriveAuthenticator.class);
	
	public Boolean handleLoginPrompt(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, String baseURL) {
		logger.debug(BasicDriveAuthenticator.class.getSimpleName()+" : handleLoginPrompt");
		try {
			String baHeader = "Basic realm=\"" + realmName + '\"';
			httpResponse.addHeader(BA_HEADER_NAME, baHeader);
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public UserIdentificationInfo handleRetrieveIdentity(
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		
		logger.debug(BasicDriveAuthenticator.class.getSimpleName()+" : UserIdentificationInfo");
		logger.debug(httpRequest.getHeader("authorization"));
		String auth = httpRequest.getHeader("authorization");

		if (auth != null && auth.toLowerCase().startsWith("basic")) {
			int idx = auth.indexOf(' ');
			String b64userpassword = auth.substring(idx + 1);
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				byte[] clearUp = decoder.decodeBuffer(b64userpassword);
				String userpassword = new String(clearUp);

		
				String username = userpassword.split(":")[0];
				String password = userpassword.split(":")[1];
			
				username = Ul2AuthenticationUtils.uidFromLogin(username);// <- Custom

				
				return new UserIdentificationInfo(username, password); 

			} catch (IOException e) {
			
				e.printStackTrace();
			} catch (DirectoryException e) {

				e.printStackTrace();
			}
		}
		return null;
	}
	
	


	public Boolean needLoginPrompt(HttpServletRequest httpRequest) {
		logger.debug(BasicDriveAuthenticator.class.getSimpleName()+" : needLoginPrompt");
		if (autoPrompt) {
			return true;
		} else {
			String requestedURI = httpRequest.getRequestURI();
			String context = httpRequest.getContextPath() + '/';
			requestedURI = requestedURI.substring(context.length());
			for (String prefixURL : forcePromptURLs) {
				if (requestedURI.startsWith(prefixURL)) {
					return true;
				}
			}
			return false;
		}
	}

	public void initPlugin(Map<String, String> parameters) {
		logger.debug(BasicDriveAuthenticator.class.getSimpleName()+" : initPlugin");
		if (parameters.containsKey(REALM_NAME_KEY)) {
			realmName = parameters.get(REALM_NAME_KEY);
		} else {
			realmName = DEFAULT_REALMNAME;
		}

		if (parameters.containsKey(AUTO_PROMPT_KEY)) {
			autoPrompt = parameters.get(AUTO_PROMPT_KEY).equalsIgnoreCase("true");
		}

		forcePromptURLs = new ArrayList<String>();
		for (String key : parameters.keySet()) {
			if (key.startsWith(FORCE_PROMPT_KEY)) {
				forcePromptURLs.add(parameters.get(key));
			}
		}
	}

	public List<String> getUnAuthenticatedURLPrefix() {
		return null;
	}

}