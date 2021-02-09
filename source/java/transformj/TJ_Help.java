package transformj;

import ij.plugin.BrowserLauncher;
import ij.plugin.PlugIn;

/** ImageJ plugin for showing the <a href="https://imagescience.org/meijering/software/transformj/" target="_blank">TransformJ website</a> in the default internet browser. */
public class TJ_Help implements PlugIn {
	
	/** Default constructor. */
	public TJ_Help() { }
	
	public void run(String arg) {
		
		try { BrowserLauncher.openURL("https://imagescience.org/meijering/software/transformj/"); }
		catch (Throwable e) { TJ.error("Could not open default internet browser"); }
	}
	
}
