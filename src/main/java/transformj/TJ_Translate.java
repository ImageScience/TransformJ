package transformj;

import ij.ImagePlus;
import ij.gui.GenericDialog;
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
	
	private static String xShift = "0.0";
	private static String yShift = "0.0";
	private static String zShift = "0.0";
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
		gd.addMessage("Translation in voxels:");
		gd.addStringField("x-Translation:",xShift);
		gd.addStringField("y-Translation:",yShift);
		gd.addStringField("z-Translation:",zShift);
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
		
		xShift = gd.getNextString();
		yShift = gd.getNextString();
		zShift = gd.getNextString();
		interpolation = gd.getNextChoiceIndex();
		background = gd.getNextString();
		
		(new TJTranslate()).run(image,xShift,yShift,zShift,interpolation,background);
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

class TJTranslate {
	
	void run(
		final ImagePlus image,
		final String xShift,
		final String yShift,
		final String zShift,
		final int interpolation,
		final String background
	) {
		
		try {
			final Image input = Image.wrap(image);
			final Translate translator = new Translate();
			translator.messenger.log(TJ_Options.log);
			translator.messenger.status(TJ_Options.progress);
			translator.progressor.display(TJ_Options.progress);
			double xs, ys, zs, bg;
			try { xs = Double.parseDouble(xShift); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid x-translation value"); }
			try { ys = Double.parseDouble(yShift); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid y-translation value"); }
			try { zs = Double.parseDouble(zShift); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid z-translation value"); }
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
			final Image output = translator.run(input,xs,ys,zs,scheme);
			TJ.show(output,image);
			
		} catch (OutOfMemoryError e) {
			TJ.error("Not enough memory for this operation");
			
		} catch (UnknownError e) {
			TJ.error("Could not create output image for some reason.\nPossibly there is not enough free memory");
			
		} catch (IllegalArgumentException e) {
			TJ.error(e.getMessage());
			
		} catch (Throwable e) {
			TJ.error("An unidentified error occurred while running the plugin");
			
		}
	}
	
}
