package com.higedata.fprintr;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.Sides;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.Documents4jConversionSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import org.docx4j.openpackaging.packages.Filetype;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.documents4j.local.Documents4jLocalServices;

import com.jacob.activeX.*;
import com.jacob.com.*;

public class FilesPrinter implements ActionListener {
	
	JFrame frame;//mainframe
	private JPanel dataPanel = new JPanel();//where the data is shown and changed
	private JPanel changePanel = new JPanel();//nothing yet
	ScheduledExecutorService schEx = Executors.newSingleThreadScheduledExecutor();
	ScheduledFuture<?> printHandle;
	JTextField tfPrinter;
	JTextField tfDir;
	JTextField tfTimer;
	JButton bSetPrinter;
	JButton bSetDir;
	JButton bSetTimer;
	LocalDateTime date;
	
	private PrinterJob pjob = PrinterJob.getPrinterJob();
	private PrintService printer;	//what printer will be used
	private Path path;			//the directory from where the files will be printed
	private int printTimer;			//time (in minutes) between file checks
	private Path[] files;
	private boolean canPrintPDF;
	private PrintRequestAttributeSet attr;
	private ActiveXComponent oWord;
	private int copies;
	private boolean isDuplex;
	
	FilesPrinter() {
		//initVars();
		
		//this is the default ones, in case we have no configuration file
		//will also make and read configuration file ... one day ...
		this.printer = PrintServiceLookup.lookupDefaultPrintService();
		try {
			this.pjob.setPrintService(this.printer);
		} catch (PrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.path = Paths.get(System.getProperty("user.dir"));
		this.printTimer = 100;
		this.date = LocalDateTime.now();
		this.canPrintPDF = false;
		this.attr = new HashPrintRequestAttributeSet();
		this.attr.add(new Copies(1));
		this.copies = 1;
		this.attr.add(MediaSizeName.ISO_A4);
		this.attr.add(Sides.DUPLEX);
		this.isDuplex = true;
	}
	
	class PrintJob implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			date = LocalDateTime.now();
			checkLogFolder();
			checkFiles();
			printFiles();
		}
	}//for testing
	
	public JPanel getDataPanel() {
		return dataPanel;
	}
	
	public JPanel getChangePanel() {
		return changePanel;
	}


	public void gui() {
		this.frame = new JFrame("HigeFilesPrinter");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setLocationRelativeTo(null);
		//BorderLayout bl = new BorderLayout();
		//frame.getContentPane().setLayout(bl);
		this.initMenu();
		
		this.showData();
		
		this.frame.add(getDataPanel());
		
		this.frame.pack();
		this.frame.setVisible(true);
	}//display that gui!
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				FilesPrinter fpApp = new FilesPrinter();
				fpApp.gui();
			}
		});
		
		/*
		// TODO Auto-generated method stub
		FilesPrinter fpApp = new FilesPrinter();
		fpApp.setVisible(true);
		//fpApp.askNewPath();
		fpApp.printVar();
		fpApp.checkFiles();
		//checking how it works
		*/
		
	}
	
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu help = new JMenu("Help");
		menuBar.add(help);
		JMenuItem about = new JMenuItem("About");
		help.add(about);
		about.addActionListener(this);
		help.addMouseListener(mouseAction);//cosmetics?
		//only one thing in the menu
		//and it's the damn about page
		/*
		JMenuItem chPrinter = new JMenuItem("Choose Printer");
		chPrinter.addActionListener(this);
		chPrinter.addMouseListener(mouseAction);
		menuBar.add(chPrinter);
		JMenuItem setTiming = new JMenuItem("Set Print Timing");
		setTiming.addActionListener(this);
		setTiming.addMouseListener(mouseAction);
		menuBar.add(setTiming);
		*/
	}//to be replacing strings with constants
	
	private void showData() {

		GridBagLayout gbl = new GridBagLayout();
		dataPanel.setLayout(gbl);
		GridBagConstraints gc = new GridBagConstraints();
		
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.WEST;
		dataPanel.add(new JLabel("Current printer:"), gc);
		
		tfPrinter = new JTextField(24);
		tfPrinter.setEditable(false);
		tfPrinter.setText(this.getPrinter().getName());
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 1;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.WEST;
		dataPanel.add(tfPrinter, gc);
		
		bSetPrinter = new JButton();
		bSetPrinter.setText("Select Printer");
		bSetPrinter.setName("SetPrinterButton");//no use for now
		bSetPrinter.addActionListener(this);
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 2;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.BOTH;
		dataPanel.add(bSetPrinter, gc);
		
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.WEST;
		dataPanel.add(new JLabel("Working Directory:"), gc);
		
		tfDir = new JTextField(24);
		tfDir.setEditable(false);
		tfDir.setText(this.getPath().toString());
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 1;
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.WEST;
		dataPanel.add(tfDir, gc);
		
		bSetDir = new JButton();
		bSetDir.setText("Select Directory");
		bSetDir.setName("SetDirButton");//no use for now
		bSetDir.addActionListener(this);
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 2;
		gc.gridy = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.BOTH;
		dataPanel.add(bSetDir, gc);
		
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 0;
		gc.gridy = 2;
		gc.anchor = GridBagConstraints.WEST;
		dataPanel.add(new JLabel("Printing Timer:"), gc);
		
		tfTimer = new JTextField(24);
		tfTimer.setEditable(false);
		tfTimer.setText(String.valueOf(getPrintTimer() + " Minutes"));
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 1;
		gc.gridy = 2;
		gc.anchor = GridBagConstraints.WEST;
		dataPanel.add(tfTimer, gc);
		
		bSetTimer = new JButton();
		bSetTimer.setText("Set Print Timer");
		bSetTimer.setName("SetTimerButton");//no use for now
		bSetTimer.addActionListener(this);
		gc = new GridBagConstraints();
		gc.insets = new Insets(3,3,3,3);
		gc.gridx = 2;
		gc.gridy = 2;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.BOTH;
		dataPanel.add(bSetTimer, gc);
		//FROM HERE!!
		
		//for testing I will place a call to PrintJob() here, will later place after initializing all variables
		printHandle = schEx.scheduleWithFixedDelay(new PrintJob(), 1, getPrintTimer(), TimeUnit.SECONDS);
	}//show all the settings and buttons to change them
	
	
	void checkLogFolder() {
		String logPath = this.path.toString() + "/PrintLogs";
		if (!Files.isDirectory(Paths.get(logPath))) {
			try {
				Files.createDirectory(Paths.get(logPath));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//String day = date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);//use later
	}//checks the folder with the logs (and zips) so many TODOs
	
	void printPDF(String filePath) {
		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fileIn == null) {
			return;
		}
		if (this.canPrintPDF) {
			//build the document for printing
			DocFlavor format = DocFlavor.INPUT_STREAM.PDF;
			Doc document = new SimpleDoc(fileIn, format, null);
			//create the print job
			try {
				this.getPrinter().createPrintJob().print(document, this.attr);
			} catch (PrintException pe) {
				pe.printStackTrace();
			}
		} else {//printer doesn't natively know PDFs
			try {//we use Apache PDBox to make PDDoc from it
				PDDocument document = PDDocument.load(fileIn);
				this.pjob.setPageable(new PDFPageable(document));
				pjob.print(this.attr);//and we print that
				document.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {//we close the fileInputStream
			fileIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	String convertDocx(String filePath) {
		String tmpPath = "";
		try {
			File inFile = new File(filePath);
			InputStream is = new FileInputStream(inFile);
			WordprocessingMLPackage wmlp = WordprocessingMLPackage.load(is);
			tmpPath = new String(filePath + ".pdf");
			File tmpFile = new File(tmpPath);
			FileOutputStream fos = new FileOutputStream(tmpFile);
			Documents4jLocalServices exporter = new Documents4jLocalServices();
			exporter.export(wmlp, fos);
			fos.close();
			is.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return tmpPath;
	}
	
	void printFiles() {
		/**
		 * Method to go trough FilesPrinter.files[];
		 * If we find docx, we will convert them to pdf using docx4j;
		 * If the printer does not support direct pdf printing,
		 * we will use PDFBox library to print.
		 */
		for (Path path : this.files) {
			//p.toString();
			if ((path.getFileName().toString().endsWith("doc")) || (path.getFileName().toString().endsWith("docx"))) {//if file is docx
				/*
				String tmpPath = convertDocx(path.toString());
				//we made a pdf file out of it
				this.printPDF(tmpPath);
				//print the pdf
				 * 
				 */
				//convert a docx to a pdf and print it
				
				this.printDocx(path.toAbsolutePath().toString());
				
			}
			if ((path.getFileName().toString().endsWith("xls")) || (path.getFileName().toString().endsWith("xlsx"))) {
				this.printXlsx(path.toAbsolutePath().toString());
			}
			
			/* there is no support for xlsx for now, this code does nothing
			if (p.getFileName().toString().endsWith("xlsx")) {
				try {
					File inFile = p.toFile();
					System.out.println(inFile);
					InputStream is = new FileInputStream(inFile);
					SpreadsheetMLPackage smlp = SpreadsheetMLPackage.load(inFile);
					File tmpFile = new File(p.toString()+".pdf");
					FileOutputStream fos = new FileOutputStream(tmpFile);
					Documents4jLocalServices exporter = new Documents4jLocalServices();
					System.out.println("loaded");	
					//smlp.save(new File(p.toFile()+"v2.xlsx"));
					//exporter.export(smlp, fos);
					exporter.export(inFile, fos, DocumentType.MS_EXCEL);
					
					System.out.println("exporting xlsx");
					fos.close();
					is.close();
					System.out.println("close streams xlsx");
				} catch (Docx4JException | FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}//THERE is NO save as PDF for excel support, it tries to open word
			//maybe in future version I will use other libraries
			*/
			
		}
	}
	
	void printXlsx(String filePath) {
		ActiveXComponent oExcel = new ActiveXComponent("Excel.Application");
		oExcel.setProperty("Visible", new Variant(false));
		//oExcel.setProperty("ActivePrinter", new Variant(this.getPrinter().getName()));//hangs here
		/*
		while (!oExcel.getPropertyAsBoolean("Ready")) {
			//wait
		}
		*/
		Dispatch oWorkbooks = oExcel.getProperty("Workbooks").toDispatch();
		/*
		while (!oExcel.getPropertyAsBoolean("Ready")) {
			//wait
		}
		*/
		Dispatch oWorkbook = Dispatch.call(oWorkbooks, "Open", filePath).toDispatch();
		/*
		while (!oExcel.getPropertyAsBoolean("Ready")) {
			//wait
		}
		*/
		//oWorkbook = oExcel.getProperty("ThisWorkbook").toDispatch();
		
		Variant From = Variant.VT_MISSING;
		Variant To = Variant.DEFAULT;
		Variant Copies = new Variant(this.copies);
		Variant Preview = Variant.VT_FALSE;
		Variant ActivePrinter = oExcel.getProperty("ActivePrinter");
		Variant PrintToFile = Variant.VT_TRUE;
		Variant Collate = Variant.VT_TRUE;
		Variant PrToFileName = new Variant(filePath + ".pdf");//testing
		//Variant PrToFileName = Variant.VT_MISSING;
		System.out.println("Printing");
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
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			oExcel.invoke("Quit");
			ComThread.Release();
		}
		
	}
	
	
	void printDocx(String filePath) {
		//System.out.println(filePath);
		//System.out.println("Starting word");
		this.oWord = new ActiveXComponent("Word.Application");
		//System.out.println("setting visibility");
        this.oWord.setProperty("Visible", new Variant(false));
        //System.out.println("setting printer");
        //System.out.println(this.getPrinter().getName());
        //System.out.println(this.pjob.toString());
        this.oWord.setProperty("ActivePrinter", new Variant(this.getPrinter().getName()));
        //this.oWord.setProperty("ActivePrinter", new Variant("Microsoft Print to PDF"));//for testing
        //System.out.println("dispatching documents");
        Dispatch oDocuments = oWord.getProperty("Documents").toDispatch();
        Dispatch oDocument = Dispatch.call(oDocuments, "Open", filePath).toDispatch();
        
        //System.out.println("building arguments");
        Variant Background= new Variant(false);           
        Variant Append = new Variant(false);                       
        Variant Range = new Variant(0); //> print out all document                       
        //Variant OutputFileName = new Variant(filePath+".pdf");// for testing
        Variant OutputFileName = new Variant("");//switch from above
        Variant From = new Variant("");                       
        Variant To  = new Variant("");                       
        Variant Item  = new Variant(0);                       
        Variant Copies = new Variant(this.copies);
        Variant Pages = new Variant("");
        Variant PageType = new Variant(0);
        Variant PrintToFile = new Variant();
        Variant Collate = new Variant(true);
        Variant ActivePrinterMacGX = new Variant("");
        Variant ManualDuplexPrint = new Variant(this.isDuplex);
        Variant PrintZoomColumn = new Variant("");
        Variant PrintZoomRow = new Variant("");
        Variant PrintZoomPaperWidth = new Variant("");
        Variant PrintZoomPaperHeight = new Variant("");
        
         
        //System.out.println("building argument array");
        Object[] args=new Object[]{Background,Append,Range,OutputFileName, From, To, Item, Copies, Pages, PageType,
        		PrintToFile, Collate, ActivePrinterMacGX, ManualDuplexPrint, PrintZoomColumn, PrintZoomRow,
        		PrintZoomPaperWidth, PrintZoomPaperHeight};          
        //System.out.println("dispatching the printing");
        //Dispatch.callN(oDocument, "PrintOut"); this works
        //Dispatch.callN(oDocument, "PrintPreview"); this works
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
        Dispatch.callN(oWord, "Quit");
        this.oWord.safeRelease();
	}
	
	/**
	 * @return the printer
	 */
	PrintService getPrinter() {
		return this.printer;
	}
	/**
	 * @param 'printer' - the printer to set
	 */
	void setPrinter(PrintService printer) {
		this.printer = printer;
		try {
			this.pjob.setPrintService(this.printer);
		} catch (PrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.checkPDFCompat();
	}
	
	Path getPath() {
		return this.path;
	}
	void setPath(Path p) {
		this.path = p;
	}//setting the working directory//don't think i need this
	
	/**
	 * @return the printTimer
	 */
	int getPrintTimer() {
		return this.printTimer;
	}

	/**
	 * @param printTimer the printTimer to set
	 */
	void setPrintTimer(int printTimer) {
		this.printTimer = printTimer;
	}
	
	void askNewPath() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setApproveButtonText("Set");
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				if (fc.getSelectedFile().exists()) {
					Path s = fc.getSelectedFile().toPath();
					this.setPath(s);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}//will be called by a button press or something
	
	int askNewTimer() {
		int i;
		try {
			i = Integer.parseInt(JOptionPane.showInputDialog(null,
																"Select time between filechecks:",
																"Set Timer",
																JOptionPane.PLAIN_MESSAGE));
		} catch (Exception e) {
			i = this.getPrintTimer();
		}
		if ((i < 10) || (i > 1440)) {
			i = this.getPrintTimer();
		}
		return i;
	}
	
	/*
	void printVar() {
		JOptionPane.showMessageDialog(null, path);
	}
	*/
	
	PrintService askNewPrinter() {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		PrintService p = (PrintService)JOptionPane.showInputDialog(null,
												"Available printers:",
												"Select printer",
												JOptionPane.PLAIN_MESSAGE,
												null,
												services,
												services[0]);
		if (p == null) {
			return getPrinter();
		}
		return p;
	}//the method to set new printer
	
	/*
	 * void printFile(File f) {
	 * 
	 * }
	 */
	
	void checkFiles() {
		try (Stream<Path> paths = Files.list(this.getPath())) {
			this.files = paths
				.filter(Files::isRegularFile)//not folders or symbolic links or other stuff
				.toArray(Path[]::new);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//we go and check for files in $path and operate on them
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String choice = ae.getActionCommand();
		//to edit this
		if (choice.equals("Change Preferences")) {
			JOptionPane.showMessageDialog(null, "SELECTED");
		}
		if (choice.equals("About")) {
			//addcode
		}
		
		if (choice.equals("Select Directory")) {
			this.askNewPath();
			tfDir.setText(path.toString());
		}
		
		if (choice.equals("Select Printer")) {
			setPrinter(this.askNewPrinter());
			tfPrinter.setText(getPrinter().getName());
		}
		
		if (choice.equals("Set Print Timer")) {
			setPrintTimer(this.askNewTimer());
			tfTimer.setText(String.valueOf(getPrintTimer()) + " Minutes");
			printHandle.cancel(true);
			printHandle = schEx.scheduleWithFixedDelay(new PrintJob(), 1, getPrintTimer(), TimeUnit.SECONDS);
			//SECONDS will be set to MINUTES later
		}
		
	}
	
	private void checkPDFCompat() {
		PrintService service = this.getPrinter();
	    int count = 0;
	    for (DocFlavor docFlavor : service.getSupportedDocFlavors()) {
	        if (docFlavor.toString().contains("pdf")) {
	            count++;
	        }
	    }
	    if (count > 0) {
	        this.canPrintPDF = true;
	    } else {
	    	this.canPrintPDF = false;
	    }
	}
	
	private final static MouseListener mouseAction = new MouseAdapter() { //mouse event
		@Override
		public void mouseEntered(MouseEvent me) {
		    JMenuItem item = (JMenuItem) me.getSource();
		    item.setSelected(true);
		}
		@Override
		public void mouseExited(MouseEvent me) {
		    JMenuItem item = (JMenuItem) me.getSource();
		    item.setSelected(false);
		}
	};

}
