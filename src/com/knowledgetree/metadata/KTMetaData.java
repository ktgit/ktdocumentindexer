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
import java.util.List;

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

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLProperties;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xslf.XSLFSlideShow;
//import org.apache.poi.xslf.usermodel.XSLFSlideShow;



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
        
        try
        {
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
            
                this.logger.debug("POI Properties: Key - " + key + "; Value - " + value);
            }
        
            // We must set them, incase we instantiated a new object 
            dsi.setCustomProperties(customProperties);
        }
        catch(Exception ex)
        {
            this.logger.error("POI Properties: Could not create custom properties: " + ex.getMessage());
        	ex.printStackTrace();
			return -1;
        }
        
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
	
		
	public java.util.Map<String, String> readMetadata(String fileName, String keyName) 
    {
        this.logger.debug("POI Properties: Read metadata from file " + fileName);
        
		// Initialise result
		java.util.Hashtable result = new java.util.Hashtable();
        
        // Initialise POI filesystem
        POIFSFileSystem poifs;
        
        try
        {
            this.logger.debug("POI Properties: Open document stream");
            
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
            this.logger.debug("POI Properties: Read in Document Summary Information");
                              
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
        
        this.logger.debug("POI Properties: Get the custom properties");
         
        // Get the custom properties
        CustomProperties customProperties = dsi.getCustomProperties();
        
        java.util.Hashtable metadata = new java.util.Hashtable();
        
        if(customProperties == null) {
            this.logger.debug("POI Properties: No custom properties have been defined.");
            
        	result.put("status", "0");
        	result.put("metadata", metadata);
        	return result;
        }
        
        try {
            this.logger.debug("POI Properties: Get the specified custom property");
            
            String val = (String) customProperties.get(keyName);
            if(val == null){
                this.logger.debug("POI Properties: Custom property is empty");
                
                result.put("status", "0");
                result.put("metadata", metadata);
                return result;
            }
            
            metadata.put(keyName, val);
        }
        catch (Exception ex) {
            this.logger.debug("POI Properties: Custom property appears empty, exception thrown: " + ex.getMessage());
            
            result.put("status", "1");
            result.put("metadata", metadata);
            return result;
        }
        
        result.put("status", "0");
        result.put("metadata", metadata);
        return result;
        
        /*
        this.logger.debug("POI Properties: Iterate through the custom properties");
        
        Set keys = customProperties.keySet();
        Iterator iter = keys.iterator();
        
        // Iterate through the properties and convert them to readable values
        while(iter.hasNext()) { 
        	String key = (String)iter.next();
        	String val = (String) customProperties.get(key);
        	metadata.put(key, val);
        }
        */
	}
    
    public java.util.Map<String, String> readOOXMLProperty(String fileName, int type, String keyName)
    {
        this.logger.debug("POI Properties - OOXML: Read metadata from file " + fileName);
        
        // Initialise result
		java.util.Hashtable result = new java.util.Hashtable();
        java.util.Hashtable metadata = new java.util.Hashtable();
        
        // Open the document
        POIXMLDocument poi_doc;
        try
        {
            this.logger.debug("POI Properties - OOXML: Open document stream");
           
            // Use the extension to determine and initialise the correct document type
            switch(type){
                case 1:
                    this.logger.debug("POI Properties - OOXML: File is of type docx");
                    FileInputStream inStream = new FileInputStream(fileName);
                    poi_doc = new XWPFDocument(inStream);
                    inStream.close();
                    break;
                case 2:
                    this.logger.debug("POI Properties - OOXML: File is of type xlsx");
                    poi_doc = new XSSFWorkbook(fileName);
                    break;
                case 3:
                    this.logger.debug("POI Properties - OOXML: File is of type pptx");
                    poi_doc = new XSLFSlideShow(fileName);
                    break;
                default:
                    this.logger.error("POI Properties - OOXML: Input file should be of type docx, xlsx or pptx.");
                    
                    result.put("status", "1");
                    result.put("metadata", metadata);
                    return result;
            }
        }
        catch (Exception ex) 
        {
            this.logger.error("POI Properties - OOXML: Input file could not be opened: " + ex.getMessage());
            ex.printStackTrace();
            
            result.put("status", "1");
            result.put("metadata", metadata);
            return result;
        }
                
        // Read in the properties
        POIXMLProperties.CustomProperties customProps;
        
        try{
            this.logger.debug("POI Properties - OOXML: Read custom properties");
            customProps = poi_doc.getProperties().getCustomProperties();
            
            if(customProps == null) {
                this.logger.debug("POI Properties - OOXML: No custom properties have been defined.");
                
                result.put("status", "0");
                result.put("metadata", metadata);
                return result;
            }
            
            org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties ctProps = poi_doc.getProperties().getCustomProperties().getUnderlyingProperties();
            org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty p;
            
            int size = ctProps.sizeOfPropertyArray();
            int count = 0;
            
            while(size != count){
                p = ctProps.getPropertyArray(count);
                String key = p.getName();
                
                if(key.equals(keyName)) {
                    String value = p.getLpwstr();
                    
                    metadata.put(key, value);
                    this.logger.debug("POI Properties - OOXML: Custom property - name " + key + "; value " + value);
                }
                
                count++;
            }
        }
        catch (Exception ex) {
            this.logger.error("POI Properties - OOXML: Custom properties could not be found: " + ex.getMessage());
            ex.printStackTrace();
            
            result.put("status", "1");
            result.put("metadata", metadata);
            return result;
        }
        
        result.put("status", "0");
        result.put("metadata", metadata);
        return result;
    }
    
    public int writeOOXMLProperty(String fileName, String targetFile, int type, java.util.Map metadata)
    {
        this.logger.debug("POI Properties - OOXML: Read metadata from file " + fileName);
        
        // Open the document
        POIXMLDocument poi_doc;
        try
        {
            this.logger.debug("POI Properties - OOXML: Open document stream");
            
            // Use the extension to determine and initialise the correct document type
            switch(type){
                    case 1:
                        this.logger.debug("POI Properties - OOXML: File is of type docx");
                        // pptx can only be opened using document stream, no file option
                        FileInputStream inStream = new FileInputStream(fileName);
                        poi_doc = new XWPFDocument(inStream);
                        inStream.close();
                        break;
                    case 2:
                        this.logger.debug("POI Properties - OOXML: File is of type xlsx");
                        poi_doc = new XSSFWorkbook(fileName);
                        break;
                    case 3:
                        this.logger.debug("POI Properties - OOXML: File is of type pptx");
                        // pptx can only be opened using the file, no document stream option
                        poi_doc = new XSLFSlideShow(fileName);
                        break;
                default:
                    this.logger.error("POI Properties - OOXML: Input file should be of type docx, xlsx or pptx.");
                    return -1;
            }
        }
        catch (Exception ex) 
        {
            this.logger.error("POI Properties - OOXML: Input file could not be opened: " + ex.getMessage());
            ex.printStackTrace();
            
            return -1;
        }
        
        // Read in the properties
        POIXMLProperties.CustomProperties customProps;
        
        try{
            this.logger.debug("POI Properties - OOXML: Read custom properties");
            customProps = poi_doc.getProperties().getCustomProperties();
        }
        catch (Exception ex) {
            this.logger.error("POI Properties - OOXML: Custom properties could not be found: " + ex.getMessage());
            ex.printStackTrace();
            
            return -1;
        }
        
        try{
            this.logger.debug("POI Properties - OOXML: Write custom properties");
            
            Set keys = metadata.keySet();
            Iterator iter = keys.iterator();
            
            // Iterate through the keys and set them as data onto the CustomProperties
            while(iter.hasNext()) { 
                String key = (String)iter.next();
                String value = (String)metadata.get(key);
                
                Boolean check = customProps.contains(key);
                if(check){
                    this.logger.debug("POI Properties: Key exists need to update");
                    
                    // This seems convoluted but the CustomProperties class has no update function
                    org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties ctProps = poi_doc.getProperties().getCustomProperties().getUnderlyingProperties();
                    org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty p;
                    
                    int size = ctProps.sizeOfPropertyArray();
                    int count = 0;
                    
                    while(size != count){
                        p = ctProps.getPropertyArray(count);
                        String keyName = p.getName();
                        
                        if(keyName.equals(key)) {
                            p.setLpwstr(value);
                            this.logger.debug("POI Properties - OOXML: Custom property - name " + keyName + "; updated value " + value);
                        }
                        count++;
                    }
                }else {
                    customProps.addProperty(key, value);
                }
                
                this.logger.debug("POI Properties: Key - " + key + "; Value - " + value);
            }
        }
        catch (Exception ex) {
            this.logger.error("POI Properties - OOXML: Couldn't add property - " + ex.getMessage());
            ex.printStackTrace();
            
            return -1;
        }
        
        try
        {
            this.logger.debug("POI Properties - OOXML: Write file to target " + targetFile);
            
            // Write output back to filesystem
            FileOutputStream out = new FileOutputStream(targetFile);
            poi_doc.write(out);
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
}
