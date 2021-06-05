package com.higedata.fprintr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FilesPrinter {
	
	private String path = System.getProperty("user.dir");
	//private File[] listOfFiles; bad way
	
	void setPath(String s) {
		this.path = s;
	}//setting the working directory//don't think i need this
	
	void askNewPath() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String s = fc.getSelectedFile().getCanonicalPath();
				setPath(s);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}//will be called by a button press or something
	
	void printVar() {
		JOptionPane.showMessageDialog(null, path);
	}
	
	
	
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FilesPrinter fp = new FilesPrinter();
		fp.askNewPath();
		fp.printVar();
		fp.checkFiles();
		//checking how it works
	}

}
