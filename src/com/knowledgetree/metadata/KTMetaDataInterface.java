package com.knowledgetree.metadata;

public class KTMetaDataInterface {

    public int writeProperty(String fileName, String targetFile, java.util.Map metadata)
    {
        KTMetaData inserter = KTMetaData.get();
        return inserter.writeMetadata(fileName, targetFile, metadata);
    }
    	
	public java.util.Map readMetadata(String fileName) 
    {
		KTMetaData inserter = KTMetaData.get(); 
		return inserter.readMetadata(fileName);
	}
}
