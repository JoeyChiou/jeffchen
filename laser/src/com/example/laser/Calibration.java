package com.example.laser;

public class Calibration {
	
	
	public float[]  calibration(float[] x,float[] y)
	{
		   
		float[] mat=new float[16];
		
		float dx1 = x[1] - x[2], 	dy1 = y[1] - y[2];
		float dx2 = x[3] - x[2], 	dy2 = y[3] - y[2];
		float sx = x[0] - x[1] + x[2] - x[3];
		float sy = y[0] - y[1] + y[2] - y[3];
		float g = (sx * dy2 - dx2 * sy) / (dx1 * dy2 - dx2 * dy1);
		float h = (dx1 * sy - sx * dy1) / (dx1 * dy2 - dx2 * dy1);
		float a = x[1] - x[0] + g * x[1];
		float b = x[3] - x[0] + h * x[3];
		float c = x[0];
		float d = y[1] - y[0] + g * y[1];
		float e = y[3] - y[0] + h * y[3];
		float f = y[0];
		
		mat[ 0] = a;	mat[ 1] = d;	mat[ 2] = 0;	mat[ 3] = g;
		mat[ 4] = b;	mat[ 5] = e;	mat[ 6] = 0;	mat[ 7] = h;
		mat[ 8] = 0;	mat[ 9] = 0;	mat[10] = 1;	mat[11] = 0;
		mat[12] = c;	mat[13] = f;	mat[14] = 0;	mat[15] = 1;
		
		float A = e - f * h;
		float B = c * h - b;
		float C = b * f - c * e;
		float D = f * g - d;
		float E = a - c * g;
		float F = c * d - a * f;
		float G = d * h - e * g;
		float H = b * g - a * h;
		float I = a * e - b * d;
		
		float ideet = 1.0f / (a * A + b * D + c * G);
		
		mat[0] = A * ideet; mat[1] = D * ideet; mat[2] = 0; mat[3] = G * ideet;
        mat[4] = B * ideet; mat[5] = E * ideet; mat[6] = 0; mat[7] = H * ideet;
        mat[8] = 0; mat[9] = 0; mat[10] = 1; mat[11] = 0;
        mat[12] = C * ideet; mat[13] = F * ideet; mat[14] = 0; mat[15] = I * ideet;
      
        return mat;
        
	   }
	
    	
	}
	

