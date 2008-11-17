package com.knowledgetree.openoffice;

import com.knowledgetree.metadata.KTMetaData;

public class KTConverterInterface {
	
	public java.util.Map convertDocument(byte[] data, String toExtension) {	
		KTConverter converter = KTConverter.get(); 
		return converter.ConvertDocument(data, toExtension);
	}
}
