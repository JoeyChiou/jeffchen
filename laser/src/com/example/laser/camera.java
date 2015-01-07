package com.example.laser;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

public class camera extends JavaCameraView{

	public camera(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
	}
	
	 public void setExposure(int value)
	    {
	    	Camera.Parameters p=mCamera.getParameters();
	    	p.setExposureCompensation(value);
	        mCamera.setParameters(p);
	    	
	    }
	 public int maxExposure()
	 {
		 Camera.Parameters p=mCamera.getParameters();
		 return p.getMaxExposureCompensation();
	 }
	 
	 public int minExposure()
	 {
		 Camera.Parameters p=mCamera.getParameters();
		 return p.getMinExposureCompensation();
	 }
	 

}
