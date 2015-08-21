package transformj;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import imagescience.image.Aspects;
import imagescience.image.Image;
import imagescience.transform.Scale;
import imagescience.utility.FMath;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

public class TJ_Scale implements PlugIn, KeyListener, WindowListener {
	
	private static String xFactor = "1.0";
	private static String yFactor = "1.0";
	private static String zFactor = "1.0";
	private static int interpolation = 1;
	private static boolean preserve = false;
	
	private TextField xFactorField, yFactorField, zFactorField;
	private TextField xSizeField, ySizeField, zSizeField;
	
	private static Point position = new Point(-1,-1);
	
	private ImagePlus image = null;
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Scale");
		
		TJ.options();
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Scale");
		gd.setInsets(0,0,0);
		gd.addMessage("Scaling factors for input image:");
		gd.addStringField("x-Factor:",xFactor);
		gd.addStringField("y-Factor:",yFactor);
		gd.addStringField("z-Factor:",zFactor);
		gd.setInsets(10,0,5);
		gd.addMessage("Size of output image:");
		gd.setInsets(0,0,5);
		gd.addNumericField("x-Size:",d2i(image.getWidth()*s2d(xFactor)),0,7,"pixels");
		gd.setInsets(0,0,5);
		gd.addNumericField("y-Size:",d2i(image.getHeight()*s2d(yFactor)),0,7,"pixels");
		gd.setInsets(0,0,5);
		gd.addNumericField("z-Size:",d2i(image.getNSlices()*s2d(zFactor)),0,7,"slices");
		gd.setInsets(15,0,5);
		gd.addChoice("Interpolation:",TJ.interpolations,TJ.interpolations[interpolation]);
		gd.setInsets(15,3,0);
		gd.addCheckbox(" Preserve physical image dimensions",preserve);
		
		final Vector factors = gd.getStringFields();
		xFactorField = (TextField)factors.get(0); xFactorField.addKeyListener(this);
		yFactorField = (TextField)factors.get(1); yFactorField.addKeyListener(this);
		zFactorField = (TextField)factors.get(2); zFactorField.addKeyListener(this);
		
		final Vector sizes = gd.getNumericFields();
		xSizeField = (TextField)sizes.get(0); xSizeField.addKeyListener(this);
		ySizeField = (TextField)sizes.get(1); ySizeField.addKeyListener(this);
		zSizeField = (TextField)sizes.get(2); zSizeField.addKeyListener(this);
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		xFactor = gd.getNextString();
		yFactor = gd.getNextString();
		zFactor = gd.getNextString();
		interpolation = gd.getNextChoiceIndex();
		preserve = gd.getNextBoolean();
		
		try {
			final Image input = Image.wrap(image);
			final Scale scaler = new Scale();
			scaler.messenger.log(TJ_Options.log);
			scaler.progressor.display(TJ_Options.progress);
			double xf=1, yf=1, zf=1, tf=1, cf=1;
			try { xf = Double.parseDouble(xFactor); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid x-factor for scaling"); }
			try { yf = Double.parseDouble(yFactor); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid y-factor for scaling"); }
			try { zf = Double.parseDouble(zFactor); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid z-factor for scaling"); }
			int scheme = Scale.NEAREST;
			switch (interpolation) {
				case 0: scheme = Scale.NEAREST; break;
				case 1: scheme = Scale.LINEAR; break;
				case 2: scheme = Scale.CUBIC; break;
				case 3: scheme = Scale.BSPLINE3; break;
				case 4: scheme = Scale.OMOMS3; break;
				case 5: scheme = Scale.BSPLINE5; break;
			}
			final Image output = scaler.run(input,xf,yf,zf,tf,cf,scheme);
			if (preserve) {
				final Aspects a = input.aspects();
				output.aspects(new Aspects(a.x/xf,a.y/yf,a.z/zf,a.t/tf,a.c/cf));
			}
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
	
	private double s2d(final String s) {
		
		try { return Double.parseDouble(s); }
		catch (Exception e) { return 0; }
	}
	
	private int d2i(final double d) {
		
		final int i = FMath.round(d);
		return (i < 1) ? 1 : i;
	}
	
	public void keyPressed(final KeyEvent e) { }
	
	public void keyReleased(final KeyEvent e) {
		
		final Object source = e.getSource();
		
		if (source == xFactorField) {
			xSizeField.setText(String.valueOf(d2i(image.getWidth()*s2d(xFactorField.getText()))));
		} else if (source == yFactorField) {
			ySizeField.setText(String.valueOf(d2i(image.getHeight()*s2d(yFactorField.getText()))));
		} else if (source == zFactorField) {
			zSizeField.setText(String.valueOf(d2i(image.getNSlices()*s2d(zFactorField.getText()))));
		} else if (source == xSizeField) {
			xFactorField.setText(String.valueOf(s2d(xSizeField.getText())/image.getWidth()));
		} else if (source == ySizeField) {
			yFactorField.setText(String.valueOf(s2d(ySizeField.getText())/image.getHeight()));
		} else if (source == zSizeField) {
			zFactorField.setText(String.valueOf(s2d(zSizeField.getText())/image.getNSlices()));
		}
	}
	
	public void keyTyped(final KeyEvent e) { }
	
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
