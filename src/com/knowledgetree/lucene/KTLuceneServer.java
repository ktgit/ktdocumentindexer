/**
 *
 * The KTLuceneServer main().
 *
 * @license
 *
 */

package com.knowledgetree.lucene;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.knowledgetree.lucene.core.IndexerManager;

public class KTLuceneServer {

	public static WebServer webServer;
	
	public static void main(String[] args) throws Exception 
	{
		// load properties from configuration file
		PropertyConfigurator.configure(IndexerManager.KnowledgeTreeLoggingProperties);
		
		IndexerManager manager = IndexerManager.get();
		
		// setup the basic web server
		webServer = new WebServer(manager.getPort());
		
		// check for paranoid settings - if the xmlrpc server should only allow connections from certain IPs
		if (manager.isParanoid())
		{
			manager.getLogger().info("Server in paranoid mode!");
			webServer.setParanoid(true);
			String [] allow = manager.getAcceptableIps();
	    	for(int i=0;i<allow.length;i++)
	    	{
	    		String ip = allow[i];
	    		manager.getLogger().info("\tAccepting connections from: " + ip);
	    		webServer.acceptClient(ip);	    	
	    	}
		}
		else
		{
			manager.getLogger().info("Server trusting everyone!");
		}
	    
		// setup the xmlrpc server 	
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();      
        PropertyHandlerMapping phm = new PropertyHandlerMapping();     
       
        phm.addHandler("indexer", com.knowledgetree.lucene.core.IndexerInterface.class);
        xmlRpcServer.setHandlerMapping(phm);
      
        XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);
        
        manager.getLogger().info("Starting web server on port: " + manager.getPort());
        
        // start the server finally!
        webServer.start();
	}

}
