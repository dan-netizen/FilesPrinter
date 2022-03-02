package com.higedata.fprintr;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

class TableDocumentPrint implements DocumentPrintStrategy {
	//TODO other methods?
	//TODO additional 

	@Override
	public void printDocumnt(String documentPath, java.util.ArrayList<String> settings) {
		// TODO Auto-generated method stub
		ActiveXComponent oExcel = new ActiveXComponent("Excel.Application");
		oExcel.setProperty("Visible", new Variant(false));
		//oExcel.setProperty("ActivePrinter", new Variant(this
		//	.getPrinter().getName()));//hangs here
		Dispatch oWorkbooks = oExcel.getProperty("Workbooks").toDispatch();
		Dispatch oWorkbook = Dispatch.call(oWorkbooks, "Open", documentPath).toDispatch();
		
		Variant From = Variant.VT_MISSING;
		Variant To = Variant.DEFAULT;
		Variant Copies = new Variant(1);//TODO need to get input from config
		Variant Preview = Variant.VT_FALSE;
		Variant ActivePrinter = oExcel.getProperty("ActivePrinter");//TODO Printer issue
		Variant PrintToFile = Variant.VT_TRUE;
		Variant Collate = Variant.VT_TRUE;
		Variant PrToFileName = new Variant(documentPath + ".pdf");//testing
		//Variant PrToFileName = Variant.VT_MISSING;
		Object[] args = new Object[] {From, To, Copies, Preview, ActivePrinter, PrintToFile, Collate, PrToFileName};
		/*
		Dispatch.callN(oWorkbook, "PrintOut", Variant.VT_MISSING, Variant.DEFAULT, new Variant(this.copies),
				Variant.VT_FALSE, oExcel.getProperty("ActivePrinter"), Variant.VT_TRUE, Variant.VT_TRUE,
				new Variant(filePath + ".pdf"));
		*/
		Dispatch.callN(oWorkbook, "PrintOut", args);
		Dispatch.callN(oWorkbook, "Close");
		Dispatch.callN(oWorkbooks, "Close");
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			oExcel.invoke("Quit");
			ComThread.Release();
		}
	}

}
