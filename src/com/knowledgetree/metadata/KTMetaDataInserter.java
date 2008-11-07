package com.knowledgetree.metadata;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


@SuppressWarnings("unchecked")
public class KTMetaDataInserter {

	private static KTMetaDataInserter metaDataInserter = null;
	
	public KTMetaDataInserter() {
		System.out.println("Instantiated");
	}
	
	public static KTMetaDataInserter get() {
		if(metaDataInserter == null) {
			metaDataInserter = new KTMetaDataInserter();
		}
		
		return metaDataInserter;
	}
	
	
	
	public java.util.Map addMetaData(byte[] data, String mimeType, java.util.Map<String, String> metaData) {
		/* Prepare our result */
		java.util.Hashtable result = new java.util.Hashtable();
		POIFSFileSystem poifs;
		
		/* Converting our byte array into a byte stream for the POIFS adapter */
		ByteArrayInputStream ba = new ByteArrayInputStream(data);
		
		try {
		    poifs = new POIFSFileSystem(ba);
		} catch(IOException e) {
			/* Probably not a valid OLE2 document */
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
			return result;
		}
		
		DirectoryEntry dir = poifs.getRoot();
		
		DocumentSummaryInformation dsi;
        try
        {
        	/* We are now going to get the virtual document out of the POIFS containing metadata */
            DocumentEntry dsiEntry = (DocumentEntry)
                dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
            DocumentInputStream dis = new DocumentInputStream(dsiEntry);
            PropertySet ps = new PropertySet(dis);
            dis.close();
            dsi = new DocumentSummaryInformation(ps);
        }
        catch (Exception ex)
        {
            /* There is no document summary information yet. We have to create a
             * new one. */
            dsi = PropertySetFactory.newDocumentSummaryInformation();
        }
		
        
        
        CustomProperties customProperties = dsi.getCustomProperties();
        
        if(customProperties == null) {
        	customProperties = new CustomProperties();
        }
        
        
       
        
        Set keys = metaData.keySet();
        Iterator iter = keys.iterator();
        
        while(iter.hasNext()) { /* Iterate through the keys and set them as data onto the CustomProperties */
        	String key = (String)iter.next();
        	customProperties.put(key, (String)metaData.get(key));
        }
        
        /* We must set them, incase we instantiated a new object */
        dsi.setCustomProperties(customProperties);
        
        try {
        	/* Write back the summary file */
        	dsi.write(dir, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        } catch(Exception ex) {
        	ex.printStackTrace();
        	result.put("status", "1");
			result.put("message", ex.getMessage());
			return result;
        }
        
       
        /* Put it back into a byte array for us */
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			poifs.writeFilesystem(out);
		} catch(Exception ex) {
        	ex.printStackTrace();
        	result.put("status", "1");
			result.put("message", ex.getMessage());
			return result;
		}
		
	
		result.put("status", "0");
		result.put("data", out.toByteArray());
		
		
		return result;
	}
}
