package com.knowledgetree.textextraction;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class StringHandler implements ContentHandler {

	
	private String buffer;
	
	public StringHandler() {
		this.buffer = new String();
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		for(int i=0;i<ch.length;i++) {
			this.buffer += ch[i];
		}
	}

	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	public void endElement(String uri, String localName, String name)
			throws SAXException {
		// TODO Auto-generated method stub
		this.buffer += " ";
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub

	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
	}

	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
	}

	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		// TODO Auto-generated method stub
		
		/* For now we will store the data in a private buffer */
		int l = atts.getLength();
		for(int i=0;i<l;i++) {
			this.buffer += " " + atts.getValue(i) ;
		}
	}
	
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub

	}
	
	public String getString() {
		return this.buffer;
	}

}
