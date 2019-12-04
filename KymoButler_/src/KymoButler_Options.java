/**
*
*  KymoButler_Options.java, 20 juil. 2019
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

import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 * This class is aimed at tuning wome options used by the KymoButler for ImageJ plugin
 * @author Fabrice P. Cordelieres
 *
 */
public class KymoButler_Options implements PlugIn{
	/** KymoButler default API **/
	public static final String DEFAULT_API="https://www.wolframcloud.com/obj/deepmirror/Projects/KymoButler/API/Public/publicAPI ";
	
	/** Use default API ? **/
	boolean useDefaultAPI=Prefs.get("KymoButler_useDefaultAPI.boolean", true);
	
	/** KymoButler API URL **/
	String URL=Prefs.get("KymoButler_URL.string", "");
	
	/** The server timout response (default: 2 minutes) **/
	long timeOut=(long) Prefs.get("KymoButler_timeOut.double", 120000);
	
	/** Debug tag: true to save JSON in IJ installation folder **/
	boolean debug=Prefs.get("KymoButler_debug.boolean", false);
	
	/**
	 * Displays the GUI and stores the parameters
	 */
	@Override
	public void run(String arg) {
		GenericDialog gd=new GenericDialog("KymoButler for IJ options by fabrice.cordelieres@gmail.com");
		gd.addStringField("KymoButler_API_URL", URL);
		gd.addCheckbox("User_default_API_?_(Limited_but_free)", useDefaultAPI);
		gd.addNumericField("Server_timeout (default: 120 sec)", timeOut/1000, 0);
		gd.addCheckbox("Debug_mode (default: false)", debug);
		gd.showDialog();
		
		if(gd.wasOKed()) {
			URL=gd.getNextString();
			useDefaultAPI=gd.getNextBoolean();
			timeOut=(long) (gd.getNextNumber()*1000);
			debug=gd.getNextBoolean();
			
			storePreferences();
		}
	}
	
	/**
	 * Stores preferences, based on the user input
	 */
	public void storePreferences() {
		if(useDefaultAPI) URL=DEFAULT_API;
		Prefs.set("KymoButler_URL.string", URL);
		Prefs.set("KymoButler_useDefaultAPI.boolean", useDefaultAPI);
		Prefs.set("KymoButler_timeOut.double", timeOut);
		Prefs.set("KymoButler_debug.boolean", debug);
	}
}
