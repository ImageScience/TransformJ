package transformj;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;

import imagescience.image.Coordinates;
import imagescience.image.Image;
import imagescience.transform.Crop;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/** ImageJ plugin for image cropping. */
public class TJ_Crop implements PlugIn, WindowListener {
	
	private static String xRange = "0,0";
	private static String yRange = "0,0";
	private static String zRange = "1,1";
	private static String tRange = "1,1";
	private static String cRange = "1,1";
	
	private static Point position = new Point(-1,-1);
	
	/** Default constructor. */
	public TJ_Crop() { }
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		final ImagePlus image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Crop",true);
		
		final Roi roi = image.getRoi();
		if (roi != null) {
			final Rectangle rect = roi.getBounds();
			xRange = rect.x + "," + (rect.x + rect.width - 1);
			yRange = rect.y + "," + (rect.y + rect.height - 1);
			zRange = "1," + image.getNSlices();
			tRange = "1," + image.getNFrames();
			cRange = "1," + image.getNChannels();
		}
		
		boolean xDo = true; if (image.getWidth() == 1) xDo = false;
		boolean yDo = true; if (image.getHeight() == 1) yDo = false;
		boolean zDo = true; if (image.getNSlices() == 1) zDo = false;
		boolean tDo = true; if (image.getNFrames() == 1) tDo = false;
		boolean cDo = true; if (image.getNChannels() == 1) cDo = false;
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Crop",IJ.getInstance());
		if (xDo) gd.addStringField("x-Range:",xRange,10);
		if (yDo) gd.addStringField("y-Range:",yRange,10);
		if (zDo) gd.addStringField("z-Range:",zRange,10);
		if (tDo) gd.addStringField("t-Range:",tRange,10);
		if (cDo) gd.addStringField("c-Range:",cRange,10);
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		xRange = xDo ? gd.getNextString() : "0,0";
		yRange = yDo ? gd.getNextString() : "0,0";
		zRange = zDo ? gd.getNextString() : "1,1";
		tRange = tDo ? gd.getNextString() : "1,1";
		cRange = cDo ? gd.getNextString() : "1,1";
		
		try {
			int xStart, xStop; try {
				xStart = Integer.parseInt(xRange.substring(0,xRange.indexOf(',')));
				xStop = Integer.parseInt(xRange.substring(xRange.indexOf(',')+1));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid x-range for cropping");
			}
			int yStart, yStop; try {
				yStart = Integer.parseInt(yRange.substring(0,yRange.indexOf(',')));
				yStop = Integer.parseInt(yRange.substring(yRange.indexOf(',')+1));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid y-range for cropping");
			}
			int zStart, zStop; try {
				zStart = Integer.parseInt(zRange.substring(0,zRange.indexOf(','))) - 1;
				zStop = Integer.parseInt(zRange.substring(zRange.indexOf(',')+1)) - 1;
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid z-range for cropping");
			}
			int tStart, tStop; try {
				tStart = Integer.parseInt(tRange.substring(0,tRange.indexOf(','))) - 1;
				tStop = Integer.parseInt(tRange.substring(tRange.indexOf(',')+1)) - 1;
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid t-range for cropping");
			}
			int cStart, cStop; try {
				cStart = Integer.parseInt(cRange.substring(0,cRange.indexOf(','))) - 1;
				cStop = Integer.parseInt(cRange.substring(cRange.indexOf(',')+1)) - 1;
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid c-range for cropping");
			}
			final Image input = Image.wrap(image);
			final Crop cropper = new Crop();
			cropper.messenger.log(TJ_Options.log);
			cropper.progressor.display(TJ_Options.progress);
			final Coordinates startpos = new Coordinates(xStart,yStart,zStart,tStart,cStart);
			final Coordinates stoppos = new Coordinates(xStop,yStop,zStop,tStop,cStop);
			final Image output = cropper.run(input,startpos,stoppos);
			TJ.show(output,image,mapChannels(cStart,cStop));
			
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
	
	private int[][] mapChannels(final int start, final int stop) {
		
		final int len = stop - start + 1;
		final int[][] idx = new int[2][len];
		for (int i=0; i<len; ++i) {
			idx[0][i] = start + i + 1;
			idx[1][i] = i + 1;
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
