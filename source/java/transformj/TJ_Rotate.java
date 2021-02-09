package transformj;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import imagescience.image.Image;
import imagescience.transform.Rotate;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/** ImageJ plugin for image rotation. */
public class TJ_Rotate implements PlugIn, WindowListener {
	
	private static String zAngle = "0.0";
	private static String yAngle = "0.0";
	private static String xAngle = "0.0";
	private static int interpolation = 1;
	private static String background = "0.0";
	private static boolean adjust = true;
	private static boolean resample = false;
	private static boolean antialias = false;
	
	private static Point position = new Point(-1,-1);
	
	/** Default constructor. */
	public TJ_Rotate() { }
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		final ImagePlus image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Rotate",true);
		
		TJ.options();
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Rotate",IJ.getInstance());
		gd.setInsets(0,0,0);
		gd.addMessage("Rotation angles in degrees:");
		gd.addStringField("z-Angle:",zAngle);
		gd.addStringField("y-Angle:",yAngle);
		gd.addStringField("x-Angle:",xAngle);
		gd.setInsets(15,0,5);
		gd.addChoice("Interpolation:",TJ.interpolations,TJ.interpolations[interpolation]);
		gd.addStringField("Background:",background);
		gd.addCheckbox(" Adjust bounds to fit result",adjust);
		gd.addCheckbox(" Resample isotropically",resample);
		gd.addCheckbox(" Anti-alias borders",antialias);
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		zAngle = gd.getNextString();
		yAngle = gd.getNextString();
		xAngle = gd.getNextString();
		interpolation = gd.getNextChoiceIndex();
		background = gd.getNextString();
		adjust = gd.getNextBoolean();
		resample = gd.getNextBoolean();
		antialias = gd.getNextBoolean();
		
		try {
			final Image input = Image.wrap(image);
			final Rotate rotator = new Rotate();
			rotator.messenger.log(TJ_Options.log);
			rotator.progressor.display(TJ_Options.progress);
			double za, ya, xa, bg;
			try { za = Double.parseDouble(zAngle); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid z-angle for rotation"); }
			try { ya = Double.parseDouble(yAngle); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid y-angle for rotation"); }
			try { xa = Double.parseDouble(xAngle); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid x-angle for rotation"); }
			try { bg = Double.parseDouble(background); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid background value"); }
			rotator.background = bg;
			int scheme = Rotate.NEAREST;
			switch (interpolation) {
				case 0: scheme = Rotate.NEAREST; break;
				case 1: scheme = Rotate.LINEAR; break;
				case 2: scheme = Rotate.CUBIC; break;
				case 3: scheme = Rotate.BSPLINE3; break;
				case 4: scheme = Rotate.OMOMS3; break;
				case 5: scheme = Rotate.BSPLINE5; break;
			}
			final Image output = rotator.run(input,za,ya,xa,scheme,adjust,resample,antialias);
			TJ.show(output,image);
			
		} catch (OutOfMemoryError e) {
			TJ.error("Not enough memory for this operation");
			
		} catch (UnknownError e) {
			TJ.error("Could not create output image for some reason.\nPossibly there is not enough free memory");
			
		} catch (IllegalArgumentException e) {
			TJ.error(e.getMessage());
			
		} catch (IllegalStateException e) {
			TJ.error(e.getMessage());
			
		} catch (Throwable e) {
			TJ.error("An unidentified error occurred while running the plugin");
			
		}
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
