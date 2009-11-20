package com.knowledgetree.openoffice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeException;
//import com.artofsolving.jodconverter.openoffice.connection.PipeOpenOfficeConnection;


/* Using a ResourcePool so our communications to the OpenOffice server are thread safe */
public class KTConverter extends ResourcePool {

	public static KTConverter       ktc     = null;
    private Logger 					logger;
    private int                     ooPort  = 8100;
    private String                  ooHost  = "127.0.0.1";
	
    /**
     * Returns the converter object
     * @return KTConverter
     */
	public static KTConverter get(String host, int port) {
		if(ktc == null) {
			ktc = new KTConverter(host, port);
		}
		return ktc;
	}
	
    /**
     * Constructor
     */
	public KTConverter(String host, int port) {
		super("OfficeManagers");
        
        // set the host and port to connect to open office on
        this.ooHost = host;
        this.ooPort = port;
        
        // set up logging
        this.logger  = Logger.getLogger("com.knowledgetree.openoffice");		 
        this.logger.info("JODConverter starting...");
	}
	
    /**
     * Convert the document based on the extension of the given source and target files.
     * If the target file has a .pdf extension then the source file is converted to a pdf document.
     *
     * @param String sourceFilename The document to be converted
     * @param String targetFilename The file to save the converted document to. The file extension determines the conversion type.
     */
    public int ConvertDocument(String sourceFilename, String targetFilename)
    {
        // Get a connection to open office - one is created if none exist or are available
        SocketOpenOfficeConnection office = (SocketOpenOfficeConnection)this.getResource();
        //PipeOpenOfficeConnection office = (PipeOpenOfficeConnection)this.getResource();
        if(office == null){
            this.logger.error("JODConverter: could not connect to Open Office");
            return -1;
        }
        
        this.logger.debug("JODConverter: creating OO Document converter");
        
        // Get the document converter
        OpenOfficeDocumentConverter converter = new OpenOfficeDocumentConverter(office);
        
        File source = new File(sourceFilename);
        File target = new File(targetFilename);
        
        try 
        {
            this.logger.debug("JODConverter: attempting conversion of: " + sourceFilename + " to: " + targetFilename);
            
            // Convert to the target document type
            converter.convert(source, target);
        }
        catch (Exception ex) 
        {
            this.logger.error("JODConverter: failure to convert file with message: " + ex.getMessage());
            return -1;
        }
        finally 
        {
            this.logger.debug("JODConverter: conversion complete, releasing office");
            
            // Release the connection to open office
            releaseResource(office);
        }
        return 0;
    }
    
	@Override
	protected Object createNewResource()
    {
        // Connect to OpenOffice on the given host and port
        try {
            SocketOpenOfficeConnection office = new SocketOpenOfficeConnection(this.ooHost, this.ooPort);
            //PipeOpenOfficeConnection office = new PipeOpenOfficeConnection();
            office.connect();
            
            this.logger.debug("JODConverter: connecting to OpenOffice on host: " + this.ooHost + " and port: " + this.ooPort);
            return office;
        }
        catch (Exception ex) {
            this.logger.error("JODConverter: failed to connect to OpenOffice on host: " + this.ooHost + " and port: " + this.ooPort + " with message: " + ex.getMessage());
            return null;
        }
	}
    
}