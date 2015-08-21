package transformj;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import imagescience.image.Image;
import imagescience.transform.Affine;
import imagescience.transform.Transform;

import java.awt.Button;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

public class TJ_Affine implements PlugIn, ActionListener, WindowListener {
	
	private static String file = "";
	private static int interpolation = 1;
	private static String background = "0.0";
	private static boolean adjust = true;
	private static boolean resample = false;
	private static boolean antialias = false;
	
	private Button browseButton, createButton;
	private TextField fileField;
	
	private static Point position = new Point(-1,-1);
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		final ImagePlus image = TJ.imageplus();
		if (image == null) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Affine");
		
		TJ.options();
		
		GenericDialog gd = new GenericDialog(TJ.name()+": Affine");
		gd.addStringField("Matrix file:",file,30);
		fileField = (TextField)gd.getStringFields().get(0);
		
		final Panel buttons = new Panel();
		GridBagLayout bgbl = new GridBagLayout();
		buttons.setLayout(bgbl);
		browseButton = new Button("    Browse    ");
		browseButton.addActionListener(this);
		createButton = new Button("     Create     ");
		createButton.addActionListener(this);
		GridBagConstraints bgbc = new GridBagConstraints();
		bgbc.anchor = GridBagConstraints.WEST;
		bgbc.insets = new Insets(0,0,0,5);
		bgbl.setConstraints(browseButton,bgbc);
		buttons.add(browseButton);
		bgbc.insets = new Insets(0,0,0,0);
		bgbl.setConstraints(createButton,bgbc);
		buttons.add(createButton);
		gd.addPanel(buttons,GridBagConstraints.WEST,new Insets(0,0,20,0));
		bgbl = (GridBagLayout)gd.getLayout();
		bgbc = bgbl.getConstraints(buttons); bgbc.gridx = 1;
		bgbl.setConstraints(buttons,bgbc);
		
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
		
		file = gd.getNextString();
		interpolation = gd.getNextChoiceIndex();
		background = gd.getNextString();
		adjust = gd.getNextBoolean();
		resample = gd.getNextBoolean();
		antialias = gd.getNextBoolean();
		
		try {
			if (file == null || file.equals(""))
				throw new IllegalArgumentException("Empty matrix file name");
			final TJ_Matrix matrix = new TJ_Matrix();
			matrix.load(file);
			final Transform transform = matrix.get();
			final Image input = Image.wrap(image);
			final Affine affiner = new Affine();
			affiner.messenger.log(TJ_Options.log);
			affiner.progressor.display(TJ_Options.progress);
			double bg; try { bg = Double.parseDouble(background); }
			catch (Exception e) { throw new IllegalArgumentException("Invalid background value"); }
			affiner.background = bg;
			int scheme = Affine.NEAREST;
			switch (interpolation) {
				case 0: scheme = Affine.NEAREST; break;
				case 1: scheme = Affine.LINEAR; break;
				case 2: scheme = Affine.CUBIC; break;
				case 3: scheme = Affine.BSPLINE3; break;
				case 4: scheme = Affine.OMOMS3; break;
				case 5: scheme = Affine.BSPLINE5; break;
			}
			final Image output = affiner.run(input,transform,scheme,adjust,resample,antialias);
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
	
	public void actionPerformed(final ActionEvent e) {
		
		if (e.getSource() == browseButton) {
			final FileDialog fdg = new FileDialog(IJ.getInstance(),TJ.name()+": Load",FileDialog.LOAD);
			fdg.setFile(""); fdg.setVisible(true);
			final String d = fdg.getDirectory();
			final String f = fdg.getFile();
			fdg.dispose();
			if (d != null && f != null) {
				String path = d + f;
				if (File.separator.equals("\\"))
					path = path.replace('\\','/');
				fileField.setText(path);
			}
		} else if (e.getSource() == createButton) {
			final TJ_Matrix matrix = new TJ_Matrix();
			try { matrix.load(fileField.getText()); }
			catch (Throwable x) { }
			matrix.run("");
			final String path = matrix.saved();
			if (path != null) fileField.setText(path);
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
