package com.knowledgetree.openoffice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.DocumentFormat;
import org.artofsolving.jodconverter.DocumentFormatRegistry;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.ExternalProcessOfficeManager;
import org.artofsolving.jodconverter.office.ManagedProcessOfficeManager;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.artofsolving.jodconverter.util.OsUtils;


/* Using a ResourcePool so our communications to the OpenOffice server are thread safe */
public class KTConverter extends ResourcePool {

	public static KTConverter ktc = null;
	
	/* No use besides testing */
	public static void main(String[] args) {
		KTConverter ktc = KTConverter.get();
		byte[] data;
		try {
			data = getBytesFromFile(new File(args[0]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
			return;
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(new File(args[1]));
			fos.write((byte[])ktc.ConvertDocument(data, "pdf").get("data"));
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
		
		
	}
	
	public static KTConverter get() {
		if(ktc == null) {
			ktc = new KTConverter();
		}
		return ktc;
	}
	
	public KTConverter() {
		super("OfficeManagers");
	}
	
	public java.util.Map ConvertDocument(byte[] data, String toExtension) {
		Hashtable result = new Hashtable();
        
        File tmpInputFile;
        File tmpOutputFile;
		try {
			tmpInputFile = File.createTempFile("ktinput", null);
			FileOutputStream fos = new FileOutputStream(tmpInputFile);
			fos.write(data);
			fos.flush();	
			fos.close();
			
			tmpOutputFile = File.createTempFile("ktoutput", null);
		} catch (IOException e) {
			e.printStackTrace();
			result.put("status", "1");
			result.put("message", e.getMessage());
			return result;
		}
        
		DocumentFormatRegistry df = new DefaultDocumentFormatRegistry();

		/* Consume off the pool */
		ExternalProcessOfficeManager office = (ExternalProcessOfficeManager)this.getResource();
		DocumentFormat ddf = df.getFormatByExtension(toExtension);
		
		if(ddf == null) {
			/* Unsupported format */
			result.put("status", "1");
			result.put("message", "Unsupported format");
			return result;
		}
        
        OfficeDocumentConverter converter = new OfficeDocumentConverter(office);
        try {
                converter.convert(tmpInputFile, tmpOutputFile, ddf);
        } finally {
        	/* Produce back to the pool */
            this.releaseResource( office );
        }
        
        byte[] fileData;
        
        try {
        	fileData = getBytesFromFile(tmpOutputFile);
        } catch(IOException ex) {
        	ex.printStackTrace();
        	result.put("status", "1");
        	result.put("message", ex.getMessage());
        	return result;
        }

        result.put("status", "0");
        result.put("data", fileData);
		
        /* Make sure we clean up */
        tmpInputFile.delete();
        tmpOutputFile.delete();
        
		return result;
	}
	
	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

	@Override
	protected Object createNewResource() {
		ExternalProcessOfficeManager office = new ExternalProcessOfficeManager();
		office.setConnectOnStart(true);
		office.start();
		return office;
	}
	

}
