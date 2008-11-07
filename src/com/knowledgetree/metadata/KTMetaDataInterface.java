package com.knowledgetree.metadata;

public class KTMetaDataInterface {

	public java.util.Map writeCustomProperties(byte[] data, String mimeType, java.util.Map metadata) {
	
		
		KTMetaData inserter = KTMetaData.get(); 

		return inserter.addMetaData(data, mimeType, metadata);
	}
	
	public java.util.Map readMetaData(byte[] data) {
		KTMetaData inserter = KTMetaData.get(); 

		return inserter.readMetaData(data);
	}
}
