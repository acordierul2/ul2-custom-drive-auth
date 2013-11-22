package fr.univlille2.ecm.drive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

public class Ul2AuthenticationUtils {
	private static Log logger = LogFactory.getLog(BasicDriveAuthenticator.class);
	
	// complementary method
		public static String uidFromLogin(String login) throws DirectoryException{
			String username; // output string
			
			// if given login is made from digits, then return
			Pattern pattern = Pattern.compile("^[0-9]*$");
			Matcher matcher = pattern.matcher(login);
			
			if(matcher.matches()){
				logger.debug(login + " authenticating");
				return login;
			}
			
			// else transform
			logger.debug("Getting uid from human readable login");
			DirectoryService directoryService=null;
			Directory  directory = null;
			try {
				directoryService = Framework.getService(DirectoryService.class); // get directory service
			} catch (Exception e) {
				logger.error("Failed to get " + DirectoryService.class.getSimpleName() + " service");
			}
			try {
				directory = directoryService.getDirectory("userDirectory"); // get user directory
			} catch (DirectoryException e) {
				logger.error(e.getMessage());
			}
			Session directorySession = null;
			try {
				directorySession = directory.getSession();
			} catch (DirectoryException e2) {
				e2.printStackTrace();
			}


			Map<String, Serializable> filter = new HashMap<String, Serializable>(); // query filter ...
			filter.put("hrUsername", login); // ...will filter user human readable username


			List<String> directoryResults = null;
			
			try {
				directoryResults = directorySession.getProjection(filter, "username");
			} catch (DirectoryException e) {
				logger.error(e.getMessage());
			} catch (ClientException e) {
				logger.error(e.getMessage());
			} 

			
			if(directoryResults.size()==1){
				username = directoryResults.get(0);
			}else{
				throw new DirectoryException("User result has to be unique");
			}
			
			logger.debug(String.format("Returning %s from %s", username, login));
			
			return username;
		}

}
