package org.opencv.tutorials.imgproc;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class UtilsOpenCV {
	private static final String TAG = "OCVSample::UtilsOpenCV";
	
	/**
	 * Method to scale screen coordinates to image coordinates, 
	 * as they have different resolutions.
	 * 
	 * x - width; y - height; 
	 * Nexus 4: xMax = 1196; yMax = 768
	 * 
	 * @param displayX
	 * @param displayY
	 * @return
	 */
	public static Point getImageCoordinates(WindowManager wManager, Mat image, float displayX, float displayY){
		Display display = wManager.getDefaultDisplay();		
		android.graphics.Point outSize = new android.graphics.Point();
		display.getSize(outSize);
		
		float xScale = outSize.x / (float) image.width();
		float yScale = outSize.y / (float) image.height();					
		
		return new Point(displayX/xScale, displayY/yScale);
	}
	

	
	public static Mat matRetrieve(String filePath, int rows, int cols, int type){
		Log.d(TAG, "matRetrieve - path: "+filePath);
		
    	if(isExternalStorageReadable()){
        	
        	File matFile = new File(filePath); 

        	InputStream in = null;
        	try {
        		in = new FileInputStream(matFile);
        		
        		byte[] data = convertInputStreamToByteArray(in);
        		
        		//Mat result = new Mat(320, 212, CvType.CV_8UC1);
        		        		
        		Mat result = new Mat(rows, cols, type);
        		result.put(0, 0, data);
        		
        		return result;
        		
        	} catch (FileNotFoundException e) {			
        		e.printStackTrace();
        	} finally {
        		if (in != null) {
        			try {
        				in.close();
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
        		}
        	}
    	} else {
    		Log.e(TAG, "External Storage not readable.");
    	}
    	
    	return null;
    }
	
    public static void matStore(String filePath, Mat mat){
    	Log.d(TAG, "matStore - path: "+filePath);
    	
    	if(isExternalStorageWritable() && mat.isContinuous()){
    		int cols = mat.cols();
        	int rows = mat.rows();
        	int elemSize = (int) mat.elemSize(); // or  mat.channels() ?        	
        	
        	byte[] data = new byte[cols * rows * elemSize];

        	mat.get(0, 0, data);

        	//System.arraycopy(b, 0, targetPixels, 0, b.length);          	
        	
        	File file = new File(filePath);
        	
        	writeByteArrayToFile(data, file);        	
    	} else {
    		Log.e(TAG, "External Storage not writable.");
    	}
    }
    
    public static String matToJson(Mat mat){
    	Log.d(TAG, "matToJson");
    	
    	JsonObject obj = new JsonObject();
    	
    	if(mat.isContinuous()){
    		int cols = mat.cols();
        	int rows = mat.rows();
        	int elemSize = (int) mat.elemSize();	
        	
        	byte[] data = new byte[cols * rows * elemSize];

        	mat.get(0, 0, data);
        	
        	obj.addProperty("rows", mat.rows()); 
        	obj.addProperty("cols", mat.cols()); 
        	obj.addProperty("type", mat.type());
        	
        	// We cannot set binary data to a json object, so:
        	// Encoding data byte array to Base64.
        	String dataString = new String(Base64.encode(data, Base64.DEFAULT));
        	
        	Log.d(TAG, "matToJson - dataString: "+dataString); 
        	
        	obj.addProperty("data", dataString);
        	
        	
        	Gson gson = new Gson();
        	String json = gson.toJson(obj);
        	
        	Log.d(TAG, "matToJson - json: "+json); 
        	
        	return json;
    	} else {
    		Log.e(TAG, "Mat not continuous.");
    	}
    	return "{}";
    }
    
    public static Mat matFromJson(String json){
    	Log.d(TAG, "matToJson");
    	
    	JsonParser parser = new JsonParser();
    	JsonObject JsonObject = parser.parse(json).getAsJsonObject();
    	
    	int rows = JsonObject.get("rows").getAsInt();
    	int cols = JsonObject.get("cols").getAsInt();
    	int type = JsonObject.get("type").getAsInt();
    	
    	String dataString = JsonObject.get("data").getAsString();    	
    	byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT); 
    	
    	Mat mat = new Mat(rows, cols, type);
    	mat.put(0, 0, data);
    	
    	return mat;
    }
    
    public static String keypointsToJson(MatOfKeyPoint mat){
    	if(mat!=null && !mat.empty()){    		
    		Gson gson = new Gson();
    		
    		JsonArray jsonArr = new JsonArray();    		
    		
    		KeyPoint[] array = mat.toArray();
    		for(int i=0; i<array.length; i++){
    			KeyPoint kp = array[i];
    			
    			JsonObject obj = new JsonObject();
    			
    			obj.addProperty("class_id", kp.class_id); 
    			obj.addProperty("x",        kp.pt.x);
    			obj.addProperty("y",        kp.pt.y);
    			obj.addProperty("size",     kp.size);
    			obj.addProperty("angle",    kp.angle);    			   			
    			obj.addProperty("octave",   kp.octave);
    			obj.addProperty("response", kp.response);
    			
    			jsonArr.add(obj);    			
    		}
    		
    		String json = gson.toJson(jsonArr);
    		
    		//Log.d(TAG, "keypointsStore: "+json);
    		
    		return json;
    	}
    	return "{}";
    }
    
    public static MatOfKeyPoint keypointsFromJson(String json){
    	MatOfKeyPoint result = new MatOfKeyPoint();
    	
    	JsonParser parser = new JsonParser();
        JsonArray jsonArr = parser.parse(json).getAsJsonArray();    	
    	
    	int size = jsonArr.size();
    	
    	KeyPoint[] kpArray = new KeyPoint[size];
    	
    	for(int i=0; i<size; i++){
    		KeyPoint kp = new KeyPoint(); 
    		
    		JsonObject obj = (JsonObject) jsonArr.get(i);
    		
    		Point point = new Point( 
    				obj.get("x").getAsDouble(), 
    				obj.get("y").getAsDouble() 
    		);    		
    		
    		kp.pt       = point;
    		kp.class_id = obj.get("class_id").getAsInt();
    		kp.size     =     obj.get("size").getAsFloat();
    		kp.angle    =    obj.get("angle").getAsFloat();
    		kp.octave   =   obj.get("octave").getAsInt();
    		kp.response = obj.get("response").getAsFloat();
    		
    		kpArray[i] = kp;
    	}
    	
    	result.fromArray(kpArray);
    	
    	return result;
    }
    
    private static void writeByteArrayToFile(byte[] data, File file){
    	OutputStream stream = null;
    	try {
    		stream = new BufferedOutputStream(new FileOutputStream(file));
    		stream.write(data);
    	} catch (IOException e) {
    		Log.e(TAG, "Failed to write file \"" + file.getPath()
    				+ "\". Exception is thrown: " + e);
    	} finally {
    		if (stream != null){
    			try {
    				stream.close();
    			} catch (IOException e) {
    				Log.e(TAG, "Exception is thrown: " + e);
    			}
    		}
    	}
    }
    
    private static byte[] convertInputStreamToByteArray(InputStream inputStream) {
    	byte[] bytes= null;

    	try {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();

    		byte buff[] = new byte[1024];
    		int count;

    		while ((count = inputStream.read(buff)) != -1) {
    			bos.write(buff, 0, count);
    		}

    		bos.flush();
    		bos.close();
    		inputStream.close();

    		bytes = bos.toByteArray();
    		
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return bytes;
    }
    
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
