package org.rti.kidsthrive.secugenplugin;

public class ScanProperties {
	
	private int mImageWidth;
	private int mImageHeight;
	
	public int getmImageWidth() {
		return mImageWidth;
	}
	public void setmImageWidth(int mImageWidth) {
		this.mImageWidth = mImageWidth;
	}
	public int getmImageHeight() {
		return mImageHeight;
	}
	public void setmImageHeight(int mImageHeight) {
		this.mImageHeight = mImageHeight;
	}
	
	public ScanProperties()  {
	}
	
	public ScanProperties(int mImageWidth, int mImageHeight)  {
		this.setmImageWidth(mImageWidth);
		this.setmImageHeight(mImageHeight);
	}

}


