/**
*
*  KymoButler_Upload.java, 8 oct. 2019
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
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

/**
 * This class is aimed at uploading a kymograph together with ROIs to the KymoButler webservice
 * @author Fabrice P. Cordelieres
 *
 */
public class KymoButler_Upload implements PlugIn{
	/** KymoButler API URL **/
	String URL=Prefs.get("KymoButler_URL.string", "");
	
	/** The ImagePlus that is present at startup (or null) **/
	ImagePlus ip=null;
	
	/** The RoiManager that is present at startup (or null) **/
	RoiManager rm=null;
	
	/** KymoButlerIO object: will handle all the analysis process **/
	KymoButlerIO kbio=new KymoButlerIO();
	
	/** Debug tag: true to save JSON in IJ installation folder **/
	boolean debug=Prefs.get("KymoButler_debug.boolean", false);
	
	@Override
	public void run(String arg) {
		ip=WindowManager.getCurrentImage();
		rm=RoiManager.getRoiManager();
		
		if(KymoButlerIO.checkForLibraries()) {
			if(!URL.isEmpty()) {	
				if(ip!=null) {
					if(rm.getCount()!=0) {
						upload();
					}else {
						IJ.showMessage("Nothing to do, please add ROIs to the ROI Manager first");
					}
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
	 * Uploads the kymograph and the ROIs to KymoButler webapp
	 */
	public void upload() {
		kbio.setURL(URL);
		kbio.setKymograph(ip);
		kbio.setTracks();
		
		String response=kbio.upload();
		if(KymoButlerResponseParser.isJSON(response)){
			KymoButlerResponseParser pkr=new KymoButlerResponseParser(response);
			if(debug && pkr.hasSomethingToLog()) IJ.log(pkr.getSomethingToLog());
		}
		
		if(debug) kbio.saveResults(response, IJ.getDirectory("imageJ")+(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()))+"_debug_KymoButler.json");
	}
}
