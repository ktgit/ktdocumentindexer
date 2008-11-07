/**
 *
 * The KTLuceneServer main().
 *
 * @license
 *
 */

package com.knowledgetree.xmlrpc;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.knowledgetree.lucene.IndexerManager;

public class KTXmlRpcServer {

	public static WebServer webServer;
	private static KTXmlRpcServer xmlRpcServer;
	
	private Logger logger;
	private int port = 8875;
	private boolean paranoid = true;
	private String 						clientIps 			= "127.0.0.1,192.168.1.1";
	private String						propertiesFilename 	= "KnowledgeTreeIndexer.properties";
	private String						authenticationToken = "";
	
	public static void main(String[] args) throws Exception 
	{
		// load properties from configuration file
		PropertyConfigurator.configure(IndexerManager.KnowledgeTreeLoggingProperties);
		
		KTXmlRpcServer manager = KTXmlRpcServer.get();
		
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
        
        /* Start-up the services */
        com.knowledgetree.lucene.IndexerManager.get();
       
        phm.addHandler("indexer", com.knowledgetree.lucene.IndexerInterface.class);        
        phm.addHandler("textextraction", com.knowledgetree.textextraction.KTTextExtractorInterface.class);
        phm.addHandler("metadata",com.knowledgetree.metadata.KTMetaDataInterface.class);
        phm.addHandler("control", com.knowledgetree.xmlrpc.KTXmlRpcServerInterface.class);
        
        xmlRpcServer.setHandlerMapping(phm);
      
        XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);
        
        manager.getLogger().info("Starting web server on port: " + manager.getPort());
        
        // start the server finally!
        webServer.start();
	}
	
	public KTXmlRpcServer() {
		this.logger  = Logger.getLogger("com.knowledgetree");		 
		this.logger.info("XML-RPC Server starting up...");
		// get the default parameters
		// load properties
		
		this.logger.info("Loading properties file: " + this.propertiesFilename);
		Properties properties = new Properties();
		try
		{
			FileInputStream in = new FileInputStream(this.propertiesFilename);
			properties.load(in);
			in.close();
		}
		catch(Exception ex)
		{
			this.logger.error("Problem loading properties: " + ex.getMessage());
			this.logger.error("Shutting down");
			System.exit(1);
		}
		
		this.port = Integer.parseInt(properties.getProperty("server.port", Integer.toString(this.port)));
		//this.threadDelay = Integer.parseInt(properties.getProperty("thread.delay", Integer.toString(this.threadDelay)));
		this.paranoid = Boolean.parseBoolean(properties.getProperty("server.paranoid", Boolean.toString(this.paranoid)));		
		this.clientIps = properties.getProperty("server.accept", clientIps);
		this.authenticationToken =  properties.getProperty("auth.token", this.authenticationToken);
	}
	
	public static KTXmlRpcServer get() {
		if(KTXmlRpcServer.xmlRpcServer == null) {
			KTXmlRpcServer.xmlRpcServer = new KTXmlRpcServer();
		}
		return KTXmlRpcServer.xmlRpcServer;
	}

	public boolean authenticate(String token) {
		// TODO: THIS IS REALLY PRIMITIVE!!!! WE SHOULD MD5 
		return this.authenticationToken.equals(token);
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public int getPort() 				{ return port; }
	public boolean isParanoid() 		{ return paranoid; }
	public String[] getAcceptableIps() 	{ return clientIps.split(","); }
	
	/**
	 * Shut the webserver down.
	 *
	 */
	public void shutdown()
	{
		this.logger.info("Shutting down...");
		KTXmlRpcServer.webServer.shutdown();
	}

}
