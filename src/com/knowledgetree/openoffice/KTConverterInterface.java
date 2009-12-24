package com.knowledgetree.openoffice;

//import com.knowledgetree.metadata.KTMetaData;

public class KTConverterInterface {
	
	public int convertDocument(String sourceFilename, String targetFilename, String host, int port) {	
		KTConverter converter = KTConverter.get(host, port); 
		return converter.ConvertDocument(sourceFilename, targetFilename);
	}
}
