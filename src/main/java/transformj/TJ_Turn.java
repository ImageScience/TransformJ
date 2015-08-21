package transformj;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import imagescience.image.Image;
import imagescience.transform.Turn;

import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class TJ_Turn implements PlugIn, WindowListener {
	
	private static final String[] angles = {"0","90","180","270"};
	
	private static int zIndex = 0;
	private static int yIndex = 0;
	private static int xIndex = 0;
	
	private static Point position = new Point(-1,-1);
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		final ImagePlus image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Turn");
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Turn");
		gd.setInsets(0,0,0);
		gd.addMessage("Turning angles in degrees:");
		gd.addChoice("z-Angle:",angles,angles[zIndex]);
		gd.addChoice("y-Angle:",angles,angles[yIndex]);
		gd.addChoice("x-Angle:",angles,angles[xIndex]);
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		zIndex = gd.getNextChoiceIndex();
		yIndex = gd.getNextChoiceIndex();
		xIndex = gd.getNextChoiceIndex();
		
		try {
			final Image input = Image.wrap(image);
			final Turn turner = new Turn();
			turner.messenger.log(TJ_Options.log);
			turner.progressor.display(TJ_Options.progress);
			final Image output = turner.run(input,zIndex,yIndex,xIndex);
			TJ.show(output,image);
			
		} catch (OutOfMemoryError e) {
			TJ.error("Not enough memory for this operation");
			
		} catch (UnknownError e) {
			TJ.error("Could not create output image for some reason.\nPossibly there is not enough free memory");
			
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
