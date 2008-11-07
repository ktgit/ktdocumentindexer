package com.knowledgetree.textextraction;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;

public class KTTextExtractor {
	
	private static KTTextExtractor textExtractor = null;
	
	/**
	 * 
	 * @return
	 */
	public static KTTextExtractor get() {
		if(KTTextExtractor.textExtractor == null) {
			KTTextExtractor.textExtractor = new KTTextExtractor();
		}
		return KTTextExtractor.textExtractor;
	}

	/**
	 * 
	 * @param data
	 * @return
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
