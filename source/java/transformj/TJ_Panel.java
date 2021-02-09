package transformj;

import ij.IJ;
import ij.gui.GUI;
import ij.plugin.PlugIn;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/** ImageJ plugin for launching the TransformJ panel. */
public class TJ_Panel implements PlugIn, ActionListener, WindowListener {
	
	private Dialog dialog; Panel panel;
	
	private Button affineButton, cropButton, embedButton;
	private Button matrixButton, mirrorButton, rotateButton;
	private Button scaleButton, translateButton, turnButton;
	private Button aboutButton, optionsButton, helpButton;
	
	private static Point position = new Point(-1,-1);
	
	/** Default constructor. */
	public TJ_Panel() { }
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Panel",true);
		
		final Frame parent = (IJ.getInstance() != null) ? IJ.getInstance() : new Frame();
		dialog = new Dialog(parent,TJ.name(),false);
		dialog.setLayout(new FlowLayout());
		dialog.addWindowListener(this);
		
		panel = new Panel();
		panel.setLayout(new GridLayout(4,3,5,5));
		
		affineButton = addButton("Affine");
		cropButton = addButton("Crop");
		embedButton = addButton("Embed");
		
		matrixButton = addButton("Matrix");
		mirrorButton = addButton("Mirror");
		rotateButton = addButton("Rotate");
		
		scaleButton = addButton("Scale");
		translateButton = addButton("Translate");
		turnButton = addButton("Turn");
		
		aboutButton = addButton("About");
		optionsButton = addButton("Options");
		helpButton = addButton("Help");
		
		dialog.add(panel);
		dialog.pack();
		if (position.x < 0 || position.y < 0) GUI.center(dialog);
		else dialog.setLocation(position);
		dialog.setVisible(true);
	}
	
	private Button addButton(String label) {
		
		final Button b = new Button("   "+label+"   ");
		b.addActionListener(this);
		panel.add(b);
		return b;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		final Object source = e.getSource();
		if (source == affineButton) IJ.doCommand("TransformJ Affine");
		else if (source == cropButton) IJ.doCommand("TransformJ Crop");
		else if (source == embedButton) IJ.doCommand("TransformJ Embed");
		else if (source == matrixButton) (new TJ_Matrix()).run("");
		else if (source == mirrorButton) IJ.doCommand("TransformJ Mirror");
		else if (source == rotateButton) IJ.doCommand("TransformJ Rotate");
		else if (source == scaleButton) IJ.doCommand("TransformJ Scale");
		else if (source == translateButton) IJ.doCommand("TransformJ Translate");
		else if (source == turnButton) IJ.doCommand("TransformJ Turn");
		else if (source == aboutButton) (new TJ_About()).run("");
		else if (source == optionsButton) IJ.doCommand("TransformJ Options");
		else if (source == helpButton) (new TJ_Help()).run("");
	}
	
	public void windowActivated(final WindowEvent e) { }
	
	public void windowClosed(final WindowEvent e) {
		
		position.x = e.getWindow().getX();
		position.y = e.getWindow().getY();
	}
	
	public void windowClosing(final WindowEvent e) {
		
		dialog.setVisible(false);
		dialog.dispose();
	}
	
	public void windowDeactivated(final WindowEvent e) { }
	
	public void windowDeiconified(final WindowEvent e) { }
	
	public void windowIconified(final WindowEvent e) { }
	
	public void windowOpened(final WindowEvent e) { }
	
}
