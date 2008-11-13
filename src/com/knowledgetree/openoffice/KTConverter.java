package com.knowledgetree.openoffice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.ManagedProcessOfficeManager;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.artofsolving.jodconverter.util.OsUtils;



public class KTConverter {

	public static KTConverter ktc = null;
	
	
	public static KTConverter get() {
		if(ktc == null) {
			ktc = new KTConverter();
		}
		return ktc;
	}
	
	public KTConverter() {
		// TODO: Global instantiation of Office Manager
	}
	
	// TODO: Complete implementation of method. Add configuration options.
	public java.util.Map ConvertDocument(byte[] data, String fromType, String toType) {
		Hashtable result = new Hashtable();
		
		OfficeManager officeManager = getOfficeManager(8100);
        officeManager.start();
        
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
        
        
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        try {
            if (fromType == null) {
                converter.convert(tmpInputFile, tmpOutputFile);
            }
        } finally {
            officeManager.stop();
        }

		
		return result;
	}
	
	private static OfficeManager getOfficeManager(int port) {
        String officeHome = System.getenv("OFFICE_HOME");
        if (officeHome == null) {
            //TODO try searching in standard locations
            throw new RuntimeException("Please set your OFFICE_HOME environment variable.");
        }
        String acceptString = "socket,host=127.0.0.1,port=" + port;
        return new ManagedProcessOfficeManager(new File(officeHome), guessDefaultProfileDir(), acceptString);
    }
    

    private static File guessDefaultProfileDir() {
        if (OsUtils.isWindows()) {
            return new File(System.getenv("APPDATA"), "OpenOffice.org2");
        } else {
            return new File(System.getProperty("user.home"), ".openoffice.org2");
        }
    }

}
