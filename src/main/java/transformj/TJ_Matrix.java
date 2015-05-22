package transformj;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.GUI;
import ij.plugin.PlugIn;

import imagescience.image.Axes;
import imagescience.transform.Transform;
import imagescience.utility.Formatter;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;
import java.util.Vector;

public class TJ_Matrix implements PlugIn, ActionListener, ClipboardOwner, FocusListener, KeyListener, WindowListener {
	
	private Dialog dialog; Panel panel;
	private TextField[][] textFields;
	
	private Button rotateButton, scaleButton, shearButton, translateButton;
	private Button invertButton, resetButton, copyButton, printButton;
	private Button undoButton, loadButton, saveButton, closeButton;
	
	private Transform previousTransform = null;
	private static final Transform transform = new Transform();
	private final Formatter formatter = new Formatter();
	private String saved = null;
	
	private static final Point position = new Point(-1,-1);
	private static final Point rotatePosition = new Point(-1,-1);
	private static final Point scalePosition = new Point(-1,-1);
	private static final Point translatePosition = new Point(-1,-1);
	private static final Point shearPosition = new Point(-1,-1);
	
	private static String rotationAngle = "0.0";
	private static String scalingFactor = "1.0";
	private static String translationDistance = "0.0";
	private static String shearingFactor = "1.0";
	
	private static final String[] axes = { "x", "y", "z" };
	
	private static int rotationAxis = 0;
	private static int scalingAxis = 0;
	private static int translationAxis = 0;
	private static int shearingAxis = 0;
	private static int drivingAxis = 0;
	
	public TJ_Matrix() {
		formatter.decs(10);
		formatter.chop(1E-10);
	}
	
	public void run(String arg) {
		
		if (!TJ.check()) return;
		
		TJ.log(TJ.name()+" "+TJ.version()+": Matrix");
		
		final Frame parent = (IJ.getInstance() != null) ? IJ.getInstance() : new Frame();
		dialog = new Dialog(parent,TJ.name()+": Matrix",true);
		dialog.setLayout(new FlowLayout());
		dialog.addWindowListener(this);
		
		panel = new Panel();
		panel.setLayout(new GridLayout(7,4,5,5));
		
		textFields = new TextField[4][4];
		for (int r=0; r<4; ++r) {
			for (int c=0; c<4; ++c) {
				textFields[r][c] = addField();
				if (r == 3) textFields[r][c].setEditable(false);
			}
		}
		
		rotateButton = addButton("Rotate");
		scaleButton = addButton("Scale");
		shearButton = addButton("Shear");
		translateButton = addButton("Translate");
		
		invertButton = addButton("Invert");
		resetButton = addButton("Reset");
		copyButton = addButton("Copy");
		printButton = addButton("Print");
		
		undoButton = addButton("Undo");
		loadButton = addButton("Load");
		saveButton = addButton("Save");
		closeButton = addButton("Close");
		
		formatter.decs(10);
		formatter.chop(1E-10);
		refresh();
		dialog.add(panel);
		dialog.pack();
		if (position.x < 0 || position.y < 0) GUI.center(dialog);
		else dialog.setLocation(position);
		dialog.setVisible(true);
	}
	
	private Button addButton(String label) {
		
		final Button b = new Button("   "+label+"   ");
		b.addActionListener(this);
		b.addKeyListener(this);
		panel.add(b);
		return b;
	}
	
	private TextField addField() {
		
		final TextField tf = new TextField(10);
		tf.addFocusListener(this);
		tf.addKeyListener(this);
		tf.setEditable(true);
		panel.add(tf);
		return tf;
	}
	
	private void refresh() {
		
		for (int r=0; r<4; ++r) {
			for (int c=0; c<4; ++c) {
				textFields[r][c].setText(formatter.d2s(transform.get(r,c)));
			}
		}
	}
	
	private String string(final String prefix, final String delim, final String postfix) {
		
		final StringBuffer sb = new StringBuffer();
		
		for (int r=0; r<4; ++r) {
			sb.append(prefix);
			for (int c=0; c<4; ++c) {
				sb.append(formatter.d2s(transform.get(r,c)));
				if (c < 3) sb.append(delim);
			}
			sb.append(postfix);
		}
		
		return sb.toString();
	}
	
	private String file(final int mode) {
		
		String file = null;
		final String m = (mode == FileDialog.LOAD) ? "Load" : "Save";
		final FileDialog fdg = new FileDialog(IJ.getInstance(),TJ.name()+": "+m,mode);
		fdg.setFile(""); fdg.setVisible(true);
		final String d = fdg.getDirectory();
		final String f = fdg.getFile();
		fdg.dispose();
		if (d != null && f != null) {
			file = d + f;
			if (File.separator.equals("\\"))
				file = file.replace('\\','/');
		}
		return file;
	}
	
	public Transform get() {
		
		return transform.duplicate();
	}
	
	public void set(final Transform a) {
		
		transform.set(a);
	}
	
	public void load(final String file) {
		
		// Read lines:
		final Vector<String> lines = new Vector<String>();
		String line = null;
		try {
			final BufferedReader br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			while (line != null) {
				line = line.trim();
				if (!line.equals(""))
					lines.add(line);
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Unable to find "+file);
		} catch (Throwable e) {
			throw new IllegalArgumentException("Error reading from "+file);
		}
		
		// Convert lines:
		if (lines.size() != 4)
			throw new IllegalArgumentException("File "+file+" does not contain a 4 x 4 matrix");
		String delim = "\t";
		line = lines.get(0);
		if (line.indexOf(",") >= 0) delim = ",";
		else if (line.indexOf(" ") >= 0) delim = " ";
		final double[][] matrix = new double[4][4];
		for (int r=0; r<4; ++r) {
			line = lines.get(r);
			final StringTokenizer st = new StringTokenizer(line,delim);
			if (st.countTokens() != 4)
				throw new IllegalArgumentException("File "+file+" does not contain a 4 x 4 matrix");
			for (int c=0; c<4; ++c) {
				try {
					matrix[r][c] = Double.parseDouble(st.nextToken());
				} catch (Throwable e) {
					throw new IllegalArgumentException("Error reading element ("+r+","+c+") in "+file);
				}
			}
		}
		
		// Store matrix:
		transform.set(matrix);
	}
	
	public void save(final String file) {
		
		try {
			final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(string("","\t","\n"));
			bw.close();
			saved = file;
		} catch (Throwable e) {
			throw new IllegalArgumentException("Error writing to "+file);
		}
	}
	
	String saved() {
		
		return saved;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		doButton(e.getSource());
	}
	
	private void doButton(final Object source) {
		
		if (source == rotateButton) {
			final GenericDialog gd = new GenericDialog(TJ.name()+": Rotate");
			gd.addStringField("Rotation angle:",rotationAngle);
			gd.addChoice("Rotation axis:",axes,axes[rotationAxis]);
			if (rotatePosition.x >= 0 && rotatePosition.y >= 0) {
				gd.centerDialog(false);
				gd.setLocation(rotatePosition);
			} else gd.centerDialog(true);
			gd.showDialog();
			gd.getLocation(rotatePosition);
			if (!gd.wasCanceled()) {
				rotationAngle = gd.getNextString();
				rotationAxis = gd.getNextChoiceIndex();
				try {
					int axis = Axes.X;
					if (rotationAxis == 1) axis = Axes.Y;
					else if (rotationAxis == 2) axis = Axes.Z;
					double angle = Double.parseDouble(rotationAngle);
					previousTransform = transform.duplicate();
					transform.rotate(angle,axis);
					refresh();
					TJ.log("Rotated the matrix "+rotationAngle+" degrees around "+axes[rotationAxis]);
				} catch (Exception x) {
					TJ.error("Invalid rotation angle");
				}
			}
			
		} else if (source == scaleButton) {
			final GenericDialog gd = new GenericDialog(TJ.name()+": Scale");
			gd.addStringField("Scaling factor:",scalingFactor);
			gd.addChoice("Scaling axis:",axes,axes[scalingAxis]);
			if (scalePosition.x >= 0 && scalePosition.y >= 0) {
				gd.centerDialog(false);
				gd.setLocation(scalePosition);
			} else gd.centerDialog(true);
			gd.showDialog();
			gd.getLocation(scalePosition);
			if (!gd.wasCanceled()) {
				scalingFactor = gd.getNextString();
				scalingAxis = gd.getNextChoiceIndex();
				try {
					int axis = Axes.X;
					if (scalingAxis == 1) axis = Axes.Y;
					else if (scalingAxis == 2) axis = Axes.Z;
					double factor = Double.parseDouble(scalingFactor);
					previousTransform = transform.duplicate();
					transform.scale(factor,axis);
					refresh();
					TJ.log("Scaled the matrix by a factor of "+scalingFactor+" in "+axes[scalingAxis]);
				} catch (Exception x) {
					TJ.error("Invalid scaling factor");
				}
			}
			
		} else if (source == shearButton) {
			final GenericDialog gd = new GenericDialog(TJ.name()+": Shearing");
			gd.addStringField("Shearing factor:",shearingFactor);
			gd.addChoice("Shearing axis:",axes,axes[shearingAxis]);
			gd.addChoice("Driving axis:",axes,axes[drivingAxis]);
			if (shearPosition.x >= 0 && shearPosition.y >= 0) {
				gd.centerDialog(false);
				gd.setLocation(shearPosition);
			} else gd.centerDialog(true);
			gd.showDialog();
			gd.getLocation(shearPosition);
			if (!gd.wasCanceled()) {
				shearingFactor = gd.getNextString();
				shearingAxis = gd.getNextChoiceIndex();
				drivingAxis = gd.getNextChoiceIndex();
				try {
					int axis = Axes.X;
					if (shearingAxis == 1) axis = Axes.Y;
					else if (shearingAxis == 2) axis = Axes.Z;
					int drive = Axes.X;
					if (drivingAxis == 1) drive = Axes.Y;
					else if (drivingAxis == 2) drive = Axes.Z;
					double factor = Double.parseDouble(shearingFactor);
					previousTransform = transform.duplicate();
					transform.shear(factor,axis,drive);
					refresh();
					TJ.log("Sheared the matrix by a factor of "+shearingFactor+" in "+axes[shearingAxis]+" by "+axes[drivingAxis]);
				} catch (Exception x) {
					TJ.error("Invalid shearing factor");
				}
			}
			
		} else if (source == translateButton) {
			final GenericDialog gd = new GenericDialog(TJ.name()+": Translate");
			gd.addStringField("Translation distance:",translationDistance);
			gd.addChoice("Translation axis:",axes,axes[translationAxis]);
			if (translatePosition.x >= 0 && translatePosition.y >= 0) {
				gd.centerDialog(false);
				gd.setLocation(translatePosition);
			} else gd.centerDialog(true);
			gd.showDialog();
			gd.getLocation(translatePosition);
			if (!gd.wasCanceled()) {
				translationDistance = gd.getNextString();
				translationAxis = gd.getNextChoiceIndex();
				try {
					int axis = Axes.X;
					if (translationAxis == 1) axis = Axes.Y;
					else if (translationAxis == 2) axis = Axes.Z;
					double distance = Double.parseDouble(translationDistance);
					previousTransform = transform.duplicate();
					transform.translate(distance,axis);
					refresh();
					TJ.log("Translated the matrix by "+translationDistance+" in "+axes[translationAxis]);
				} catch (Exception x) {
					TJ.error("Invalid translation distance");
				}
			}
			
		} else if (source == invertButton) {
			try {
				final Transform tmp = transform.duplicate();
				transform.invert();
				refresh();
				previousTransform = tmp;
				TJ.log("Inverted the matrix");
			} catch (Throwable x) {
				TJ.error(x.getMessage());
			}
			
		} else if (source == resetButton) {
			previousTransform = transform.duplicate();
			transform.reset();
			refresh();
			TJ.log("Reset the matrix to the identity matrix");
			
		} else if (source == copyButton) {
			try {
				final StringSelection ss = new StringSelection(string("","\t","\n"));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss,this);
				TJ.log("Copied the matrix to the clipboard");
				TJ.status("Copied matrix to clipboard");
			} catch (Throwable x) {
				TJ.error("Failed to copy the matrix to the clipboard");
			}
			
		} else if (source == printButton) {
			IJ.log(string("[  ","   ","  ]\n"));
			
		} else if (source == undoButton) {
			if (previousTransform != null) {
				final Transform tmp = transform.duplicate();
				transform.set(previousTransform);
				refresh();
				previousTransform = tmp;
				TJ.log("Undone last change");
			}
			
		} else if (source == loadButton) {
			final String file = file(FileDialog.LOAD);
			if (file != null) try {
				final Transform tmp = transform.duplicate();
				load(file);
				refresh();
				previousTransform = tmp;
				TJ.log("Loaded matrix from "+file);
				TJ.status("Loaded matrix from "+file);
			} catch (Throwable x) {
				TJ.error(x.getMessage());
			}
			
		} else if (source == saveButton) {
			final String file = file(FileDialog.SAVE);
			if (file != null) try {
				save(file);
				TJ.log("Saved matrix to "+file);
				TJ.status("Saved matrix to "+file);
			} catch (Throwable x) {
				TJ.error(x.getMessage());
			}
			
		} else if (source == closeButton) {
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
	
 	public void lostOwnership(final Clipboard clip, final Transferable contents) { }
	
	public void focusGained(final FocusEvent e) {
		
		final Object source = e.getComponent();
		if (source instanceof TextField) {
			((TextField)source).selectAll();
		}
	}
	
	public void focusLost(final FocusEvent e) {
		
		final Object source = e.getSource();
		if (source instanceof TextField) {
			final TextField tf = (TextField)source;
			tf.select(0,0);
			for (int r=0; r<4; ++r) {
				for (int c=0; c<4; ++c) {
					if (textFields[r][c] == tf) {
						try {
							final double d = Double.parseDouble(tf.getText());
							if (d != transform.get(r,c)) {
								previousTransform = transform.duplicate();
								transform.set(r,c,d);
								refresh();
								TJ.log("Updated matrix");
							}
						} catch (Throwable x) {
							TJ.error("Invalid input value (will be reverted)");
							refresh();
						}
						return;
					}
				}
			}
		}
	}
	
	public void keyPressed(final KeyEvent e) { 
		
		final int keyCode = e.getKeyCode();
		
		if (keyCode == KeyEvent.VK_ESCAPE) {
			dialog.setVisible(false);
			dialog.dispose();
			
		} else if (keyCode == KeyEvent.VK_ENTER) {
			doButton(e.getSource());
		}
	}
	
	public void keyReleased(final KeyEvent e) { }
	
	public void keyTyped(final KeyEvent e) { }
	
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
