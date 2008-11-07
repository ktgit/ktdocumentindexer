package com.knowledgetree.metadata;

public class KTMetaDataInterface {

	public java.util.Map writeCustomProperties(byte[] data, String mimeType, java.util.Map metadata) {
	
		
		KTMetaDataInserter inserter = KTMetaDataInserter.get(); 

		return inserter.addMetaData(data, mimeType, metadata);
	}
}
