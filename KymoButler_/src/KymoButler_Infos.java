/**
*
*  KymoButler_Infos.java, 8 oct. 2019
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
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 * This class is aimed at retrieving some informations about the API and the allowance when using the KymoButler webservice
 * @author Fabrice P. Cordelieres
 *
 */
public class KymoButler_Infos implements PlugIn{
	/** KymoButler API URL **/
	String URL=Prefs.get("KymoButler_URL.string", "");
	
	/** KymoButlerIO object: will handle all the analysis process **/
	KymoButlerIO kbio=new KymoButlerIO();
	
	/** The response retrieved from the server, as a String **/
	String response="";
	
	/** Debug tag: true to save JSON in IJ installation folder **/
	boolean debug=Prefs.get("KymoButler_debug.boolean", false);
	

	@Override
	public void run(String arg) {
		if(KymoButlerIO.checkForLibraries()) {
			if(!URL.isEmpty()) {	
				getInfos();
			}else {
				IJ.showMessage("No URL found for the API: please set one under the KymoButler/Options menu");
			}
		}else {
			IJ.showStatus("Installation of the required libraries needs to be done");	
		}
	}
	
	/**
	 * Retrieves the informations and displays them in a generic dialog box
	 */
	public void getInfos() {
		kbio.setURL(URL);
		response=kbio.getStatistics();
		
		if(response==null) {
			IJ.showStatus("Process cancelled, either by server or by user");
		}else {
			if(KymoButlerResponseParser.isJSON(response)){
				KymoButlerResponseParser pkr=new KymoButlerResponseParser(response);
				GenericDialog gd=new GenericDialog("KymoButler for ImageJ: informations");
				if(pkr.hasVersion()) gd.addMessage("<html><b>API version: </b>"+pkr.getVersion()+"</html>");
				if(pkr.hasKymographsLeft()) gd.addMessage("<html><b>Number of kymographs left: </b>"+pkr.getKymographsLeft()+"</html>");
				if(pkr.hasMaxKymographs()) gd.addMessage("<html><b>Max. number of kymographs allowed: </b>"+pkr.getMaxKymographs()+"</html>");
				if(debug && pkr.hasMessages()) gd.addMessage("<html><b>Message from the API: </b>"+pkr.getMessages()+"</html>");
				gd.showDialog();
			}else {
				IJ.showStatus("The response doesn't seem to be properly formatted");
			}
		}
		
		if(debug) kbio.saveResults(response, IJ.getDirectory("imageJ")+(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()))+"_debug_KymoButler.json");			
	}

}
