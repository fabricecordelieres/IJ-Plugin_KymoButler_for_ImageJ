/**
*
*  KymoButlerResponseParser.java, 20 juil. 2019
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
package KymoButler;


import java.awt.Point;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.HyperStackConverter;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;

/**
 * This class is aimed at parsing the JSON content from KymoButler into two images (kymograph and overlay) and a set of ROIs
 * @author Fabrice P Cordelieres, fabrice.cordelieres at gmail.com
 *
 */
public class KymoButlerResponseParser {
	/** Stores the JSON content **/
	JSONObject json=null;
	
	
	/**
	 * Builds a new parser, using the input path to get the JSON file
	 * @param dir folder in which the the JSON file is stored
	 * @param filename name of the JSON file
	 */
	public KymoButlerResponseParser(String dir, String filename) {
		try {
			json=new JSONObject(FileUtils.readFileToString(new File(dir+filename), "UTF-8"));
		} catch (JSONException e) {
			IJ.log("Error while reading the file: please check the file is of JSON type");
			e.printStackTrace();
		} catch (IOException e) {
			IJ.log("Error while reading the file: please check the provided path");
			e.printStackTrace();
		}
	}
	
	/**
	 * Builds a new parser, using the input string content as the JSON content
	 * @param JSONContent the content to parse, as a string
	 */
	public KymoButlerResponseParser(String JSONContent) {
		json=new JSONObject(JSONContent);
	}
	
	/**
	 * Tests if a String contains JSON information (check if the String starts with {)
	 * @param JSONContent the String to test
	 * @return true or false depending on if the content seems to be JSON or not
	 */
	public static boolean isJSON(String JSONContent) {
		return JSONContent.startsWith("{\n" + 
				"	\"");	
	}
	
	/**
	 * Checks if the kymograph data is present
	 * @return true if the kymograph data is present, false otherwise
	 */
	public boolean hasKymograph() {
		return json.has(KymoButlerFields.KYMOGRAPH_FIELD_TAG);
	}
	
	/**
	 * Parses the kymograph field from the KymoButler response and returns an ImageJ ImagePlus
	 * @return the extracted kymograph as an ImagePlus or null if the field was not found
	 */
	public ImagePlus getKymograph() {
		JSONArray kymograph=null;
		
		try {
			kymograph=json.getJSONArray(KymoButlerFields.KYMOGRAPH_FIELD_TAG);
		}catch (JSONException e) {
			IJ.log("The "+KymoButlerFields.KYMOGRAPH_FIELD_TAG+" section was not found: please check the JSON file");
			e.printStackTrace();
			return null;
		}
		
		
		//Get dimensions
		int height=kymograph.length();
		int width=(kymograph.getJSONArray(0)).length();
		
		ImagePlus ip=NewImage.createFloatImage("Kymograph", width, height, 1, NewImage.FILL_BLACK);
		
		for(int y=0; y<height; y++) {
			JSONArray line=kymograph.getJSONArray(y);
			for(int x=0; x<width; x++) {
				ip.getProcessor().putPixelValue(x, y, line.getDouble(x));
			}
		}
		
		return ip;
	}
	
	/**
	 * Parses the kymograph field from the KymoButler response and returns an ImageJ ImagePlus
	 * @param cal the calibration to apply to the kymograph
	 * @return the extracted kymograph as an ImagePlus or null if the field was not found
	 */
	public ImagePlus getKymograph(Calibration cal) {
		ImagePlus ip=getKymograph();
		if(ip!=null) ip.setCalibration(cal);
		
		return ip;
	}
	
	/**
	 * Parses the kymograph field from the KymoButler response and displays an ImageJ ImagePlus
	 */
	public void showKymograph() {
		ImagePlus ip=getKymograph();
		if(ip!=null) ip.show();
	}
	
	/**
	 * Parses the kymograph field from the KymoButler response and displays an ImageJ ImagePlus
	 * @param cal the calibration to apply to the kymograph
	 */
	public void showKymograph(Calibration cal) {
		ImagePlus ip=getKymograph();
		if(ip!=null) {
			ip.setCalibration(cal);
			ip.show();
		}
	}
	
	/**
	 * Checks if the overlay data is present
	 * @return true if the overlay data is present, false otherwise
	 */
	public boolean hasOverlay() {
		return json.has(KymoButlerFields.OVERLAY_FIELD_TAG);
	}
	
	/**
	 * Parses the overlay field from the KymoButler response and returns an ImageJ ImagePlus, to be displayed as a composite
	 * @return the extracted overlay as an ImagePlus, to be displayed as a composite or null if the field was not found
	 */
	public ImagePlus getOverlay() {
		JSONArray overlay=null;
		
		try {
			overlay=json.getJSONArray(KymoButlerFields.OVERLAY_FIELD_TAG);
		}catch (JSONException e) {
			IJ.log("The "+KymoButlerFields.OVERLAY_FIELD_TAG+" section was not found: please check the JSON file");
			return null;
		}
		
		//Get dimensions
		int height=overlay.length();
		int width=((JSONArray) overlay.get(0)).length();
		
		ImagePlus ip=NewImage.createFloatImage("Overlay", width, height, 3, NewImage.FILL_BLACK);
		
		for(int y=0; y<height; y++) {
			JSONArray line=overlay.getJSONArray(y);
			for(int x=0; x<width; x++) {
				JSONArray RGB=line.getJSONArray(x);
				for(int c=0; c<3; c++) {
					ip.getStack().getProcessor(c+1).putPixelValue(x, y, RGB.getDouble(c));
				}
			}
		}
		
		return HyperStackConverter.toHyperStack(ip, 3, 1, 1,"Composite");
	}
	
	/**
	 * Parses the overlay field from the KymoButler response and returns an ImageJ ImagePlus, to be displayed as a composite
	 * @param cal the calibration to apply to the kymograph
	 * @return the extracted overlay as an ImagePlus, to be displayed as a composite or null if the field was not found
	 */
	public ImagePlus getOverlay(Calibration cal) {
		ImagePlus ip=getOverlay();
		if(ip!=null) ip.setCalibration(cal);
		
		return ip;
	}
	
	/**
	 * Parses the overlay field from the KymoButler response and displays an ImageJ ImagePlus
	 */
	public void showOverlay() {
		ImagePlus ip=getOverlay();
		if(ip!=null) ip.show();
	}
	
	/**
	 * Parses the overlay field from the KymoButler response and displays an ImageJ ImagePlus
	 * @param cal the calibration to apply to the overlay
	 */
	public void showOverlay(Calibration cal) {
		ImagePlus ip=getOverlay();
		if(ip!=null) {
			ip.setCalibration(cal);
			ip.show();
		}
	}
	
	/**
	 * Checks if the tracks data is present
	 * @return true if the tracks data is present, false otherwise
	 */
	public boolean hasTracks() {
		return json.has(KymoButlerFields.TRACKS_FIELD_TAG);
	}
	
	/**
	 * Parses the tracks field from the KymoButler response and returns an array of ImageJ Rois
	 * @param simplifyTracks if true, the rois will be simplified into segments rather than being composed of one point per timepoint
	 * @return the extracted tracks as an array of ImagePlus Rois or null if the field was not found
	 */
	public Roi[] getTracks(boolean simplifyTracks) {
		JSONArray tracks=null;
		
		try {
			tracks=json.getJSONArray(KymoButlerFields.TRACKS_FIELD_TAG);
		}catch (JSONException e) {
			IJ.log("The "+KymoButlerFields.TRACKS_FIELD_TAG+" section was not found: please check the JSON file");
			return null;
		}
		
		//Get dimensions
		int nRois=tracks.length();
		
		Roi[] rois=new Roi[nRois];
		
		for(int i=0; i<nRois; i++) {
			JSONArray line=tracks.getJSONArray(i);
			FloatPolygon roi=new FloatPolygon();
			
			for(int j=0; j<line.length(); j++) {
				JSONArray coord=line.getJSONArray(j);
				roi.addPoint(coord.getDouble(1), coord.getDouble(0));
			}
			rois[i]=new PolygonRoi(roi, Roi.POLYLINE);
			if(simplifyTracks) rois[i]=simplifyTrack(rois[i]);
			rois[i].setName("Track_"+(i+1));
		}
		
		return rois;
	}
	
	/**
	 * Parses the tracks field from the KymoButler response and pushes all Rois to the RoiManager
	 * @param simplifyTracks if true, the rois will be simplified into segments rather than being composed of one point per timepoint
	 * @param clearRoiManager if true, the RoiManager will be emptied before adding new Rois
	 * @return the number of Rois found
	 */
	public int pushRoisToRoiManager(boolean simplifyTracks, boolean clearRoiManager) {
		RoiManager rm=RoiManager.getRoiManager();
		if(clearRoiManager) rm.reset();
		
		Roi[] rois=getTracks(simplifyTracks);
		int nRois=0;
		
		if(rois!=null) for(Roi roi:rois) if(roi!=null) {
			rm.add((ImagePlus) null, roi, -1);
			nRois++;
		}
		
		return nRois;
	}
	
	/**
	 * Parses the tracks field from the KymoButler response and pushes all Rois to the RoiManager
	 * @param simplifyTracks if true, the rois will be simplified into segments rather than being composed of one point per timepoint
	 * @return the number of Rois found
	 */
	public int pushRoisToRoiManager(boolean simplifyTracks) {
		return pushRoisToRoiManager(simplifyTracks, false);
	}
	
	/**
	 * KymoButler returns rois where all timepoints are exposed. This method simplifies the input roi by
	 * only exposing segments when the slope is changing.
	 * @param roi the input roi, on point per timepoint.
	 * @return a simplified roi, divided in segments.
	 */
	public Roi simplifyTrack(Roi roi) {
		Point[] points=roi.getContainedPoints();
		FloatPolygon polygon=new FloatPolygon();
		
		float deltaXOld=Float.NaN;
		
		for(int i=0; i<points.length-1; i++) {
			float deltaX=points[i].x-points[i+1].x;
			
			if(deltaX!=deltaXOld) { // The displacement along the Y axis is always the same, ie 1
				deltaXOld=deltaX;
				
				polygon.addPoint(points[i].getX(), points[i].getY());
			}
		}
		polygon.addPoint(points[points.length-1].getX(), points[points.length-1].getY());
		
		return new PolygonRoi(polygon, Roi.POLYLINE);
	}
	
	
	/**
	 * Checks if the messages is present
	 * @return true if the messages is present, false otherwise
	 */
	public boolean hasMessages() {
		return json.has(KymoButlerFields.MESSAGES_FIELD_TAG);
	}
	
	/**
	 * Parses the messages field from the KymoButler response and returns its content as a String
	 * @return the content of the messages field as a String, or null if the field was not found
	 */
	public String getMessages() {
		return getStringField(KymoButlerFields.MESSAGES_FIELD_TAG);
	}
	
	/**
	 * Checks if the MaxKymographs field is present
	 * @return true if the MaxKymographs is present, false otherwise
	 */
	public boolean hasMaxKymographs() {
		return json.has(KymoButlerFields.MAX_KYMOGRAPHS_FIELD_TAG);
	}
	
	/**
	 * Parses the MaxKymographs field from the KymoButler response and returns its content as an integer
	 * @return the content of the MaxKymographs field as an integer, or -1 if the field was not found
	 */
	public int getMaxKymographs() {
		return getIntField(KymoButlerFields.MAX_KYMOGRAPHS_FIELD_TAG);
	}
	
	/**
	 * Checks if the KymographsLeft field is present
	 * @return true if the KymographsLeft is present, false otherwise
	 */
	public boolean hasKymographsLeft() {
		return json.has(KymoButlerFields.KYMOGRAPHS_LEFT_FIELD_TAG);
	}
	
	/**
	 * Parses the KymographsLeft field from the KymoButler response and returns its content as an integer
	 * @return the content of the KymographsLeft field as an integer, or -1 if the field was not found
	 */
	public int getKymographsLeft() {
		return getIntField(KymoButlerFields.KYMOGRAPHS_LEFT_FIELD_TAG);
	}
	
	/**
	 * Checks if the Version field is present
	 * @return true if the Version is present, false otherwise
	 */
	public boolean hasVersion() {
		return json.has(KymoButlerFields.VERSION_FIELD_TAG);
	}
	
	/**
	 * Parses the Version field from the KymoButler response and returns its content as a String
	 * @return the content of the KymographsLeft field as an integer, or -1 if the field was not found
	 */
	public String getVersion() {
		return getStringField(KymoButlerFields.VERSION_FIELD_TAG);
	}
	
	/**
	 * Checks if the Messages, KymographsLeft, MaxKymographs or Version fields is/are present
	 * @return true if any of those fields is present, false otherwise
	 */
	public boolean hasSomethingToLog() {
		return hasMessages() || hasKymographsLeft() || hasMaxKymographs() || hasVersion();
	}
	
	/**
	 * Parses the Messages, KymographsLeft, MaxKymographs and/or Version fields from the KymoButler response and returns its/their content as a String
	 * @return the content of those fields as a String, empty String in case no field has been found
	 */
	public String getSomethingToLog() {
		String out="";
		if(hasMessages()) out+="Messages: "+getMessages();
		if(hasKymographsLeft()) out+=(out==""?"":"\n")+"Kymographs left: "+getKymographsLeft();
		if(hasMaxKymographs()) out+=(out==""?"":"\n")+"Max. kymographs: "+getMaxKymographs();
		if(hasVersion()) out+=(out==""?"":"\n")+"API version: "+getVersion();
		
		return out;
	}
	
	/**
	 * Looks for the designated key in the KymoButler response and returns the field's content as a String
	 * @param fieldKey The key to look for in the response
	 * @return the extracted field as a String or null if the field was not found
	 */
	public String getStringField(String fieldKey) {
		String fieldContent=null;
		
		try {
			fieldContent=json.getString(fieldKey);;
			return fieldContent;
		}catch (JSONException e) {
			IJ.log("The "+fieldKey+" section was not found: please check the JSON file");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Looks for the designated key in the KymoButler response and returns the field's content as an integer
	 * @param fieldKey The key to look for in the response
	 * @return the extracted field as an integer or -1 if the field was not found
	 */
	public int getIntField(String fieldKey) {
		int fieldContent=-1;
		
		try {
			fieldContent=json.getInt(fieldKey);;
			return fieldContent;
		}catch (JSONException e) {
			IJ.log("The "+fieldKey+" section was not found: please check the JSON file");
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Saves the full JSON content to interpret as a JSON file
	 * @param outputPath targeted path (including the filename and extension), as a String
	 */
	public void saveAsJSON(String outputPath) {
		try {
			FileUtils.writeStringToFile(new File(outputPath), json.toString(), "UTF-8");
		} catch (IOException e) {
			IJ.log("Something went wrong while saving the JSON content to the provided path "+outputPath);
			e.printStackTrace();
		}
	}

}
