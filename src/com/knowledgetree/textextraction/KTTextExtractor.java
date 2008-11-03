package com.knowledgetree.textextraction;

public class KTTextExtractor {
	
	private static KTTextExtractor textExtractor = null;
	
	public static KTTextExtractor get() {
		if(KTTextExtractor.textExtractor == null) {
			KTTextExtractor.textExtractor = new KTTextExtractor();
		}
		return KTTextExtractor.textExtractor;
	}

	public KTTextExtractor() {
		System.out.println("I was instantiated");
	}
	
	public void sayHello() {
		System.out.println("I was called");
	}
	
	
}
