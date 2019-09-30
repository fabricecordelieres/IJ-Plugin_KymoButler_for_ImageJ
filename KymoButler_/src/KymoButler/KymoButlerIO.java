/**
*
*  KymoButlerIO.java, 20 juil. 2019
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;

/**
 * This class is aimed at pushing a kymograph to the KymoButler cloud, and retrieving both an image with tracks overlayed 
 * together with a table containing all detected tracks
 * @author Fabrice P. Cordelieres
 *
 */
public class KymoButlerIO{
	/** KymoButler API URL **/
	String URL=Prefs.get("KymoButler_URL.string", "");
	
	//Image to be processed, as a byte array
	byte[] img=null;
	
	//Parameter p (Threshold), default value 0.2
	String p="0.2";
	
	//Parameter minimumSize, default value 3
	String minimumSize="3";
	
	//Parameter minimumFrames, default value 3
	String minimumFrames="3";
	
	//Parameter simplifyTracks, default value true
	boolean simplifyTracks=true;
	
	/** Stores the time at which the analysis was started**/
	long startTime=(long) 0;
	
	/** The server timout response (default: 2 minutes) **/
	long timeOut=(long) Prefs.get("KymoButler_timeOut.double", 120000);
	
	/** The http POST request **/
	HttpPost httpPost =null;
	
	/** The http response **/
	HttpResponse response=null;
	
	/** The server response, as a JSON object containing the kymograph image, the overlay image and the tracks as a CSV formatted string **/
	JSONObject result;
	
	/** Keeps track of the user pressing the escape key: will cancel all the process **/
	boolean escPressed=false;
	
	
	
	/**
	 * Builds a new KymoButlerIO object (a kymograph should be set before launching analysis)
	 */
	public KymoButlerIO(){}
	
	/**
	 * Sets the kymograph: should be called before analysis takes place.
	 * It takes the active image as the ImagePlus to analyse
	 */
	public void setCurrentImageAsKymograph() {
		setKymograph(WindowManager.getCurrentImage());
	}
	
	/**
	 * Sets the kymograph: should be called before analysis takes place
	 * @param imagePath a String containing the path to the kymograph to analyse
	 */
	public void setKymograph(String imagePath) {
		try {
			img = FileUtils.readFileToByteArray(new File(imagePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			IJ.log("Something went wrong when trying to load the image: please check path ("+imagePath+") and file format");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the kymograph: should be called before analysis takes place
	 * @param ip an ImagePlus containing the kymograph to analyse
	 */
	public void setKymograph(ImagePlus ip) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			boolean isVisible=ip.isVisible();
			if(!isVisible) ip.show();
			ImageIO.write(ip.getBufferedImage(), "bmp", baos ); //Is not working if using tif...
			if(!isVisible) ip.hide();
			baos.flush();
			img= baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			IJ.log("Something went wrong when turning the input ImagePlus to a byte array");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the URL: should be called before analysis takes place
	 * @param URL the URL, as a String
	 */
	public void setURL(String URL) {
		this.URL=URL;
	}
	
	/**
	 * Returns the current URL parameter, as a String
	 * @return the current URL parameter, as a String
	 */
	public String getURL() {
		return URL;
	}
	
	/**
	 * Sets the threshold: should be called before analysis takes place
	 * @param threshold the detection threshold, as a float
	 */
	public void setThreshold(float threshold) {
		p=""+threshold;
	}
	
	/**
	 * Returns the current threshold parameter, as a float
	 * @return the current threshold parameter, as a float
	 */
	public float getThreshold() {
		return Float.parseFloat(p);
	}
	
	/**
	 * Sets the minimum track size: should be called before analysis takes place
	 * @param minimumSize the minimum expected number of pixels traveled for a track to be detected, as a float
	 */
	public void setMinimumSize(float minimumSize) {
		this.minimumSize=""+minimumSize;
	}
	
	/**
	 * Returns the current minimumSize parameter, as a float
	 * @return the current minimumSize parameter, as a float
	 */
	public float getMinimumSize() {
		return Float.parseFloat(minimumSize);
	}
	
	/**
	 * Sets the minimum frame number: should be called before analysis takes place
	 * @param minimumFrames the minimum expected number of frames composing a track for a track to be detected, as a float
	 */
	public void setMinimumFrames(float minimumFrames) {
		this.minimumFrames=""+minimumFrames;
	}
	
	/**
	 * Returns the current minimumFrames parameter, as a float
	 * @return the current minimumFrames parameter, as a float
	 */
	public float getMinimumFrames() {
		return Float.parseFloat(minimumFrames);
	}
	
	
	/**
	 * Sets the server's response timeout 
	 * @param timeOut the server's timeout, in seconds
	 */
	public void setTimeout(int timeOut) {
		this.timeOut=timeOut*1000;
	}
	
	/**
	 * Returns the server's response timeout, in seconds
	 */
	public int getTimeout() {
		return (int) timeOut/1000;
	}
	
	/**
	 * Pushes the image data and parameters to the KymoButler webapp.
	 * @return a String JSON formatted, containing the response (two images, kymograph and overlay, and the tracks as a CSV-style file)
	 */
	public String getAnalysisResults() {
		MultipartEntityBuilder builder=MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("Kymograph", img)
				.addTextBody("p", p)
				.addTextBody("minimumSize", minimumSize)
				.addTextBody("minimumFrames", minimumFrames);
		HttpEntity multiPartEntity = builder.build();
		
		httpPost = new HttpPost(URL);
		httpPost.setEntity(multiPartEntity);
		
		HttpClient client = HttpClientBuilder.create().build();
		startTime=System.currentTimeMillis();
		
		showMessage();
		
		
		try {
			response=client.execute(httpPost);
			
			IJ.showStatus("Analysis performed in "+getElapsedTime());
			
			//HttpEntity entity = response.getEntity();
			//String responseString = EntityUtils.toString(entity);//, "UTF-8");
			
			//JSONObject result=new JSONObject(responseString);
			//return result.toString();
			
			String out=EntityUtils.toString(response.getEntity(), "UTF-8");
			
			httpPost.releaseConnection();
			
			return out;
		
		} catch (IOException e) {
			IJ.log("Something went wrong while sending the request/getting the response to/from the server");
			//e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Pushes the image data and parameters to the KymoButler webapp, and saves the JSON response to a file
	 * @param outputPath path to the file where the JSON content will be saved
	 */
	public void saveAnalysisResults(String outputPath) {
		String  results=getAnalysisResults();
		try {
			FileUtils.writeStringToFile(new File(outputPath), results, "UTF-8");
		} catch (IOException e) {
			IJ.log("Something went wrong while saving the analysis results to the provided path "+outputPath);
			e.printStackTrace();
		}
	}
	
	/**
	 * This methods handles the display of the elapsed time in the status bar. It creates a new thread so that 
	 * display does not interfere with the analysis process while allowing the display to be updated.
	 */
	private void showMessage() {
		Thread t=new Thread() {
			public void run() {
				while(response==null && !escPressed && (System.currentTimeMillis()-startTime)<timeOut) {
					
					if(!IJ.escapePressed()) {
						IJ.showStatus("Analysis started "+getElapsedTime()+" ago, waiting for response");
					}else {
						httpPost.abort();
						IJ.showStatus("Analysis cancelled");
						escPressed=true;
					}
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
				
				if((System.currentTimeMillis()-startTime)>timeOut)  httpPost.abort();
			}
		};
		t.start();
	}
	
	/**
	 * Computes the elapsed time since the "startTime" as stored in the class variable
	 * @return the elapsed time since startTime as a string, using the mm:ss format
	 */
	private String getElapsedTime() {
		Date elapsedTime=new Date(System.currentTimeMillis()-startTime);
		SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
		return sdf.format(elapsedTime);
	}
}
