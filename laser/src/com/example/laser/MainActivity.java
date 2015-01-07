package com.example.laser;


import java.util.Arrays;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.widget.SeekBar;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;


public class MainActivity extends Activity implements CvCameraViewListener2{


	private camera mOpenCvCameraView;
	private static final String TAG="MyActivity_OpenCVTest";
    Mat mRgba;
    Mat threshold;
    Mat threshold_scanning;
    SeekBar bar1;
    Button frame,tracking;
    Button ex_minus,ex_plus;
    TextView tv,cor,range;
    int button=0,exposure=5;
    float[] touchX=new float[4];
    float[] touchY=new float[4];
    float[] mat=new float[16];
    int count=0;
  //Bitmap�ϥήɻݭn���w���e�榡�M�j�p
    Bitmap bmp= Bitmap.createBitmap(176, 144, Bitmap.Config.RGB_565);
	
    int imagecount=0,status;
  //��l�ȳ]�w�P�i�઺�y�Ц�m �e�ۤ� �~���� �Ҧp������W�I �}�C����l�ȭn�a��k�U�I �H���L�k���
    int[][] correction={{1000,1000},{0,1000},{1000,0},{0,0}};
    
    Handler  mHandler = new Handler();
    TouchFunction tf= new TouchFunction();
    ImageProcess imp = new ImageProcess();
    Calibration cb=new Calibration();
    //Camera cam = new Camera();
    
    private BaseLoaderCallback mLoaderCallback=new BaseLoaderCallback(this){
        public void onManagerConnected(int status){
            switch(status){
                case LoaderCallbackInterface.SUCCESS:
                {
                     //�վ�e���ѪR��
                	 mOpenCvCameraView.setMaxFrameSize(176, 144);
                	 //�}����v��
                	 mOpenCvCameraView.enableView();
                }break;
                default:
                {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     mOpenCvCameraView = (camera) findViewById(R.id.laser_activity_java_surface_view);
	     mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE); 
	     
	    
	     mOpenCvCameraView.setCvCameraViewListener(this);
	     
	     
	    
	     bar1=(SeekBar)findViewById(R.id.seekBar1);
	     bar1.setMax(255);
		 bar1.setProgress(180);
		 
		 frame=(Button)findViewById(R.id.frame);
		 tracking=(Button)findViewById(R.id.tracking);
		 ex_minus=(Button)findViewById(R.id.minus);
		 ex_plus=(Button)findViewById(R.id.plus);
		 tv = (TextView)findViewById(R.id.text);
		 cor= (TextView)findViewById(R.id.correction);
		 range=(TextView)findViewById(R.id.range);	
		 
		 //�e������
		 frame.setOnClickListener(new Button.OnClickListener()
		   {
			 public void onClick(View arg0) {
		    if(button%2==0)
		    {
		    	Correction(threshold);
		    }
		    else {
				
			}
				 button++;
				 
		   }
		   });

		 
		 tracking.setOnClickListener(new Button.OnClickListener()
		   {
			 public void onClick(View arg0) 
			 {
				 imagecount++;
				 if(imagecount%2==1)
				 {
			     //�}�l���������
				 mHandler.post(imp);
				 }
				 else {
				//������������
					 mHandler.removeCallbacks(imp);
				}
		    }
		   
		   });
			
		 
		 //����n���վ�
		 ex_minus.setOnClickListener(new Button.OnClickListener()
		   {
		   public void onClick(View arg0) {
			   if(exposure > mOpenCvCameraView.minExposure())
			   {
			   exposure--;
			   mOpenCvCameraView.setExposure(exposure);
			   }
		   }
		   });
		 
		 //�W�[�n���վ�
		 ex_plus.setOnClickListener(new Button.OnClickListener()
		   {
		   public void onClick(View arg0) {
			   if(exposure<mOpenCvCameraView.maxExposure())
			   {
			   exposure++;
			   mOpenCvCameraView.setExposure(exposure);
			   }
		   }
		   });
	     
	}
	
	//����I���y��
	//���O���A��
	public boolean onTouchEvent(MotionEvent event) {
    	int cols = threshold.cols();
        int rows =threshold.rows();
    	int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;
    	switch(event.getAction() ) {
    	
        case MotionEvent.ACTION_DOWN:  // ���U
        	if ((x < 0) || (y < 0) || (x >  (mOpenCvCameraView.getWidth()*6)/10) || (y > mOpenCvCameraView.getHeight())) return false;
        	if(count<=3)
        	{
        		  touchX[count]= x;
        		  touchY[count]= y;
        		count++;
        		
        	}else{
        		count=0;
        	}
        	// cor.setText("X1: " + touchX[0] + ", Y1: " + touchY[0] + "\n" + "X2: " + touchX[1] + ", Y2: " + touchY[1] + "\n" + 
        			// "X3: " + touchX[2] + ", Y3: " + touchY[2] + "\n" + "X4: " + touchX[3] + ", Y1: " + touchY[3]);
        	
        	break;
        	
    	 }
    	return super.onTouchEvent(event);
    }
	 
	//������,���{���������
	 public class ImageProcess extends Thread  {
	        public void run() {
	        process(threshold);
            //�C������  imp�u�|����@��,���򩵿�0.001������handler�Ӷi�歫�ư���
	        mHandler.postDelayed(imp,1);
	   
	        }
	    }
	 
	 public class Camera extends Thread{
		 public void run()
		 {
			 mLoaderCallback.onManagerConnected(status);
		 }
	 }
     
	

	public void onPause()
	 {
	     super.onPause();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

	 public void onDestroy() {
	     super.onDestroy();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

	
	 public void onCameraViewStarted(int width, int height) {
		 
	 }

	 public void onCameraViewStopped() {
		 
	 }
	 
	 public void Correction(Mat image) {
		 int r=0,g=0,b=0,k=0;
		 float brightness=0,bright=0;
		 Bitmap bb= Bitmap.createBitmap(176, 144, Bitmap.Config.RGB_565);
		 Utils.matToBitmap(image, bb);
		 for (int i = 0; i < bb.getWidth(); i++)
         {
             for (int j = 0; j < bb.getHeight(); j++)
             {
            	 k=bb.getPixel(i, j);
                 r=Color.red(k);
				 g=Color.green(k);
				 b=Color.blue(k);
				 brightness+=(r+g+b);
             }  
         }
		//��ӵe����������
		 brightness=brightness/(bb.getWidth()*bb.getHeight());
		 for (int i = 0; i < bb.getWidth(); i++)
         {
             for (int j = 0; j < bb.getHeight(); j++)
             {
            	 k=bb.getPixel(i, j);
                 r=Color.red(k);
				 g=Color.green(k);
				 b=Color.blue(k);
				 bright=(r+g+b);
            	if(brightness<=bright)
            	{
            		//���W
            		if (i + j < correction[0][0] + correction[0][1])
                    {
            			correction[0][0] = i;
            			correction[0][1] = j;
                    }
            		
            		//�k�W
            		int ci=bmp.getWidth()-i;
            		int cj = j;
            		int ci1=bmp.getWidth()-correction[1][0];
            		int cj1 = correction[1][1];
            		if ((ci * ci) + (cj * cj) < (ci1 * ci1) + (cj1 * cj1))
                    {
            			correction[1][0] = i;
            			correction[1][1] = j;
                    }
            		
            		//���U
                    ci = i;
                    cj = bmp.getHeight() - j;
                    ci1 = correction[2][0];
                    cj1 = bmp.getHeight() - correction[2][1];
                    if ((ci * ci) + (cj * cj) < (ci1 * ci1) + (cj1 * cj1))
                    {
                    	correction[2][0] = i;
                    	correction[2][1] = j;
                    }
                    
                    //�k�U
                    if (i + j > correction[2][0] + correction[3][1])
                    {
                    	correction[3][0] = i;
                    	correction[3][1] = j;
                    }
                    ci = 0;
                    cj = 0;
                    ci1 = 0;
                    cj1 = 0;
            	}
             }
             
          }
		 cor.setText(correction[0][0]+","+correction[0][1]+"\n"+correction[1][0]+","+correction[1][1]+"\n"+
          correction[2][0]+","+correction[2][1]+"\n"+correction[3][0]+","+correction[3][1]);
		 Paint p =new Paint();
		 p.setColor(Color.RED);
		 p.setStyle(Paint.Style.STROKE);
		 Canvas canvas=new Canvas();
		 canvas.drawRect(correction[0][0]-1, correction[0][1]-1,correction[0][0]+1,correction[0][1]+1,p);
		 canvas.drawRect(correction[1][0]-1, correction[1][1]-1,correction[1][0]+1,correction[1][1]+1,p);
		 canvas.drawRect(correction[2][0]-1, correction[2][1]-1,correction[2][0]+1,correction[2][1]+1,p);
		 canvas.drawRect(correction[3][0]-1, correction[3][1]-1,correction[3][0]+1,correction[3][1]+1,p);
		 touchX[0]=correction[0][0];
		 touchX[1]=correction[1][0];
		 touchX[2]=correction[2][0];
		 touchX[3]=correction[3][0];
		 touchY[0]=correction[0][1];
		 touchY[1]=correction[1][1];
		 touchY[2]=correction[2][1];
		 touchY[3]=correction[3][1];
		 
		 mat=cb.calibration(touchX, touchY);
		 
	}
	 
	 public void process(Mat image)
	 {
		 int[][] pixel=new int[176][144];
		 int r,g,b,brightness;
		 float[] x=new float[3];
		 float[] y=new float[3];

		 //�G����@��,�קK�}�C�Ŷ�����,�������w�e����Ӥj�p
		 int[] X_left=new  int[176*144];
		 int[] Y_left=new  int[176*144];
		 int[] X_right=new  int[176*144];
		 int[] Y_right=new  int[176*144];
		 int[] Y=new  int[176*144];
		 int[] X=new  int[176*144];
		 int[] maxX=new int[3];
		 int[] maxY=new int[3];
		 int[] minY=new int[3];
		 int[] minX=new int[3];
		 int count=0,countt=0,counttt=0;
		Log.d("size", ""+image.cols()+","+image.rows()+","+image.depth());	
		 Utils.matToBitmap(image, bmp);
		 
		 
		 for(int i=0; i<bmp.getWidth(); i++)
		 {
			 for(int j=0; j<bmp.getHeight(); j++)
			 {
				 if(i==0&&j==0)
				 {
					 count=0;
					 countt=0;
					 counttt=0;
				 }
				 else {
				//����e���զ⪺����				
				 pixel[i][j]=bmp.getPixel(i, j);
				 r=Color.red(pixel[i][j]);
				 g=Color.green(pixel[i][j]);
				 b=Color.blue(pixel[i][j]);
				 brightness=(r+g+b)/3;
				 //�G�קP�_
				 if(brightness>200)
				 {
					 if(i<bmp.getWidth()/2)
					 {
					//���b��
					 X_left[count]=i;
					 Y_left[count]=j;
					 count++;
					 }
					 
					 if(i>bmp.getWidth()/2)
					 { 
						 //�k�b��
						 X_right[countt]=i;
						 Y_right[countt]=j;
						 countt++;
					 }
					 
					 X[counttt]=i;
					 Y[counttt]=j;
				 }
				
				 }
			 }
		 }
		 
		 
		 Arrays.sort(X);
		 Arrays.sort(Y);
		 //�]���}�C�ƧǷ|��}�C��Ӷi��Ƨ�,�̤p�Ȼݭn�t�~�P�_
		 for(int i=0; i<X.length; i++)
		 {
			 if(X[i]>0)
			 {
				 minX[0]=X[i];
				 break;
			 }
			 
		 }
		 
		 for(int i=0; i<Y.length; i++)
		 {
			 if(Y[i]>0)
			 {
				 minY[0]=Y[i];
				 break;				
			 }

		 }
		  
			 maxX[0]=X[X.length-1];
			 maxY[0]=Y[Y.length-1];	 
		 
			 x[0]=(maxX[0]+minX[0])/2;
			 y[0]=(maxY[0]+minY[0])/2;
		 
		 
		 Arrays.sort(X_left);
		 Arrays.sort(Y_left);
		 //�]���}�C�ƧǷ|��}�C��Ӷi��Ƨ�,�̤p�Ȼݭn�t�~�P�_
		 for(int i=0; i<X_left.length; i++)
		 {
			 if(X_left[i]>0)
			 {
				 minX[1]=X_left[i];
				 break;
			 }
			 
		 }
		 
		 for(int i=0; i<Y_left.length; i++)
		 {
			 if(Y_left[i]>0)
			 {
				 minY[1]=Y_left[i];
				 break;				
			 }

		 }
		  
			 maxX[1]=X_left[X_left.length-1];
			 maxY[1]=Y_left[Y_left.length-1];	 
		 
			 x[1]=(maxX[1]+minX[1])/2;
			 y[1]=(maxY[1]+minY[1])/2;
			 
			
			 
			 
			 Arrays.sort(X_right);
			 Arrays.sort(Y_right);
			 //�]���}�C�ƧǷ|��}�C��Ӷi��Ƨ�,�̤p�Ȼݭn�t�~�P�_
			 for(int i=0; i<X_right.length; i++)
			 {
				 if(X_right[i]>0)
				 {
					 minX[2]=X_right[i];
					 break;
				 }
				 
			 }
			 
			 for(int i=0; i<Y_right.length; i++)
			 {
				 if(Y_right[i]>0)
				 {
					 minY[2]=Y_right[i];
					 break;				
				 }

			 }
			 
			 maxX[2]=X_right[X_right.length-1];
			 maxY[2]=Y_right[Y_right.length-1];	 
		 
			 x[2]=(maxX[2]+minX[2])/2;
			 y[2]=(maxY[2]+minY[2])/2;
			 
			 tv.setText("x1:"+x[1]+","+"y1:"+y[1]+"\n"+"x2:"+x[2]+","+"y2:"+y[2]+"\n"
					 +"X:"+x[0]+","+"Y:"+y[0]);
			 resolution(x[0], y[0]);

	            
	 }
	 
	 public void resolution(float x,float y)
	 {
		 float[] result=new float[4];
		 float X = x;
         float Y = y;
         float Z = 0;
		 float u=0,v=0;
         result[0] = (float)(X * mat[0] + Y * mat[4] + Z * mat[8] + 1 * mat[12]);
         result[1] = (float)(X * mat[1] + Y * mat[5] + Z * mat[9] + 1 * mat[13]);
         result[2] = (float)(X * mat[2] + Y * mat[6] + Z * mat[10] + 1 * mat[14]);
         result[3] = (float)(X * mat[3] + Y * mat[7] + Z * mat[11] + 1 * mat[15]);
         u = result[0] / result[3];
         v = result[1] / result[3];
         DisplayMetrics monitorsize =new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(monitorsize);

         float xx=u*monitorsize.widthPixels;
         float yy=v*monitorsize.heightPixels;
         if(xx<=monitorsize.widthPixels&&yy<=monitorsize.heightPixels)
         {
         range.setText("�d��!!");
         }
         else {
         	range.setText("�d��~!!");
			}
         if(xx==0&&yy==0)
         {
        	 range.setText("�d��");
         }
	 }


	 public Mat onCameraFrame(CvCameraViewFrame  inputFrame) {
		 mRgba=inputFrame.rgba();
		 //�Ƕ�
		 threshold=inputFrame.gray();
		 threshold_scanning=inputFrame.gray();
		 if(button%2==0)
		 {			 
			 //�G�Ȥ�
			 Imgproc.threshold(threshold, threshold, bar1.getProgress(), 255, Imgproc.THRESH_BINARY);
			 
		     return  threshold;
		 }else
		 {		 
			 //���q�e��
			 return mRgba;
		 }
		 
	 }

	 public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu; this adds items to the action bar if it is present.
	        getMenuInflater().inflate(R.menu.main, menu);
	        return true;
	    }



	public void onResume()
	{
	    super.onResume();
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}
	

}
