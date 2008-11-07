package com.knowledgetree.textextraction;

public class KTTextExtractorInterface {
	
	public java.util.Map<String,String> getText(byte[] data) {
		return KTTextExtractor.get().ExtractText(data);
	}
}
