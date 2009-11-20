package com.knowledgetree.textextraction;

import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.exception.TikaException;

import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class KTTextExtractor {
	
	private static KTTextExtractor      textExtractor = null;
    private Logger 						logger;
	
    
	/**
	 * Returns the existing KTTextExtractor object
	 * @return KTTextExtractor
	 */
	public static KTTextExtractor get()
    {
		if(KTTextExtractor.textExtractor == null) {
			KTTextExtractor.textExtractor = new KTTextExtractor();
		}
		return KTTextExtractor.textExtractor;
	}

    /**
     * Constructor for KTTextExtractor
     */
    private KTTextExtractor()
    {
        this.logger  = Logger.getLogger("com.knowledgetree.textextraction");		 
        this.logger.info("Text Extraction starting...");
    }
    
    /**
     * Returns the log object
     */
	public Logger getLogger()			
    { 
        return logger; 
    }
    
	/**
	 * Extracts the content from a given file and writes the plain text output to a file
     *
	 * @param String contentFilename The source file containing the content to be extracted
     * @param String outputFilename The target file for saving the extracted text
     * @return Integer 0 on success | -1 on failure
	 */
	public int ExtractTextFromFile(String contentFilename, String outputFilename) 
    {
        this.logger.debug("Text Extractor: file in: " + contentFilename + "; file out: " + outputFilename);
        
        try 
        {
            // Open streams to the source file and target/output file
            FileInputStream inStream = new FileInputStream(contentFilename);
            FileOutputStream outStream = new FileOutputStream(outputFilename);
            
            // Use a writer to handle the output from the tika extractor
            OutputStreamWriter outFile = new OutputStreamWriter(outStream, "UTF8");
            ContentHandler textHandler = new BodyContentHandler(outFile);
                    
            // Instantiate the Tika 'AutoDetect' Parser 
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();

            try {
                // Parse the file, the output is automatically written to the target file
                parser.parse(inStream, textHandler, metadata);
                
                outFile.close();
                inStream.close();
            }
            catch (Exception ex) {
                this.logger.error("Text Extractor: Failed with message - " + ex.getMessage());
                return -1;
            }
        }
        catch (Exception ex) 
        {
            this.logger.error("Text Extractor: File could not be found - " + ex.getMessage());
            return -1;
        }
        return 0;
	}
    
	/**
	 * Extracts the content from a data stream and returns the plain text in an SAX XML object
	 * @param data The data to be extracted.
	 * @return SAX XML
	 */
	public java.util.Map<String, String> ExtractText(byte[] data) {
        
		Hashtable<String,String> result = new Hashtable<String,String>();
		
		/* We use no files so we use the ByteArrayInputStream to simulate an input stream */
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		
		/* Instantiate the Tika 'AutoDetect' Parser */
		AutoDetectParser p = new AutoDetectParser();
		
		/* Create a new ContentHandler interface to store our content */
		StringHandler sh = new StringHandler();
		try {
			p.parse(stream, sh, new Metadata());
		} catch(Exception ex) {
			result.put("status","1");
			result.put("message", ex.getMessage());
			return result;
		}
		result.put("status", "0");
		result.put("text", sh.getString());
		return result;
	}
	
	
}
