package org.rti.kidsthrive.secugenplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.cordova.CallbackContext;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

public class Utils {

	// kudos: http://stackoverflow.com/a/8330635
	
	String TAG = "Utils";

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
 * @param callbackContext
 * @param b
 * @param fileName TODO
 */
public static void saveImageFile(CallbackContext callbackContext, Bitmap b, String fileName) {
	FileOutputStream out = null;
    try {
    	Log.d(SecugenPlugin.TAG, "Saving file to "+ SecugenPlugin.getTemplatePath() + fileName + ".png");
    	out = new FileOutputStream(SecugenPlugin.getTemplatePath() + fileName + ".png");
    	b.compress(Bitmap.CompressFormat.PNG, 100, out);
    	if (callbackContext != null) {
        	callbackContext.success("Fingerprint scan saved.");
    	}
    } catch (Exception e) {
    	Log.d(SecugenPlugin.TAG, "Error saving file to "+ SecugenPlugin.getTemplatePath() + fileName + ".png. Message: " + e);
    	e.printStackTrace();
    } finally {
    	try{
    		out.close();
    	} catch(Throwable ignore) {}
    }
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
  method reads in all lines of the file sequentially. */

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

}
