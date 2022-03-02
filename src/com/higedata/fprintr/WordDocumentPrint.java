package com.higedata.fprintr;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

class WordDocumentPrint implements DocumentPrintStrategy {
	//TODO Fix printer issue (add vars here? maybe fix copies also?)

	@Override
	public void printDocumnt(String documentPath, java.util.ArrayList<String> settings) {
		// TODO Auto-generated method stub
		
		ActiveXComponent oWord = new ActiveXComponent("Word.Application");
		oWord.setProperty("Visible", new Variant(false));
		oWord.setProperty("ActivePrinter", new Variant(oWord.getProperty("ActivePrinter")));//TODO printer issue
        //this.oWord.setProperty("ActivePrinter", new Variant("Microsoft 
        //	Print to PDF"));//for testing
        Dispatch oDocuments = oWord.getProperty("Documents").toDispatch();
        Dispatch oDocument = Dispatch.call(oDocuments, "Open", documentPath, new Variant(false), new Variant(true)).toDispatch();
        
        Variant Background= new Variant(false);           
        Variant Append = new Variant(false);                       
        Variant Range = new Variant(0); //> print out all document                       
        Variant OutputFileName = new Variant(documentPath+".pdf");// for testing
        //Variant OutputFileName = new Variant("");//switch from above
        Variant From = new Variant("");                       
        Variant To  = new Variant("");                       
        Variant Item  = new Variant(0);                       
        Variant Copies = new Variant(1);//TODO edit with nr of copies
        Variant Pages = new Variant("");
        Variant PageType = new Variant(0);
        Variant PrintToFile = new Variant();
        Variant Collate = new Variant(true);
        Variant ActivePrinterMacGX = new Variant("");
        Variant ManualDuplexPrint = new Variant(true);//TODO edit with isDuplex
        Variant PrintZoomColumn = new Variant("");
        Variant PrintZoomRow = new Variant("");
        Variant PrintZoomPaperWidth = new Variant("");
        Variant PrintZoomPaperHeight = new Variant("");
        
         
        //building argument array
        Object[] args=new Object[]{Background,Append,Range,OutputFileName, From, To, Item, Copies, Pages, PageType,
        		PrintToFile, Collate, ActivePrinterMacGX, ManualDuplexPrint, PrintZoomColumn, PrintZoomRow,
        		PrintZoomPaperWidth, PrintZoomPaperHeight};          
        /*
        Dispatch.callN(oDocument, "PrintOut", new Variant(false), new Variant(false), new Variant(0),
        		new Variant(filePath+".pdf"), new Variant(""), new Variant(""), new Variant(0),
        		new Variant(this.copies), new Variant(""), new Variant(0), new Variant(), new Variant(true),
        		new Variant(""), new Variant(this.isDuplex));
        * Used this to test the functionality of each argument, very precious experience.
        * Keeping this code for reminders
        */
        Dispatch.callN(oDocument, "PrintOut", args);
        Dispatch.callN(oDocument, "Close");
        try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Dispatch.callN(oWord, "Quit");
			oWord.safeRelease();
		}
	}

}
