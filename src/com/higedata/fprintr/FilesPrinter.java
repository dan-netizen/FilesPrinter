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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
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
import org.eclipse.persistence.internal.libraries.asm.util.Printer;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
//import com.jacob.activeX.*;
//import com.jacob.com.*; no use importing everything, right?

public class FilesPrinter implements ActionListener {
	
	JFrame frame;//mainframe
	private JPanel dataPanel = new JPanel();//where data is shown and changed
	private JPanel changePanel = new JPanel();//nothing yet
	ScheduledExecutorService schEx = Executors.newSingleThreadScheduledExecutor();
	ScheduledFuture<?> printHandle;
	JTextField tfPrinter;
	JTextField tfDir;
	JTextField tfTimer;
	JButton bSetPrinter;
	JButton bSetDir;
	JButton bSetTimer;
	LocalDateTime dateTime;
	LocalDate date;
	LocalTime time;
	
	private PrinterJob pjob = PrinterJob.getPrinterJob();
	private PrintService printer;	//what printer will be used
	private Path path;			//directory from where the files will be printed
	private int printTimer;			//time (in minutes) between file checks
	private Path[] files;
	private boolean canPrintPDF;
	private PrintRequestAttributeSet attr;
	private ActiveXComponent oWord;
	private int copies;
	private boolean isDuplex;
	private String logPath, logPathDaily;
	
	/*
	 * The settings are:
	 * filesPath?, printer, printTimer, canPrintPDF, numberCopies, isDuplex
	 */
	
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
		//this.date = LocalDateTime.now(); old
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
			dateTime = LocalDateTime.now();//update the dateTime
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


	public void showGui() {
		this.frame = new JFrame("HigeFilesPrinter");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setLocationRelativeTo(null);
		//BorderLayout bl = new BorderLayout();
		//frame.getContentPane().setLayout(bl);
		this.initMenu();
		
		this.showData();
		
		this.frame.add(getDataPanel());
		
		this.frame.pack();
		this.frame.setResizable(false);
		this.frame.setVisible(true);
	}//display that gui!
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				FilesPrinter fpApp = new FilesPrinter();
				fpApp.showGui();
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
		
		//for testing I will place a call to PrintJob() here,
		//will later place after initializing all variables
		printHandle = schEx.scheduleWithFixedDelay(new PrintJob(), 1,
				getPrintTimer(), TimeUnit.SECONDS);
	}//show all the settings and buttons to change them
	
	
	void checkLogFolder() {
		logPath = this.path.toString() + "\\" + "PrintLogs";
		if (!Files.isDirectory(Paths.get(logPath))) {
			try {
				Files.createDirectory(Paths.get(logPath));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		date = dateTime.toLocalDate();
		logPathDaily = logPath + "\\" + "printlist" + date.toString() + ".txt";
		if (!Files.isRegularFile(Paths.get(logPathDaily))) { 
			try {
				Files.createFile(Paths.get(logPathDaily));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//time = dateTime.toLocalTime(); use when printing
	}//checks the folder with the logs (and zips) so many TODOs
	
	
	/*
	 * Method that returns true if the configuration file exists, false otherwise
	 * Could be made static?
	 */
	boolean configurationFileExists() {
		Path programPath = java.nio.file.Paths.get(System.getProperty("user.dir"));
		programPath.resolve("\\settings.cfg");
		if (java.nio.file.Files.isRegularFile(programPath, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Method that saves the given settings ArrayList in the configuration file
	 */
	void saveSettingsToFile(ArrayList<String> settings) {
		try (FileWriter fileWriter = new FileWriter("settings.cfg");
				PrintWriter printWriter = new PrintWriter(fileWriter)){
			settings.forEach(element -> printWriter.println(element));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Method that reads the settings from the configuration file (line by line)
	 * and returns them as an ArrayList<String>
	 */
	ArrayList<String> readSettingsArrayFromFile() {
		ArrayList<String> settings = null;
		Path filePath = java.nio.file.Paths.get(System.getProperty("user.dir"));
		filePath.resolve("\\settings.cfg");
		try {
			settings = new ArrayList<String>(Files.readAllLines(filePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return settings;
	}
	
	
	
	void printPDF(String filePath) {
		/*
		 * Checks if printer natively supports PDF printing.
		 * Directly prints the PDF file or uses Apache PDBox to build
		 * a PDDocument out of it and print that.
		 */
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
				logPrint(filePath);
			} catch (PrintException pe) {
				pe.printStackTrace();
			}
		} else {//printer doesn't natively know PDFs
			try {//we use Apache PDBox to make PDDoc from it
				PDDocument document = PDDocument.load(fileIn);
				this.pjob.setPageable(new PDFPageable(document));
				this.pjob.print(this.attr);//and we print that
				document.close();
				logPrint(filePath);
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
	}//printing the PDF

	
	String getFormat(String theFile) {
		theFile = theFile.substring(theFile.lastIndexOf('.') + 1);
		theFile.toLowerCase(Locale.ROOT);
		return theFile;
	}
	
	void printFiles() {
		/*
		 * Method to go trough FilesPrinter.files[];
		 * If we find doc/x or xls/x we use office to print,
		 * making use of jacob project.
		 * If the printer does not support direct pdf printing,
		 * we will use PDFBox library to print.
		 */
		for (Path path : this.files) {
			String aFile = getFormat(path.getFileName().toString());
			switch(aFile) {
			case "pdf":
				this.printPDF(path.toAbsolutePath().toString());
				this.sendToZip(path.toAbsolutePath().toString());
				break;
			case "doc":
			case "docx":
			case "odt":
			case "txt":
			case "rtf":
				this.printDocx(path.toAbsolutePath().toString());
				this.sendToZip(path.toAbsolutePath().toString());
				break;
			case "xls":
			case "xlsx":
			case "ods":
				this.printXlsx(path.toAbsolutePath().toString());
				this.sendToZip(path.toAbsolutePath().toString());
				break;
			default:
				//need to treat cases with images + exceptions
				break;
			}		
		}
	}//end of printFiles
	
	void sendToZip(String filePath) {
		String zipPath = this.logPath + "\\" + LocalDate.now().toString() + ".zip";
		ZipFile zf = new ZipFile(zipPath);
		File sentFile = new File(filePath);
		try {
			zf.addFile(sentFile);
			sentFile.delete();
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		try {
			zf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void logPrint(String printedFile) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileOutputStream(new File(this.logPathDaily), true));
			pw.println(printedFile + "\t" + LocalTime.now().toString());
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void printXlsx(String filePath) {
		/*
		 * Uses jacob to call msexcel, open the spreadsheet and print it
		 */
		System.out.println("printing xls "+filePath);
		ActiveXComponent oExcel = new ActiveXComponent("Excel.Application");
		oExcel.setProperty("Visible", new Variant(false));
		//oExcel.setProperty("ActivePrinter", new Variant(this
		//	.getPrinter().getName()));//hangs here
		Dispatch oWorkbooks = oExcel.getProperty("Workbooks").toDispatch();
		Dispatch oWorkbook = Dispatch.call(oWorkbooks, "Open", filePath).toDispatch();
		
		Variant From = Variant.VT_MISSING;
		Variant To = Variant.DEFAULT;
		Variant Copies = new Variant(this.copies);
		Variant Preview = Variant.VT_FALSE;
		Variant ActivePrinter = oExcel.getProperty("ActivePrinter");
		Variant PrintToFile = Variant.VT_TRUE;
		Variant Collate = Variant.VT_TRUE;
		Variant PrToFileName = new Variant(filePath + ".pdf");//testing
		//Variant PrToFileName = Variant.VT_MISSING;
		//System.out.println("Printing");
		//Is it printing to file? yes
		Object[] args = new Object[] {From, To, Copies, Preview, ActivePrinter, PrintToFile, Collate, PrToFileName};
		/*
		Dispatch.callN(oWorkbook, "PrintOut", Variant.VT_MISSING, Variant.DEFAULT, new Variant(this.copies),
				Variant.VT_FALSE, oExcel.getProperty("ActivePrinter"), Variant.VT_TRUE, Variant.VT_TRUE,
				new Variant(filePath + ".pdf"));
		*/
		Dispatch.callN(oWorkbook, "PrintOut", args);
		System.out.println("printed");
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
		System.out.println("Done printing xls "+ filePath);
		logPrint(filePath);
	}
	
	
	void printDocx(String filePath) {
		/*
		 * Uses jacob to open msword, open the text file and print it
		 */
		System.out.println("Printing "+filePath);
		this.oWord = new ActiveXComponent("Word.Application");
        this.oWord.setProperty("Visible", new Variant(false));
        this.oWord.setProperty("ActivePrinter", new Variant(this.getPrinter()
        		.getName()));
        //this.oWord.setProperty("ActivePrinter", new Variant("Microsoft 
        //	Print to PDF"));//for testing
        Dispatch oDocuments = oWord.getProperty("Documents").toDispatch();
        Dispatch oDocument = Dispatch.call(oDocuments, "Open", filePath, new Variant(false), new Variant(true)).toDispatch();
        
        Variant Background= new Variant(false);           
        Variant Append = new Variant(false);                       
        Variant Range = new Variant(0); //> print out all document                       
        Variant OutputFileName = new Variant(filePath+".pdf");// for testing
        //Variant OutputFileName = new Variant("");//switch from above
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
	        this.oWord.safeRelease();
		}
        System.out.println("Done with "+filePath);
        logPrint(filePath);
	}
	
	PrintService getPrinter() {
		return this.printer;
	}

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
	
	
	int getPrintTimer() {
		return this.printTimer;
	}


	void setPrintTimer(int printTimer) {
		this.printTimer = printTimer;
	}
	
	void askNewPath() {
		//Asks the user to set the working directory for this program
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
		/*
		 * Asks for user input to set the interval (minutes) between successive
		 *  file checks - and possibly printing jobs, if new files are found
		 */
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
		//Ask user input for printer selection
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
	
	
	void checkFiles() {
		/*
		 * A method to get all the files in current working directory and
		 * put them in an array.
		 */
		try (Stream<Path> paths = Files.list(this.getPath())) {
			this.files = paths
				.filter(Files::isRegularFile)//not folders or symbolic links
				.toArray(Path[]::new);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//we go and check for files in $path and make an array of them
	
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
			//start a PrintJob() now and come again in x minutes
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
