package org.rti.kidsthrive.secugenplugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.cordova.CallbackContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class Utils {

	// kudos: http://stackoverflow.com/a/8330635

	static String TAG = "SecugenPlugin Utils";

	/** Method to check whether external media available and writable. This is adapted from
   http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */

	private void checkExternalMedia(){
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// Can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// Can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Can't read or write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}   
		Log.d(TAG, "\n\nExternal Media: readable="
				+mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
	}

	/** Method to write ascii text characters to file on SD card. Note that you must add a 
   WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
   a FileNotFound Exception because you won't have write permission. */

	private void writeToSDFile(String dir, String filePath, byte[] buffer){

		// Find the root of the external storage.
		// See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

		File root = android.os.Environment.getExternalStorageDirectory(); 
		Log.d(TAG, "\nExternal file system root: "+root);

		// See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

		//    File dir = new File (root.getAbsolutePath() + "/download");
		File file = new File (dir + filePath);
		//    dir.mkdirs();
		//    File file = new File(dir, "myData.txt");

		try {
			FileOutputStream f = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(f);
			pw.println("Hi , How are you");
			pw.println("Hello");
			pw.flush();
			pw.close();
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i(TAG, "******* File not found. Did you" +
					" add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
		} catch (IOException e) {
			e.printStackTrace();
		}   
		Log.d(TAG, "\n\nFile written to "+file);
	}

	/**
	 * Saves image to PNG format
	 * @param callbackContext
	 * @param b
	 * @param fileName TODO
	 * @return outputFilename
	 */
	public static String saveImageFile(CallbackContext callbackContext, Bitmap b, String fileName) {
		FileOutputStream out = null;
		String extension = ".png";
		String outputFilename = fileName + extension;
		String outputPath = SecugenPlugin.getTemplatePath() + outputFilename;
		try {
			Log.d(SecugenPlugin.TAG, "Saving file to " + outputPath);
			out = new FileOutputStream(outputPath);
			b.compress(Bitmap.CompressFormat.PNG, 100, out);
//			if (callbackContext != null) {
//				callbackContext.success("Fingerprint scan saved.");
//			}
		} catch (Exception e) {
			Log.d(SecugenPlugin.TAG, "Error saving file to "+ outputPath + ". Message: " + e);
			e.printStackTrace();
		} finally {
			try{
				out.close();
			} catch(Throwable ignore) {}
		}
		return outputFilename;
	}

	public static Bitmap toGrayscale(Bitmap bmpOriginal)
	{        
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();    

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	public static void DumpFile(String fileName, byte[] buffer)
	{
		try {
			File myFile = new File(SecugenPlugin.getTemplatePath() + fileName);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			fOut.write(buffer,0,buffer.length);
			fOut.close();
		} catch (Exception e) {
			Log.d(SecugenPlugin.TAG, "Exception when writing file" + fileName);
		}
	}

	/** Method to read in a text file placed in the res/raw directory of the application. The
  method reads in all lines of the file sequentially. 
	 * @throws IOException */

	//private void readRaw(){
	//    Log.d(TAG, "\nData read from res/raw/textfile.txt:");
	//    InputStream is = this.getResources().openRawResource(R.raw.textfile);
	//    InputStreamReader isr = new InputStreamReader(is);
	//    BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size
	//
	//    // More efficient (less readable) implementation of above is the composite expression
	//    /*BufferedReader br = new BufferedReader(new InputStreamReader(
	//            this.getResources().openRawResource(R.raw.textfile)), 8192);*/
	//
	//    try {
	//        String test;    
	//        while (true){               
	//            test = br.readLine();   
	//            // readLine() returns null if no more lines in the file
	//            if(test == null) break;
	//            Log.d(TAG, "\n"+"    "+test);
	//        }
	//        isr.close();
	//        is.close();
	//        br.close();
	//    } catch (IOException e) {
	//        e.printStackTrace();
	//    }
	//    Log.d(TAG, "\n\nThat is all");
	//}
	
	// kudos: http://stackoverflow.com/questions/18964288/upload-a-file-through-an-http-form-via-multipartentitybuilder-with-a-progress
	public static String upload(String filename) throws IOException {
		String message = "";
		String pathToOurFile = SecugenPlugin.getTemplatePath() + filename;
		String urlServer = SecugenPlugin.getServerUrl() + SecugenPlugin.getServerUrlFilepath();
		HttpClient client = new DefaultHttpClient();
//		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(urlServer);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();   
		/* example for setting a HttpMultipartMode */
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("file", new FileBody(new File(pathToOurFile)));
		post.setEntity(builder.build());
		HttpResponse response = client.execute(post);
	    HttpEntity entity = response.getEntity();
	    message = response.getStatusLine().toString();  // CONSIDER  Detect server complaints
//	    InputStream is = entity.getContent();
	    String responseBody = EntityUtils.toString(entity);
//	    EntityUtils.consume(entity);
	    entity.consumeContent();
//	    client.close();
	    client.getConnectionManager().shutdown(); 
		
		return responseBody;
	}
	
	public static JSONObject post(JSONObject payload, String urlServer) throws IOException {
		Log.d(TAG, "urlServer: "+"    "+urlServer);
		HttpClient client = new DefaultHttpClient();
//		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(urlServer);
		//passes the results to a string builder/entity
	    StringEntity se = new StringEntity(payload.toString());

	    //sets the post request as the resulting string
	    post.setEntity(se);
	    //sets a request header so the page receving the request
	    //will know what to do with it
	    post.setHeader("Accept", "application/json");
	    post.setHeader("Content-type", "application/json");
//	    String authorizationString = "Basic " + Base64.encodeToString(("chris" + ":" + "chris").getBytes(), Base64.DEFAULT); //this line is diffe
//	    post.setHeader("Authorization", authorizationString);
//	    post.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("chris", "chris"), "UTF-8", false));

	    //Handles what is returned from the page 
	    ResponseHandler<String> responseHandler = new BasicResponseHandler();
	    String responseBody = client.execute(post, responseHandler);
		Log.d(TAG, "responseBody from service: "+"    "+responseBody);
		JSONObject responseBodyJo = null;
		try {
			responseBodyJo = new JSONObject(responseBody);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Log.d(TAG, "responseBodyJo: "+"    "+responseBodyJo.toString());
//	    ServiceResponse serviceResponse = new ServiceResponse();
//	    serviceResponse.setScannerPayload(payload);
//	    serviceResponse.setServiceMessage(responseBodyJo);
		JSONObject serviceResponse = new JSONObject();
		try {
			serviceResponse.put("scannerPayload", payload);
			serviceResponse.put("serviceMessage", responseBodyJo);
//			Log.d(TAG, "serviceResponse.get(scannerPayload).toString(): "+"    "+serviceResponse.get("scannerPayload").toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		se.consumeContent();
		client.getConnectionManager().shutdown();
//		Log.d(TAG, "serviceResponse.toString(): "+"    "+serviceResponse.toString());
		return serviceResponse;
	}

	// kudos: http://stunningco.de/2010/04/25/uploading-files-to-http-server-using-post-android-sdk/
	public static String uploadOld(String filename) throws IOException {
		String message = "";
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;
		String pathToOurFile = SecugenPlugin.getTemplatePath() + filename;
		String urlServer = SecugenPlugin.getServerUrl() + SecugenPlugin.getServerUrlFilepath();
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;

		int serverResponseCode = 0;
		String serverResponseMessage = "";

//		try
//		{
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

			Log.d(TAG, "urlServer: " + urlServer);
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs &amp; Outputs.
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Set HTTP method to POST.
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0)
			{
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = connection.getResponseCode();
			serverResponseMessage = connection.getResponseMessage();

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
//		}
//		catch (Exception ex)
//		{
//			LOG.d(TAG, "Error uploading file: " + serverResponseMessage);
//			throw new Exception(serverResponseMessage)
//			return serverResponseMessage;
//		}

		return String.valueOf(serverResponseCode);

	}
	
	// kudos: http://stackoverflow.com/a/9855338
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	 /**
	  * 
	  * kudos: Apache Commons Codec 1.6
	  * 
	  */
	

    /**
     * Used to build output as Hex
     */
	public static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    public static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	
    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexadecimal characters
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toLowerCase
     *            <code>true</code> converts to lowercase, <code>false</code> to uppercase
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }
	
	
	 /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     * 
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toDigits
     *            the output alphabet
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
	public static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }
	
	/**
	 * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
	 * The returned array will be double the length of the passed array, as it takes two characters to represent any
	 * given byte.
	 * 
	 * @param data
	 *            a byte[] to convert to Hex characters
	 * @param toDigits
	 *            the output alphabet
	 * @return A char[] containing hexadecimal characters
	 * @since 1.4
	 */
	public static String encodeHexToString(byte[] data, char[] toDigits) {
		int l = data.length;
		String out = "";
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out = out + toDigits[(0xF0 & data[i]) >>> 4];
			out = out + toDigits[0x0F & data[i]];
			out = out + " ";
		}
		return out;
	}

}
