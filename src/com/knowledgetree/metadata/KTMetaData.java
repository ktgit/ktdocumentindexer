package com.knowledgetree.metadata;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.CustomProperty;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


@SuppressWarnings("unchecked")
public class KTMetaData {

	private static KTMetaData metaDataHandler = null;
    private Logger 						logger;
	
    private KTMetaData()
    {
        this.logger  = Logger.getLogger("com.knowledgetree.metadata");		 
        this.logger.info("Properties handler starting...");
    }
    
    
	public static KTMetaData get() {
		if(metaDataHandler == null) {
			metaDataHandler = new KTMetaData();
		}
		
		return metaDataHandler;
	}
	
    public int writeMetadata(String fileName, String targetFile, java.util.Map metadata)
    {
        this.logger.debug("POI Properties: Write metadata to file " + fileName);
        POIFSFileSystem poifs;
        
        try
        {
            // Create a POIFileSystem object from the input document
            FileInputStream inStream = new FileInputStream(fileName);
            poifs = new POIFSFileSystem(inStream);
            inStream.close();
        }
        catch (Exception ex) 
        {
            this.logger.error("POI Properties: Input file could not be opened: " + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
        
        // Get the POIFS document 
        DirectoryEntry dir = poifs.getRoot();
        
        // Read the document summary information. 
        DocumentSummaryInformation dsi;
        try
        {
            DocumentEntry dsiEntry = (DocumentEntry)dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            DocumentInputStream dis = new DocumentInputStream(dsiEntry);
            PropertySet ps = new PropertySet(dis);
            dis.close();
            dsi = new DocumentSummaryInformation(ps);
        }
        catch (Exception ex)
        {
            // There is no document summary information yet. We have to create a new one. 
            dsi = PropertySetFactory.newDocumentSummaryInformation();
            this.logger.debug("POI Properties: Creating new document summary information: " + ex.getMessage());
        }
        
        CustomProperties customProperties = dsi.getCustomProperties();
        
        if(customProperties == null) {
        	customProperties = new CustomProperties();
        }
        
        Set keys = metadata.keySet();
        Iterator iter = keys.iterator();
        
        // Iterate through the keys and set them as data onto the CustomProperties
        while(iter.hasNext()) { 
        	String key = (String)iter.next();
            String value = (String)metadata.get(key);
        	customProperties.put(key, value);
        }
        
        // We must set them, incase we instantiated a new object 
        dsi.setCustomProperties(customProperties);
        
        try {
        	// Write back the summary file 
        	dsi.write(dir, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        } catch(Exception ex) {
            this.logger.error("POI Properties: Could not write custom properties to document summary information: " + ex.getMessage());
        	ex.printStackTrace();
			return -1;
        }
        
        try
        {
            // Write output back to filesystem
            FileOutputStream out = new FileOutputStream(targetFile);
            poifs.writeFilesystem(out);
            out.close();
        }
        catch (Exception ex) 
        {
            this.logger.error("POI Properties: Could not write output to file: " + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
        
        
        return 0;
    }
	
		
	public java.util.Map readMetadata(String fileName) 
    {
        this.logger.debug("POI Properties: Read metadata from file " + fileName);
        
		// Initialise result
		java.util.Hashtable result = new java.util.Hashtable();
        POIFSFileSystem poifs;
        
        try
        {
            // Create a POIFileSystem object from the input document
            FileInputStream inStream = new FileInputStream(fileName);
            poifs = new POIFSFileSystem(inStream);
            inStream.close();
        }
        catch (Exception ex) 
        {
            this.logger.error("POI Properties: Input file could not be opened: " + ex.getMessage());
            ex.printStackTrace();
            
            result.put("status", "1");
            return result;
        }
        
        // Get the POIFS document 
        DirectoryEntry dir = poifs.getRoot();
        
        // Read the document summary information. 
        DocumentSummaryInformation dsi;
        try
        {
            DocumentEntry dsiEntry = (DocumentEntry)dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            DocumentInputStream dis = new DocumentInputStream(dsiEntry);
            PropertySet ps = new PropertySet(dis);
            dis.close();
            dsi = new DocumentSummaryInformation(ps);
        }
        catch (Exception ex)
        {
            // There is no document summary information yet. We have to create a new one. 
            dsi = PropertySetFactory.newDocumentSummaryInformation();
            this.logger.debug("POI Properties: Creating new document summary information: " + ex.getMessage());
        }
        
        // Get the custom properties
        CustomProperties customProperties = dsi.getCustomProperties();
        
        java.util.Hashtable metadata = new java.util.Hashtable();
        
        if(customProperties == null) {
            this.logger.debug("POI Properties: No custom properties have been defined.");
            
        	result.put("status", "0");
        	result.put("metadata", metadata);
        	return result;
        }
        
        Set keys = customProperties.keySet();
        Iterator iter = keys.iterator();
        
        // Iterate through the properties and convert them to readable values
        while(iter.hasNext()) { 
        	String key = (String)iter.next();
        	String val = (String) customProperties.get(key);
        	metadata.put(key, val);
        }
        
        result.put("status", "0");
        result.put("metadata", metadata);
        return result;
	}
}
