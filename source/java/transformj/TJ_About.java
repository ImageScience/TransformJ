package transformj;

import ij.IJ;
import ij.plugin.PlugIn;

import java.util.Calendar;

/** ImageJ plugin for showing basic information about TransformJ. */
public class TJ_About implements PlugIn {
	
	/** Default constructor. */
	public TJ_About() { }
	
	public void run(String arg) {
		
		IJ.showMessage(
			TJ.name()+": About",
			TJ.name()+" is a plugin package for image transformation.\n"+
			"The version number of the present installation is "+TJ.version()+".\n"+
			"Copyright (C) 2001-"+Calendar.getInstance().get(Calendar.YEAR)+" by Erik Meijering.\n"
		);
	}
	
}
