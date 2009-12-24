package com.knowledgetree.metadata;

public class KTMetaDataInterface {

    public int writeProperty(String fileName, String targetFile, java.util.Map metadata)
    {
        KTMetaData inserter = KTMetaData.get();
        return inserter.writeMetadata(fileName, targetFile, metadata);
    }
    	
	public java.util.Map<String, String> readMetadata(String fileName, String keyName) 
    {
		KTMetaData inserter = KTMetaData.get(); 
		return inserter.readMetadata(fileName, keyName);
    }
    
	public java.util.Map<String, String> readOOXMLProperty(String fileName, int type, String keyName) 
    {
		KTMetaData inserter = KTMetaData.get(); 
		return inserter.readOOXMLProperty(fileName, type, keyName);
    }
    
	public int writeOOXMLProperty(String fileName, String targetFile, int type, java.util.Map metadata) 
    {
		KTMetaData inserter = KTMetaData.get(); 
		return inserter.writeOOXMLProperty(fileName, targetFile, type, metadata);
    }    
}
