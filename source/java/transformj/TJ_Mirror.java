package transformj;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import imagescience.image.Axes;
import imagescience.image.Image;
import imagescience.transform.Mirror;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/** ImageJ plugin for image mirroring. */
public class TJ_Mirror implements PlugIn, WindowListener {
	
	private static boolean x = false;
	private static boolean y = false;
	private static boolean z = false;
	private static boolean t = false;
	private static boolean c = false;
	
	private static Point position = new Point(-1,-1);
	
	/** Default constructor. */
	public TJ_Mirror() { }
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		final ImagePlus image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Mirror",true);
		
		boolean xDo = true; if (image.getWidth() == 1) xDo = false;
		boolean yDo = true; if (image.getHeight() == 1) yDo = false;
		boolean zDo = true; if (image.getNSlices() == 1) zDo = false;
		boolean tDo = true; if (image.getNFrames() == 1) tDo = false;
		boolean cDo = true; if (image.getNChannels() == 1) cDo = false;
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Mirror",IJ.getInstance());
		if (xDo) gd.addCheckbox(" x-Mirror",x);
		if (yDo) gd.addCheckbox(" y-Mirror",y);
		if (zDo) gd.addCheckbox(" z-Mirror",z);
		if (tDo) gd.addCheckbox(" t-Mirror",t);
		if (cDo) gd.addCheckbox(" c-Mirror",c);
		gd.addPanel(new Panel(),GridBagConstraints.EAST,new Insets(0,0,0,0));
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		x = xDo ? gd.getNextBoolean() : false;
		y = yDo ? gd.getNextBoolean() : false;
		z = zDo ? gd.getNextBoolean() : false;
		t = tDo ? gd.getNextBoolean() : false;
		c = cDo ? gd.getNextBoolean() : false;
		
		try {
			final Image input = Image.wrap(image);
			final Image output = input.duplicate();
			final Mirror mirror = new Mirror();
			mirror.messenger.log(TJ_Options.log);
			mirror.progressor.display(TJ_Options.progress);
			mirror.run(output,new Axes(x,y,z,t,c));
			TJ.show(output,image,mapChannels(c,image.getNChannels()));
			
		} catch (OutOfMemoryError e) {
			TJ.error("Not enough memory for this operation");
			
		} catch (Throwable e) {
			TJ.error("An unidentified error occurred while running the plugin");
			
		}
	}
	
	private int[][] mapChannels(final boolean c, final int nc) {
		
		final int[][] idx = new int[2][nc];
		if (c) for (int i=0; i<nc; ++i) {
			idx[0][i] = i + 1;
			idx[1][i] = nc - i;
		} else for (int i=0; i<nc; ++i) {
			idx[0][i] = idx[1][i] = i + 1;
		}
		return idx;
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
