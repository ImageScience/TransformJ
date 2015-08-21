package transformj;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import imagescience.image.Coordinates;
import imagescience.image.Dimensions;
import imagescience.image.Image;
import imagescience.transform.Embed;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class TJ_Embed implements PlugIn, WindowListener {
	
	private static int xSize = 1024;
	private static int ySize = 1024;
	private static int zSize = 1;
	private static int tSize = 1;
	private static int cSize = 1;
	
	private static int xPosition = 0;
	private static int yPosition = 0;
	private static int zPosition = 1;
	private static int tPosition = 1;
	private static int cPosition = 1;
	
	private static int filling = 0;
	
	private static Point position = new Point(-1,-1);
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		final ImagePlus image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Embed");
		
		TJ.options();
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Embed");
		gd.setInsets(0,0,5);
		gd.addMessage("Size of output image:");
		gd.setInsets(0,0,5);
		gd.addNumericField("x-Size:",xSize,0,6,"pixels");
		gd.setInsets(0,0,5);
		gd.addNumericField("y-Size:",ySize,0,6,"pixels");
		gd.setInsets(0,0,5);
		gd.addNumericField("z-Size:",zSize,0,6,"slices");
		gd.setInsets(0,0,5);
		gd.addNumericField("t-Size:",tSize,0,6,"frames");
		gd.setInsets(0,0,5);
		gd.addNumericField("c-Size:",cSize,0,6,"channels");
		gd.setInsets(10,0,5);
		gd.addMessage("Position of input image:");
		gd.setInsets(0,0,5);
		gd.addNumericField("x-Position:",xPosition,0,6,"pixels");
		gd.setInsets(0,0,5);
		gd.addNumericField("y-Position:",yPosition,0,6,"pixels");
		gd.setInsets(0,0,5);
		gd.addNumericField("z-Position:",zPosition,0,6,"slices");
		gd.setInsets(0,0,5);
		gd.addNumericField("t-Position:",tPosition,0,6,"frames");
		gd.setInsets(0,0,5);
		gd.addNumericField("c-Position:",cPosition,0,6,"channels");
		gd.setInsets(15,0,5);
		gd.addChoice("Background:",TJ.fillings,TJ.fillings[filling]);
		
		if (position.x >= 0 && position.y >= 0) {
			gd.centerDialog(false);
			gd.setLocation(position);
		} else gd.centerDialog(true);
		gd.addWindowListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) return;
		
		xSize = (int)gd.getNextNumber();
		ySize = (int)gd.getNextNumber();
		zSize = (int)gd.getNextNumber();
		tSize = (int)gd.getNextNumber();
		cSize = (int)gd.getNextNumber();
		xPosition = (int)gd.getNextNumber();
		yPosition = (int)gd.getNextNumber();
		zPosition = (int)gd.getNextNumber();
		tPosition = (int)gd.getNextNumber();
		cPosition = (int)gd.getNextNumber();
		filling = gd.getNextChoiceIndex();
		
		try {
			final Image input = Image.wrap(image);
			final Embed embedder = new Embed();
			embedder.messenger.log(TJ_Options.log);
			embedder.progressor.display(TJ_Options.progress);
			int scheme = Embed.ZERO;
			switch (filling) {
				case 0: scheme = Embed.ZERO; break;
				case 1: scheme = Embed.MINIMUM; break;
				case 2: scheme = Embed.MAXIMUM; break;
				case 3: scheme = Embed.REPEAT; break;
				case 4: scheme = Embed.MIRROR; break;
				case 5: scheme = Embed.CLAMP; break;
			}
			if (xSize < 1) throw new IllegalArgumentException("Zero or negative x-size for output image");
			if (ySize < 1) throw new IllegalArgumentException("Zero or negative y-size for output image");
			if (zSize < 1) throw new IllegalArgumentException("Zero or negative z-size for output image");
			if (tSize < 1) throw new IllegalArgumentException("Zero or negative t-size for output image");
			if (cSize < 1) throw new IllegalArgumentException("Zero or negative c-size for output image");
			final Dimensions outSize = new Dimensions(xSize,ySize,zSize,tSize,cSize);
			final Coordinates inPosition = new Coordinates(xPosition,yPosition,zPosition-1,tPosition-1,cPosition-1);
			final Image output = embedder.run(input,outSize,inPosition,scheme);
			TJ.show(output,image,mapChannels(input.dimensions(),outSize,inPosition,filling));
			
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
	
	private int[][] mapChannels(final Dimensions inSize, final Dimensions outSize, final Coordinates inPosition, final int filling) {
		
		final int[][] idx = new int[2][];
		
		switch (filling) {
			case 0: case 1: case 2:
				idx[0] = new int[inSize.c]; idx[1] = new int[inSize.c];
				for (int i=0; i<inSize.c; ++i) { idx[0][i] = i + 1; idx[1][i] = inPosition.c + i + 1; }
				break;
			case 3:
				idx[0] = new int[outSize.c]; idx[1] = new int[outSize.c];
				for (int i=0; i<inSize.c; ++i) idx[0][inPosition.c+i] = i + 1;
				for (int i=0; i<outSize.c; ++i) idx[1][i] = i + 1;
				for (int i=inPosition.c-1, i0=inPosition.c+inSize.c-1; i>=0; --i, --i0) idx[0][i] = idx[0][i0];
				for (int i=inPosition.c+inSize.c, i0=inPosition.c; i<outSize.c; ++i, ++i0) idx[0][i] = idx[0][i0];
				break;
			case 4:
				idx[0] = new int[outSize.c]; idx[1] = new int[outSize.c];
				for (int i=0; i<inSize.c; ++i) idx[0][inPosition.c+i] = i + 1;
				for (int i=0; i<outSize.c; ++i) idx[1][i] = i + 1;
				int ifs = 2; int indimssm1 = inSize.c - 1;
				if (inSize.c == 1) { ++indimssm1; ifs = 1; }
				for (int i=inPosition.c-1; i>=0; --i) {
				final int idiff = i - inPosition.c;
				int i0 = idiff / indimssm1; i0 += i0 % ifs;
				idx[0][i] = idx[0][inPosition.c + Math.abs(idiff - i0*indimssm1)]; }
				for (int i=inPosition.c+inSize.c; i<outSize.c; ++i) {
					final int idiff = i - inPosition.c;
					int i0 = idiff / indimssm1; i0 += i0 % ifs;
					idx[0][i] = idx[0][inPosition.c + Math.abs(idiff - i0*indimssm1)];
				}
				break;
			case 5:
				idx[0] = new int[outSize.c]; idx[1] = new int[outSize.c];
				for (int i=0; i<inSize.c; ++i) idx[0][inPosition.c+i] = i + 1;
				for (int i=0; i<outSize.c; ++i) idx[1][i] = i + 1;
				final int b = idx[0][inPosition.c];
				final int e = idx[0][inPosition.c + inSize.c - 1];
				for (int i=inPosition.c-1; i>=0; --i) idx[0][i] = b;
				for (int i=inPosition.c+inSize.c; i<outSize.c; ++i) idx[0][i] = e;
				break;
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
