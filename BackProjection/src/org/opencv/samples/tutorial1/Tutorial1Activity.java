package org.opencv.samples.tutorial1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class Tutorial1Activity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    
    private Mat                  mIntermediateMat;
    private Mat 				 mGray;   

    private int outputWidth=300;
    private int outputHeight=200;
    private Mat mOutputROI;
    
    private Bitmap mBitmap;
   
    /// Global Variables
    private boolean bpUpdated = false;
    
    private Mat mRgba;
    private Mat mHSV;
    private Mat mask;

    private int lo = 20; 
    private int up = 20;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Tutorial1Activity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        if (mIsJavaCamera)
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        else
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Toggle Native/Java camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.setVisibility(SurfaceView.GONE);
            mIsJavaCamera = !mIsJavaCamera;

            if (mIsJavaCamera) {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
                toastMesage = "Java Camera";
            } else {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);
                toastMesage = "Native Camera";
            }

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMesage, Toast.LENGTH_LONG);
            toast.show();
        }

        return true;
    }

    public void onCameraViewStarted(int width, int height) {

    	mRgba = new Mat(height, width, CvType.CV_8UC3);
    	mHSV = new Mat();

    	mIntermediateMat = new Mat();
    	mGray = new Mat(height, width, CvType.CV_8UC1);

    	mOutputROI = new Mat(outputHeight, outputWidth, CvType.CV_8UC1);

    	mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);        
    }

    public void onCameraViewStopped() {
    	 // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();
        mIntermediateMat = null;
        
        if(mRgba!= null)
        	mRgba.release();
        mRgba = null;
        
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	Mat mCamera = inputFrame.rgba();
    	
    	Imgproc.cvtColor(mCamera, mRgba, Imgproc.COLOR_RGBA2RGB);
    	
    	Mat mOutputROI = mCamera.submat(0, outputHeight, 0, outputWidth);
    	
    	//Core.rectangle(roi, new Point(0,0), new Point(outputWidth-1, outputHeight-1), new Scalar(255,0,0));
    	
    	//Addition to remove some noise:
    	Imgproc.GaussianBlur(mRgba, mRgba, new Size(5, 5), 0, Imgproc.BORDER_DEFAULT);
    	
    	Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL);
    	
    	if(mask!=null){
    		if(bpUpdated==false){
    			mGray = histAndBackproj();
    		} else {
    			bpUpdated = false;
    		}
    		
    		Imgproc.resize(mGray, mIntermediateMat, mOutputROI.size(), 0, 0, Imgproc.INTER_LINEAR);
        	Imgproc.cvtColor(mIntermediateMat, mOutputROI, Imgproc.COLOR_GRAY2BGRA);
    	}

        return mCamera;
    }

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {

		// Fill and get the mask
		Point seed = getImageCoordinates(mRgba, arg1.getX(), arg1.getY());
		
		Log.d(TAG, "seed: "+seed +" type: "+CvType.typeToString(mRgba.type()));	
		
		int newMaskVal = 255;
		Scalar newVal = new Scalar( 120, 120, 120 );

		int connectivity = 8;
		int flags = connectivity + (newMaskVal << 8 ) + Imgproc.FLOODFILL_FIXED_RANGE + Imgproc.FLOODFILL_MASK_ONLY;

		Mat mask2 = Mat.zeros( mRgba.rows() + 2, mRgba.cols() + 2, CvType.CV_8UC1 );
		
		Rect rect = null;
		Imgproc.floodFill( mRgba, mask2, seed, newVal, rect, new Scalar( lo, lo, lo ), new Scalar( up, up, up), flags );
		
		
		// C++: 
		// mask = mask2( new Range( 1, mask2.rows() - 1 ), new Range( 1, mask2.cols() - 1 ) );
		mask = mask2.submat(new Range( 1, mask2.rows() - 1 ), new Range( 1, mask2.cols() - 1 ));

		mGray = histAndBackproj();
		bpUpdated = true;
		
		Log.d(TAG, "onTouch result: type:"+CvType.typeToString(mGray.type())+" size: "+mGray.size());
		
		
		return true;
	}
	
	// int mHistSizeNum = 25;
	// float[]  mBuff = new float[mHistSizeNum];
    // MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
	
	private Mat histAndBackproj() {
		Mat hist = new Mat();
		int h_bins = 30; 
		int s_bins = 32;
		
		// C++:
		//int histSize[] = { h_bins, s_bins };
		MatOfInt mHistSize = new MatOfInt (h_bins, s_bins);

		// C++:
		//float h_range[] = { 0, 179 };
		//float s_range[] = { 0, 255 };		
		//const float* ranges[] = { h_range, s_range };		
		//int channels[] = { 0, 1 };
		
		MatOfFloat mRanges = new MatOfFloat(0, 179, 0, 255);
		MatOfInt mChannels = new MatOfInt(0, 1);
	
		// C++:
		// calcHist( &hsv, 1, channels, mask, hist, 2, histSize, ranges, true, false );
		
		//check 'mask', it was mMat0 in ImageManipulationsActivity
		// 'mask' – Optional mask. If the matrix is not empty, it must be an 8-bit array of the same size as images[i] . 
		// The non-zero mask elements mark the array elements counted in the histogram.
		
		boolean accumulate = false;
        Imgproc.calcHist(Arrays.asList(mHSV), mChannels, mask, hist, mHistSize, mRanges, accumulate);
       
        // C++:
        // normalize( hist, hist, 0, 255, NORM_MINMAX, -1, Mat() );        
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX, -1, new Mat());
        
        // C++:
        // calcBackProject( &hsv, 1, channels, hist, backproj, ranges, 1, true );        
        Mat backproj = new Mat();
        Imgproc.calcBackProject(Arrays.asList(mHSV), mChannels, hist, backproj, mRanges, 1);

        return backproj;
	}
	
	
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
	private Point getImageCoordinates(Mat image, float displayX, float displayY){
		Display display = getWindowManager().getDefaultDisplay();		
		android.graphics.Point outSize = new android.graphics.Point();
		display.getSize(outSize);
		
		float xScale = outSize.x / (float) image.width();
		float yScale = outSize.y / (float) image.height();					
		
		return new Point(displayX/xScale, displayY/yScale);
	}
    
}
