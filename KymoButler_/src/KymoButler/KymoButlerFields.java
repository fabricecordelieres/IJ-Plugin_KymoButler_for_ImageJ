/**
*
*  KymoButlerFields.java, 15 oct.. 2019
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

/**
 * This class lists all the fields the KymoButler uses to push/retrieve data for the server
 * @author Fabrice P. Cordelieres
 *
 */   
public class KymoButlerFields {
	/** Query field tag **/
	public static final String QUERY_FIELD_TAG="query";
	
	/** Query stats field tag **/
	public static final String QUERY_STATS_FIELD_TAG="stats";
	
	/** Query analysis field tag **/
	public static final String QUERY_ANALYSIS_FIELD_TAG="analysis";
	
	/** Query upload field tag **/
	public static final String QUERY_UPLOAD_FIELD_TAG="upload";
	
	
	
	/** Messages field tag **/
	public static final String MESSAGES_FIELD_TAG="messages";
	
	/** Error field tag **/
	public static final String ERROR_FIELD_TAG="error";
	
	/** Total number of kymographs in the API field tag **/
	public static final String MAX_KYMOGRAPHS_FIELD_TAG="MaxKymographs";
	
	/** Number of kymographs left in the API field tag **/
	public static final String KYMOGRAPHS_LEFT_FIELD_TAG="KymographsLeft";
	
	/** API version field tag **/
	public static final String VERSION_FIELD_TAG="Version";
	
	
	
	/** Kymograph field to push for analysis tag **/
	public static final String KYMOGRAPH_FIELD_TAG="Kymograph";
	
	/** Threshold field to push for analysis tag **/
	public static final String THRESHOLD_FIELD_TAG="p";
	
	/** Minimum size field to push for analysis tag **/
	public static final String MINIMUM_SIZE_FIELD_TAG="minimumSize";
	
	/** Minimum frames field to push for analysis tag **/
	public static final String MINIMUM_FRAMES_FIELD_TAG="minimumFrames";
	
	/** Overlay field tag **/
	public static final String OVERLAY_FIELD_TAG="overlay";
	
	/** Tracks field tag **/
	public static final String TRACKS_FIELD_TAG="tracks";
}
