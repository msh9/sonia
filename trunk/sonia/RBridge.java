package sonia;

import java.util.Date;

import javax.swing.UIManager;

public class RBridge {
	
	public RBridge(){
		
	}

	public SoniaController getSoniaController(String[] args){
//		set the look and feel so it will appear the same on all platforms
		  try {
		        UIManager.setLookAndFeel(
		        		UIManager.getCrossPlatformLookAndFeelClassName());
		    } catch (Exception e) {System.out.println(e); }
		    
	    Date seedDate = new Date();
	    //kludge here 'cause millisecond value of date is too large for int
	    int rngSeed = (int)Math.round((double)seedDate.getTime() - 1050960000000.0);
	    String inFile = "";
	    for(int i=0; i<args.length; i++)
	    {
	      String arg = args[i];
	      //look at the arguments passed on the command line
	      //if there is "seed:9809283434" use it as the random seed
	      if (arg.startsWith("seed:"))
	      {
	        rngSeed = Integer.parseInt(arg.substring(5));
	      }
	      //if there is "file:<filenameand path>"try to load the file
	      if (arg.startsWith("file:"))
	      {
	          inFile = arg.substring(5);
	      }
	    }
	    SoniaController sonia = new SoniaController(rngSeed);
	    //if a file has been passed on the command line, load it
	    if (!inFile.equals(""))
	    {
	        sonia.loadFile(inFile);
	    }
	    return sonia;
	}
}
