package com.higedata.fprintr;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
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

public class FilesPrinter implements ActionListener {
	
	JFrame frame;
	JPanel dataPanel;
	JTextField tfPrinter;
	JTextField tfDir;
	JTextField tfTimer;


	public void gui() {
		frame = new JFrame("HigeFilesPrinter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(30,30,500,300);
		BorderLayout bl = new BorderLayout();
		frame.getContentPane().setLayout(bl);
		//super.setLocationRelativeTo(null);
		initMenu();
		dataPanel = new JPanel();
		frame.add(dataPanel);
		
		showData();
		
		frame.setVisible(true);
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
		dataPanel.add(new JLabel("Current printer:"));
		tfPrinter = new JTextField();
		tfPrinter.setEditable(false);
		tfPrinter.setText(this.getPrinter().getName());
		dataPanel.add(tfPrinter);
		//FROM HERE!!
	}//show all the settings and buttons to change them
	
	private void listCurrentPrefs() {
		
	}
	
	private PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
	/**
	 * @return the printer
	 */
	PrintService getPrinter() {
		return printer;
	}
	/**
	 * @param 'printer' - the printer to set
	 */
	void setPrinter(PrintService printer) {
		this.printer = printer;
	}
	
	private String path = System.getProperty("user.dir");
	//private File[] listOfFiles; bad way
	
	String getPath() {
		return path;
	}
	void setPath(String s) {
		this.path = s;
	}//setting the working directory//don't think i need this
	
	private int printTimer = 0;
	
	/**
	 * @return the printTimer
	 */
	public int getPrintTimer() {
		return printTimer;
	}

	/**
	 * @param printTimer the printTimer to set
	 */
	public void setPrintTimer(int printTimer) {
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
					String s = fc.getSelectedFile().getCanonicalPath();
					setPath(s);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}//will be called by a button press or something
	
	void printVar() {
		JOptionPane.showMessageDialog(null, path);
	}
	
	/*
	void askNewPrinter() {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		Object[] options;
		int i = -1;
		for (PrintService p : services) {
			++i;
			options[]
		}
		String s = (String)JOptionPane.showInputDialog(null,
												"Available printers:",
												"Select printer",
												JOptionPane.PLAIN_MESSAGE,
												null,
												options,
												getPrinter().getName());
		
		
	}
	*/
	
	void checkFiles() {
		try (Stream<Path> paths = Files.list(Paths.get(path))) {
		    paths
		        .filter(Files::isRegularFile)//not folders or symbolic links or other stuff
		    	.forEach(System.out::println);//insert operations on files
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//we go and check for files in $path and operate on them

	@Override
	public void actionPerformed(ActionEvent ae) {
		String choise = ae.getActionCommand();
		//to edit this
		if (choise.equals("Change Preferences")) {
			JOptionPane.showMessageDialog(null, "SELECTED");
		}
		if (choise.equals("About")) {
			//addcode
		}
		
		if (choise.equals("Choose Directory")) {
			this.askNewPath();
		}
		if (choise.equals("Set Print Timing")) {
			//this.setTimer();
		}
		if (choise.equals("Choose Printer")) {
			//this.askNewPrinter();
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
