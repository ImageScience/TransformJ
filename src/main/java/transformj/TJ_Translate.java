package transformj;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

import imagescience.image.Image;
import imagescience.transform.Translate;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class TJ_Translate implements PlugIn, WindowListener {
	
	private static String xDistance = "0.0";
	private static String yDistance = "0.0";
	private static String zDistance = "0.0";
	private static boolean voxels = false;
	private static int interpolation = 1;
	private static String background = "0.0";
	
	private static Point position = new Point(-1,-1);
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		final ImagePlus image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Translate");
		
		TJ.options();
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Translate");
		gd.setInsets(0,0,0);
		gd.addMessage("Translation distances:");
		gd.addStringField("x-Distance:",xDistance);
		gd.addStringField("y-Distance:",yDistance);
		gd.addStringField("z-Distance:",zDistance);
		gd.setInsets(0,10,0);
		gd.addCheckbox(" Voxel units for distances",voxels);
		gd.setInsets(15,0,5);
		gd.addChoice("Interpolation:",TJ.interpolations,TJ.interpolations[interpolation]);
		gd.addStringField("Background:",background);
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		xDistance = gd.getNextString();
		yDistance = gd.getNextString();
		zDistance = gd.getNextString();
		voxels = gd.getNextBoolean();
		interpolation = gd.getNextChoiceIndex();
		background = gd.getNextString();
		
		try {
			final Image input = Image.wrap(image);
			final Translate translator = new Translate();
			final Calibration calibration = image.getCalibration();
			translator.messenger.log(TJ_Options.log);
			translator.progressor.display(TJ_Options.progress);
			double dx, dy, dz, bg;
			try { dx = (voxels?calibration.pixelWidth:1)*Double.parseDouble(xDistance); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid x-distance value"); }
			try { dy = (voxels?calibration.pixelHeight:1)*Double.parseDouble(yDistance); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid y-distance value"); }
			try { dz = (voxels?calibration.pixelDepth:1)*Double.parseDouble(zDistance); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid z-distance value"); }
			try { bg = Double.parseDouble(background); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid background value"); }
			translator.background = bg;
			int scheme = Translate.NEAREST;
			switch (interpolation) {
				case 0: scheme = Translate.NEAREST; break;
				case 1: scheme = Translate.LINEAR; break;
				case 2: scheme = Translate.CUBIC; break;
				case 3: scheme = Translate.BSPLINE3; break;
				case 4: scheme = Translate.OMOMS3; break;
				case 5: scheme = Translate.BSPLINE5; break;
			}
			final Image output = translator.run(input,dx,dy,dz,scheme);
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
