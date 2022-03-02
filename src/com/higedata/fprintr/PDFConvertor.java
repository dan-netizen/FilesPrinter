package com.higedata.fprintr;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.docx4j.documents4j.local.Documents4jLocalServices;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;

public class PDFConvertor {
	/**
	 * this is just a test class
	 * to be deleted later
	 * @param args
	 * @throws Docx4JException
	 * @throws IOException
	 */

	public static void main(String[] args) throws Docx4JException, IOException {
		// TODO Auto-generated method stub
		File output = new File(System.getProperty("user.dir")+"/result.pdf");
		FileOutputStream fos = new FileOutputStream(output);
		
		WordprocessingMLPackage wmlp = new WordprocessingMLPackage().load(new File(System.getProperty("user.dir")+"/Hello.docx"));
		
		Documents4jLocalServices exporter = new Documents4jLocalServices();
		exporter.export(wmlp , fos); 
		
		fos.close();
	}

}
