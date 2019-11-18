/**
*
*  KymoButler_Analyze.java, 20 juil. 2019
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

import java.text.SimpleDateFormat;
import java.util.Date;

import KymoButler.KymoButlerIO;
import KymoButler.KymoButlerResponseParser;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

/**
 * This class is aimed at launching the analysis of kymographs using the KymoButler webservice
 * @author Fabrice P. Cordelieres
 *
 */
public class KymoButler_Analyze implements PlugIn{
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
	
	/** Preferences: showOverlay **/
	boolean showOverlay=Prefs.get("KymoButler_showOverlay.boolean", true);
	
	/** Preferences: allowCorrections **/
	boolean allowCorrections=Prefs.get("KymoButler_allowCorrections.boolean", false);
	
	/** Debug tag: true to save JSON in IJ installation folder **/
	boolean debug=Prefs.get("KymoButler_debug.boolean", false);
	
	String helpMsg="<html>Version 1.0.0, 18 nov. 2019<br>"
			+ "This plugin is powered by <a href=\"https://deepmirror.ai/software/kymobutler/\">KymoButler</a><br>"
			+ "a webservice provided by Andrea Dimitracopoulos and Max Jakobs<br>"
			+ "based on their <a href=\"https://doi.org/10.7554/eLife.42288\">publication</a> you should cite when using the website/plugin:<br><br>"
			+ "This plugin heavily relies on external libraries:"
			+ "<ul>"
			+ "	<li>commons-io, v2.6</li>"
			+ "	<li>org.apache.httpcomponents/httpclient, v4.5.9</li>"
			+ "	<li>org.json/json, v20180813</li>"
			+ "</ul>"
			+ "<br><br>"
			+ "The plugin is brought to you by F.P. Cordelières <a href=\"mailto:fabrice.cordelieres@gmail.com?subject=KymoButler for IJ\">fabrice.cordelieres@gmail.com</a>";
	

	@Override
	public void run(String arg) {
		ip=WindowManager.getCurrentImage();
		if(KymoButlerIO.checkForLibraries()) {
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
		gd.addCheckbox("Allow_corrections", allowCorrections);
		
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
			allowCorrections=gd.getNextBoolean();
			
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
		Prefs.set("KymoButler_allowCorrections.boolean", allowCorrections);
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
					
					if(addToManager && allowCorrections) {
						WaitForUserDialog wfud= new WaitForUserDialog("Correct and re-train", "From the current detections list you may:"+"\n"
																							+" \n"
																							+ "1-Correct the detections:"+"\n"
																							+ "    a-Click on the track to correct in the ROI Manager"+"\n"
																							+ "    b-Modify the ROI on the image"+"\n"
																							+ "    c-Click 'update' button in the ROI Manager"+"\n"
																							+ "    d-Repeat for all tracks you want to modify"+"\n"
																							+" \n"
																							+ "2-Add detections:"+"\n"
																							+ "    a-Activate the polyline tool"+"\n"
																							+ "    b-Draw the missing track on the image"+"\n"
																							+ "    c-Click 'add' button in the ROI Manager"+"\n"
																							+ "    d-Repeat for all the missing tracks"+"\n"
																							+" \n"
																							+"Once done, please click on Ok"
																							);
						wfud.show();
						new KymoButler_Upload().run(null);
					}
					
					
					if(debug && pkr.hasSomethingToLog()) IJ.log(pkr.getSomethingToLog());
				}else {
					IJ.showStatus("The response doesn't seem to be properly formatted");
				}
			}
			
			if(debug) kbio.saveResults(response, IJ.getDirectory("imageJ")+(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()))+"_debug_KymoButler.json");			
		}else {
			IJ.showStatus("Nothing to do, please check at least one option");
		}
	}
}
