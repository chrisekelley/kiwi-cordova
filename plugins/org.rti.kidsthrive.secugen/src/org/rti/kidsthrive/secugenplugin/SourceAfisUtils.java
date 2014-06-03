package org.rti.kidsthrive.secugenplugin;

import java.util.ArrayList;

import sourceafis.simple.AfisEngine;
import sourceafis.simple.Fingerprint;
import sourceafis.simple.Person;

public class SourceAfisUtils {

	/**
		 * Use SourceAFIS to identify the fingerprint.
		 * @param afis
	 * @param props height and width of image.
		 * kudos for 2d conversion: http://stackoverflow.com/q/18586813
	 * @param database TODO
		 */
		public static boolean identify(AfisEngine afis, ScanProperties props, byte[] mRegisterImage, byte[] mVerifyImage, ArrayList<Person> database) {
	
//			// seed the database with register
//			Person register = generateTemplate(afis, props, mRegisterImage, "register-sourceafis" + System.currentTimeMillis() + ".txt");
//	
//			// Add person to database
//			//			getDatabase().add(getPerson(1,mRegisterTemplate));
//			database.add(register);
			Person probe = generateTemplate(afis, props, mVerifyImage, "verify-sourceafis" + System.currentTimeMillis() + ".txt");
			
	//		/*giving dummy id -1 for probe*/
	//		Person probe = null;
	//		try {
	//			probe = getPerson(-1,new byte[][]{mVerifyTemplate});
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
			Iterable<Person> matches=afis.identify(probe, database);
			
			int i=0;
			for(Person match:matches){
				System.out.println("Matched::"+match.getId());
				i++;
			}
			
			if (i > 0) {
				return true;
			} else {
				return false;
			}
			
		}

	/**
	 * @param afis
	 * @param props
	 * @param image
	 * @param filename TODO
	 */
	public static Person generateTemplate(AfisEngine afis, ScanProperties props,
			byte[] image, String filename) {
		int height = props.getmImageHeight();
		int width = props.getmImageWidth();
		byte [][] image2d = new byte[width][height];
		//here is the main logic to convert 1D to 2D
		int x=0;
		for(int i=0;i<width;i++)
		{
			for(int j=0;j<height;j++)
			{
				image2d[i][j] = image[x];
				x++;
			}
		}
	
		Fingerprint fingerprint = new Fingerprint();
		try {
			//			fingerprint.setImage(new byte[][]{mRegisterImage});
			fingerprint.setImage(image2d);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Person person = new Person(fingerprint);
		afis.extract(person);
	
		byte[] template = fingerprint.getIsoTemplate();
		if (filename != null) {
			Utils.DumpFile(filename, template);
		}
		
		return person;
	}
	
	public static Person newPersonFromTemplate(AfisEngine afis, ScanProperties props,
			byte[] template, String sourceName) {
		int height = props.getmImageHeight();
		int width = props.getmImageWidth();

		Fingerprint fingerprint = new Fingerprint();
		try {
			//			fingerprint.setImage(new byte[][]{mRegisterImage});
			fingerprint.setIsoTemplate(template);
			fingerprint.setSourceName(sourceName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Person person = new Person(fingerprint);
		
		return person;
	}

}
