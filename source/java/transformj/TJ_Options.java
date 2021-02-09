package transformj;

import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/** ImageJ plugin for setting the TransformJ options. */
public class TJ_Options implements PlugIn, WindowListener {
	
	static boolean adopt = Prefs.get("tj.adopt",true);
	static boolean close = Prefs.get("tj.close",false);
	static boolean save = Prefs.get("tj.save",false);
	static boolean progress = Prefs.get("tj.progress",true);
	static boolean log = Prefs.get("tj.log",false);
	
	private static Point position = new Point(-1,-1);
	
	/** Default constructor. */
	public TJ_Options() { }
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Options",true);
		
		final String space = "     ";
		GenericDialog gd = new GenericDialog(TJ.name()+": Options",IJ.getInstance());
		gd.addCheckbox(" Adopt brightness and contrast from input images"+space,adopt);
		gd.addCheckbox(" Close input images after transforming"+space,close);
		gd.addCheckbox(" Save result images before closing"+space,save);
		gd.addCheckbox(" Progress indication"+space,progress);
		gd.addCheckbox(" Log messaging"+space,log);
		gd.addPanel(new Panel(),GridBagConstraints.EAST,new Insets(0,0,0,0));
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		adopt = gd.getNextBoolean();
		close = gd.getNextBoolean();
		save = gd.getNextBoolean();
		progress = gd.getNextBoolean();
		log = gd.getNextBoolean();
		
		Prefs.set("tj.adopt",adopt);
		Prefs.set("tj.close",close);
		Prefs.set("tj.save",save);
		Prefs.set("tj.progress",progress);
		Prefs.set("tj.log",log);
		
		if (adopt) TJ.log("Adopting brightness and contrast from input images");
		else TJ.log("Setting brightness and contrast based on full output range");
		
		if (close) TJ.log("Closing input images after transforming");
		else TJ.log("Keeping input images after transforming");
		
		if (save) TJ.log("Asking to save result images before closing");
		else TJ.log("Closing result images without asking to save");
		
		if (progress) TJ.log("Enabling progress indication");
		else TJ.log("Disabling progress indication");
		
		if (log) TJ.log("Enabling log messaging");
		else TJ.log("Disabling log messaging");
	}
	
	public void windowActivated(final WindowEvent e) { }
	
	public void windowClosed(final WindowEvent e) {
		
		position.x = e.getWindow().getX();
		position.y = e.getWindow().getY();
	}
	
	public void windowClosing(final WindowEvent e) { }
	
	public void windowDeactivated(final WindowEvent e) { }
	
	public void windowDeiconified(final WindowEvent e) { }
	
	public void windowIconified(final WindowEvent e) { }
	
	public void windowOpened(final WindowEvent e) { }
	
}
