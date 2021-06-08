package com.higedata.fprintr;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	JFrame frame;//mainframe
	private JPanel dataPanel = new JPanel();//where the data is shown and changed
	private JPanel changePanel = new JPanel();//nothing yet
	JTextField tfPrinter;
	JTextField tfDir;
	JTextField tfTimer;
	JButton bSetPrinter;
	JButton bSetDir;
	JButton bSetTimer;
	
	private PrintService printer;	//what printer will be used
	private String path;			//the directory from where the files will be printed
	private int printTimer;			//time (in minutes) between file checks
	
	FilesPrinter() {
		//initVars();
		
		//this is the default ones, in case we have no configuration file
		//will also make and read configuration file ... one day ...
		printer = PrintServiceLookup.lookupDefaultPrintService();
		path = System.getProperty("user.dir");
		printTimer = 15;
	}
	
	public JPanel getDataPanel() {
		return dataPanel;
	}
	
	public JPanel getChangePanel() {
		return changePanel;
	}


	public void gui() {
		frame = new JFrame("HigeFilesPrinter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setLocationRelativeTo(null);
		//BorderLayout bl = new BorderLayout();
		//frame.getContentPane().setLayout(bl);
		initMenu();
		
		showData();
		
		frame.add(getDataPanel());
		
		frame.pack();
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
		tfDir.setText(this.getPath());
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
	}//show all the settings and buttons to change them
	
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
	
	String getPath() {
		return path;
	}
	void setPath(String s) {
		this.path = s;
	}//setting the working directory//don't think i need this
	
	/**
	 * @return the printTimer
	 */
	int getPrintTimer() {
		return printTimer;
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
					String s = fc.getSelectedFile().getCanonicalPath();
					setPath(s);
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
		
		if (choise.equals("Select Directory")) {
			this.askNewPath();
			tfDir.setText(path);
		}
		
		if (choise.equals("Select Printer")) {
			setPrinter(this.askNewPrinter());
			tfPrinter.setText(getPrinter().getName());
		}
		
		if (choise.equals("Set Print Timer")) {
			setPrintTimer(this.askNewTimer());
			tfTimer.setText(String.valueOf(getPrintTimer()) + " Minutes");
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
