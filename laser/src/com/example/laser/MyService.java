package com.example.laser;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyService extends Service{
     private static final String Context = null;
	
	 

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	    public void onStart(Intent intent, int startId) {
	  	
		}
		public void onDestroy(){
		super.onDestroy();
		
		}
}
