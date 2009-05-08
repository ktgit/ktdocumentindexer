package com.knowledgetree.textextraction;

public class KTTextExtractorInterface {
	
	/**
	 * Extracts the content from a given file and writes the plain text output to a file
     *
	 * @param String contentFilename The source file containing the content to be extracted
     * @param String outputFilename The target file for saving the extracted text
	 */
	public int getTextFromFile(String contentFilename, String outputFilename) 
    {
        KTTextExtractor extractor = KTTextExtractor.get();
        int result = -1;
        
        try
        {
            result = extractor.ExtractTextFromFile(contentFilename, outputFilename);
        }
        catch(Exception ex)
        {
            extractor.getLogger().error(ex.getMessage());
            return -1;
        }
        return result;
	}
    
	/**
	 * Extracts the content from a data stream and returns the plain text in an SAX XML object
	 * @param data The data to be extracted.
	 * @return SAX XML
	 */
	public java.util.Map<String,String> getText(byte[] data) 
    {
        KTTextExtractor extractor = KTTextExtractor.get();
        return extractor.ExtractText(data);
	}
}
