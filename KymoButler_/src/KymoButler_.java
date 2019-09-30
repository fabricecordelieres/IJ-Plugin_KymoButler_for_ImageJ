/**
*
*  KymoButler_.java, 20 juil. 2019
   Fabrice P Cordelieres, fabrice.cordelieres at gmail.com

   Copyright (C) 2019 Fabrice P. Cordelieres

   License:
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import KymoButler.KymoButlerIO;
import KymoButler.KymoButlerResponseParser;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

/**
 * This class is aimed at launching the analysis of kymographs using the KymoButler webservice
 * @author Fabrice P. Cordelieres
 *
 */
public class KymoButler_ implements PlugIn{
	/** KymoButler API URL **/
	String URL=Prefs.get("KymoButler_URL.string", "");
	
	/** The ImagePlus that is present at startup (or null) **/
	ImagePlus ip=null;
	
	/** KymoButlerIO object: will handle all the analysis process **/
	KymoButlerIO kbio=new KymoButlerIO();
	
	/** Preferences: threshold **/
	float p=(float) Prefs.get("KymoButler_p.double", kbio.getThreshold());
	
	/** Preferences: minimumSize **/
	float minimumSize=(float) Prefs.get("KymoButler_minimumSize.double", kbio.getMinimumSize());
	
	/** Preferences: minimumFrames **/
	float minimumFrames=(float) Prefs.get("KymoButler_minimumFrames.double", kbio.getMinimumFrames());
	
	/** Preferences: addToManager **/
	boolean addToManager=Prefs.get("KymoButler_addToManager.boolean", true);
	
	/** Preferences: simplifyRois **/
	boolean simplifyTracks=Prefs.get("KymoButler_simplifyTracks.boolean", true);
	
	/** Preferences: addToManager **/
	boolean clearManager=Prefs.get("KymoButler_clearManager.boolean", true);
	
	/** Preferences: showKymo **/
	boolean showKymo=Prefs.get("KymoButler_showKymo.boolean", true);
	
	/** Preferences: simplifyRois **/
	boolean showOverlay=Prefs.get("KymoButler_showOverlay.boolean", true);
	
	/** Debug tag: true to save JSON in IJ installation folder **/
	boolean debug=Prefs.get("KymoButler_debug.boolean", false);
	
	String helpMsg="<html>Version 1.0.0, 13 aug. 2019<br>"
			+ "This plugin is powered by <a href=\"https://deepmirror.ai/software/kymobutler/\">KymoButler</a><br>"
			+ "a webservice provided by Andrea Dimitracopoulos and Max Jakobs<br>"
			+ "based on their <a href=\"https://www.biorxiv.org/content/10.1101/405183v3\">publication</a> you should cite when using the website/plugin:<br><br>"
			+ "This plugin heavily relies on external libraries that are packed in its jar file:"
			+ "<ul>"
			+ "	<li>commons-io, commons-io, v2.6</li>"
			+ "	<li>org.apache.httpcomponents/httpclient, v4.5.9</li>"
			+ "	<li>org.apache.httpcomponents/httpmime, v4.5.9</li>"
			+ "	<li>org.json/json, v20180813</li>"
			+ "</ul>"
			+ "<br><br>"
			+ "The plugin is brought to you by F.P. Cordelières <a href=\"mailto:fabrice.cordelieres@gmail.com?subject=KymoButler for IJ\">fabrice.cordelieres@gmail.com</a>";
	

	@Override
	public void run(String arg) {
		ip=WindowManager.getCurrentImage();
		if(checkForLibraries()) {
			if(!URL.isEmpty()) {	
				if(ip!=null) {
					showGUI();
				}else {
					IJ.showMessage("Nothing to do, please open an image first");
				}
				}else {
					IJ.showMessage("No URL found for the API: please set one under the KymoButler/Options menu");
				}
		}else {
			IJ.showStatus("Installation of the required libraries needs to be done");	
		}
	}
	
	/**
	 * Checks that the required libraries are installed, and displays an error message if they are not
	 * @return true if all required libraries are installed, false otherwise
	 */
	public boolean checkForLibraries() {
		/*
		HashMap<String, String> classesToFind=new HashMap<String, String>(){
			private static final long serialVersionUID = 1L;

			{
				put("commons-io-2.6", "org.apache.commons.io.FileUtils");
				put("commons-logging-1.2", "org.apache.commons.logging.Log");
				put("commons-codec-1.11", "org.apache.commons.codec.BinaryDecoder");
				put("httpclient-4.5.9", "org.apache.http.client.HttpClient");
				put("httpcore-4.4.11", "org.apache.http.HttpEntity");
				put("httpmime-4.5.9", "org.apache.http.entity.mime.HttpMultipartMode");
				put("json-20180813", "org.json.JSONObject");
			}
		};
		*/
		
		String[] classesToFind=new String[] {"commons-io-2.6.jar", "commons-logging-1.2.jar", "commons-codec-1.11.jar", "httpclient-4.5.9.jar", "httpcore-4.4.11.jar", "httpmime-4.5.9.jar", "json-20180813.jar"};
	
		String msg="";
		
		for(String jar: classesToFind) {
			boolean found=new File(IJ.getDirectory("plugins")+File.separator+"jars"+File.separator+jar).exists();
			if(debug) IJ.log("Check "+jar+": "+(found?"":"not ")+"found");
			
			if(!found) msg=msg+(!msg.isEmpty()?"\n":"")+jar;
		}
		
		if(!msg.isEmpty()) IJ.error("The following libraries are missing:\n"+msg);
		
		return msg.isEmpty();
	}
		
	/**
	 * Displays the GUI, stores the parameters and launches the analysis
	 */
	public void showGUI() {
		GenericDialog gd=new GenericDialog("KymoButler for IJ by fabrice.cordelieres@gmail.com");
		gd.addMessage("<html><b><u>Parameters</u></b</html>");
		gd.addNumericField("Threshold (default: 0.2)", p, 2);
		gd.addNumericField("Minimum_size (default: 3)", minimumSize, 0);
		gd.addNumericField("Minimum_frames (default: 3)", minimumFrames, 0);
		
		gd.addMessage("");
		
		gd.addMessage("<html><b><u>Output</u></b</html>");
		gd.addCheckbox("Add to manager", addToManager);
		gd.addCheckbox("Simplify tracks", simplifyTracks);
		gd.addCheckbox("Clear manager before adding", clearManager);
		gd.addCheckbox("Show_kymograph", showKymo);
		gd.addCheckbox("Show_overlay", showOverlay);
		
		gd.addMessage("<html><p style=\"color:#FF0000\";><b><u>Note</u></b>: By using this plugin, you agree your image<br>"
													  + "will be pushed to the <b>KymoButler</b> server and might<br>"
													  + "be used anonymously for software improvements</p></html>");
		
		gd.addHelp(helpMsg);
		gd.showDialog();
		
		if(gd.wasOKed()) {
			p=(float) gd.getNextNumber();
			minimumSize=(float) gd.getNextNumber();
			minimumFrames=(float) gd.getNextNumber();
			
			addToManager=gd.getNextBoolean();
			simplifyTracks=gd.getNextBoolean();
			clearManager=gd.getNextBoolean();
			showKymo=gd.getNextBoolean();
			showOverlay=gd.getNextBoolean();
			
			storePreferences();
			
			runAnalysis();
		}
	}
	
	/**
	 * Stores preferences, based on the user input
	 */
	public void storePreferences() {
		Prefs.set("KymoButler_p.double", p);
		Prefs.set("KymoButler_minimumSize.double", minimumSize);
		Prefs.set("KymoButler_minimumFrames.double", minimumFrames);
		Prefs.set("KymoButler_addToManager.boolean", addToManager);
		Prefs.set("KymoButler_simplifyTracks.boolean", simplifyTracks);
		Prefs.set("KymoButler_clearManager.boolean", clearManager);
		Prefs.set("KymoButler_showKymo.boolean", showKymo);
		Prefs.set("KymoButler_showOverlay.boolean", showOverlay);
	}
	
	/**
	 * Launches analysis once all parameters have been set, returns all images and ROIs
	 */
	public void runAnalysis() {
		if(showKymo || showOverlay || addToManager) {
			Calibration cal=ip.getCalibration();
			
			kbio.setCurrentImageAsKymograph();
			kbio.setThreshold(p);
			kbio.setMinimumSize(minimumSize);
			kbio.setMinimumFrames(minimumFrames);
			
			String response=kbio.getAnalysisResults();
			
			if(response==null) {
				IJ.showStatus("Process cancelled, either by server or by user");
			}else {
				if(KymoButlerResponseParser.isJSON(response)){
					KymoButlerResponseParser pkr=new KymoButlerResponseParser(response);
					if(addToManager) pkr.pushRoisToRoiManager(simplifyTracks, clearManager);
					if(showKymo) pkr.showKymograph(cal);
					if(showOverlay) pkr.showOverlay(cal);
				}else {
					IJ.showStatus("The response doesn't seem to be properly formatted");
				}
			}
			
			if(debug) kbio.saveAnalysisResults(IJ.getDirectory("imageJ")+(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()))+"_debug_KymoButler.json");			
		}else {
			IJ.showStatus("Nothing to do, please check at least one option");
		}
	}
}
