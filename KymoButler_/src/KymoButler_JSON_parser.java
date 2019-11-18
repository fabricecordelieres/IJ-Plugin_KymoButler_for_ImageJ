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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import KymoButler.KymoButlerResponseParser;
import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

/**
 * This class is aimed at launching the analysis of kymographs using the KymoButler webservice
 * @author Fabrice P. Cordelieres
 *
 */
public class KymoButler_JSON_parser implements PlugIn{
	/** The folder in which the JSON file to parse is stored **/
	String dir="";
	
	/** The name of the JSON file to parse **/
	String filename="";
	
	/** KymoButler API URL **/
	String URL=Prefs.get("KymoButler_URL.string", "");
	
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
			+ "based on their <a href=\"https://doi.org/10.7554/eLife.42288\">publication</a> you should cite when using the website/plugin:<br><br>"
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
		if(checkForLibraries()) {
			if(!URL.isEmpty()) {	
				if(getJSONFile()) showGUI();
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
		String[] classesNames=new String[] {"commons-io-2.6.jar", "commons-logging-1.2.jar", "commons-codec-1.11.jar", "httpclient-4.5.9.jar", "httpcore-4.4.11.jar", "httpmime-4.5.9.jar", "json-20180813.jar"};
		
		String[] classesToFind=new String[] {"org.apache.commons.io.FileUtils",
											 "org.apache.commons.logging.Log",
											 "org.apache.commons.codec.Charsets",
											 "org.apache.http.client.HttpClient",
											 "org.apache.http.HttpEntity",
											 "org.apache.http.entity.mime.HttpMultipartMode",
											 "org.json.JSONObject"};
		
		String msg="";
		
		for(int i=0; i<classesNames.length; i++) {
			Class<?> toFind=null;
			
			try {
				toFind=Class.forName(classesToFind[i]);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			boolean found=toFind!=null;
			if(debug) IJ.log("Check "+classesNames[i]+": "+(found?"":"not ")+"found");
			
			if(!found) msg=msg+(!msg.isEmpty()?"\n":"")+classesNames[i];
		}
		
		if(!msg.isEmpty()) IJ.error("The following libraries are missing:\n"+msg);
		
		return msg.isEmpty();
	}
	
	/**
	 * Displays a file chooser box to pick the JSON file to parse
	 * @return true if a file was selected and the GUI Oked, false otherwise
	 */
	public boolean getJSONFile() {
		OpenDialog od=new OpenDialog("Select the JSON file to parse", OpenDialog.getLastDirectory(), "*.JSON");
		dir=od.getDirectory();
		filename=od.getFileName();
		
		if(dir==null || filename==null)  return false;
		
		return true;
	}
		
	/**
	 * Displays the GUI, stores the parameters and launches the analysis
	 */
	public void showGUI() {
		GenericDialog gd=new GenericDialog("KymoButler for IJ by fabrice.cordelieres@gmail.com");
		gd.addCheckbox("Add to manager", addToManager);
		gd.addCheckbox("Simplify tracks", simplifyTracks);
		gd.addCheckbox("Clear manager before adding", clearManager);
		gd.addCheckbox("Show_kymograph", showKymo);
		gd.addCheckbox("Show_overlay", showOverlay);
		
		gd.addHelp(helpMsg);
		gd.showDialog();
		
		if(gd.wasOKed()) {
			addToManager=gd.getNextBoolean();
			simplifyTracks=gd.getNextBoolean();
			clearManager=gd.getNextBoolean();
			showKymo=gd.getNextBoolean();
			showOverlay=gd.getNextBoolean();
			
			storePreferences();
			
			parseJSONFile();
		}
	}
	
	/**
	 * Stores preferences, based on the user input
	 */
	public void storePreferences() {
		Prefs.set("KymoButler_addToManager.boolean", addToManager);
		Prefs.set("KymoButler_simplifyTracks.boolean", simplifyTracks);
		Prefs.set("KymoButler_clearManager.boolean", clearManager);
		Prefs.set("KymoButler_showKymo.boolean", showKymo);
		Prefs.set("KymoButler_showOverlay.boolean", showOverlay);
	}
	
	/**
	 * Launches analysis once all parameters have been set, returns all images and ROIs
	 */
	public void parseJSONFile() {
		if(showKymo || showOverlay || addToManager) {
			String response=null;
			
			try {
				response = FileUtils.readFileToString(new File(dir+filename), "UTF-8");
				
				if(response==null) {
					IJ.showStatus("Nothing to read: the JSON file seems empty");
				}else {
					if(KymoButlerResponseParser.isJSON(response)){
						KymoButlerResponseParser pkr=new KymoButlerResponseParser(response);
						if(addToManager) pkr.pushRoisToRoiManager(simplifyTracks, clearManager);
						if(showKymo) pkr.showKymograph(null);
						if(showOverlay) pkr.showOverlay(null);
					}else {
						IJ.showStatus("The response doesn't seem to be properly formatted");
					}
				}
			} catch (IOException e) {
				IJ.showMessage("A problem occured while trying to read the JSON file");
			}
			
			if(debug)
				try {
					FileUtils.writeStringToFile(new File(IJ.getDirectory("imageJ")+(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()))+"_debug_KymoButler.json"), response, "UTF-8");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}else {
			IJ.showStatus("Nothing to do, please check at least one option");
		}
	}
}
