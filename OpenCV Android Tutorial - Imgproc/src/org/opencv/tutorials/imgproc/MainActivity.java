package org.opencv.tutorials.imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    
    public static final int      VIEW_MODE_A     = 0;
    public static final int      VIEW_MODE_B     = 1;
    public static final int      VIEW_MODE_C     = 2;
    public static final int      VIEW_MODE_D     = 3;
    public static final int      VIEW_MODE_E     = 4;
    public static final int      VIEW_MODE_F     = 5;
    public static final int      VIEW_MODE_G     = 6;
    public static final int      VIEW_MODE_H     = 7;
    public static final int      VIEW_MODE_I     = 8;
    public static final int      VIEW_MODE_J     = 9;
    
    public static int            viewMode = VIEW_MODE_A;
    
    private MenuItem             mItemSwitchCamera = null;
    private MenuItem             mItemPreviewA;
    private MenuItem             mItemPreviewB;
    private MenuItem             mItemPreviewC;
    private MenuItem             mItemPreviewD;
    private MenuItem             mItemPreviewE;
    private MenuItem             mItemPreviewF;
    private MenuItem             mItemPreviewG;
    private MenuItem             mItemPreviewH;
    private MenuItem             mItemPreviewI;
    private MenuItem             mItemPreviewJ;
    
    private Mat                  mIntermediateMat;
    private Mat 				 mGray;   

    private Bitmap mBitmap;

    private Mat mRgba;
    private Mat mHSV;
    
    private int thresh = 100;
    private int max_thresh = 255;
    
    /// Global Variables
    int GLOBAL_ITER = 1;
    
    int DELAY_CAPTION = 1500;
    int DELAY_BLUR = 100;
    int MAX_KERNEL_LENGTH = 31;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
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
        mItemPreviewA  = menu.add("blur");
        mItemPreviewB = menu.add("gaussian");
        mItemPreviewC = menu.add("median");
        mItemPreviewD = menu.add("bilateral");
        mItemPreviewE = menu.add("erosion");
        mItemPreviewF = menu.add("dilation");
        mItemPreviewG = menu.add("morph");
        mItemPreviewH = menu.add("findContours");
        mItemPreviewI = menu.add("t7");
        mItemPreviewJ = menu.add("t7");
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
        } else if (item == mItemPreviewA){
            viewMode = VIEW_MODE_A;
        } else if (item == mItemPreviewB){
            viewMode = VIEW_MODE_B;
        } else if (item == mItemPreviewC){
        	viewMode = VIEW_MODE_C;
        } else if (item == mItemPreviewD){
            viewMode = VIEW_MODE_D;
        } else if (item == mItemPreviewE){
        	viewMode = VIEW_MODE_E;
        } else if (item == mItemPreviewF){
            viewMode = VIEW_MODE_F;
        } else if (item == mItemPreviewG){
        	viewMode = VIEW_MODE_G;
        } else if (item == mItemPreviewH){
            viewMode = VIEW_MODE_H;
        } else if (item == mItemPreviewI){
            viewMode = VIEW_MODE_I;
        } else if (item == mItemPreviewJ){
            viewMode = VIEW_MODE_J;
        }
        
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
    	mRgba = new Mat(height, width, CvType.CV_8UC3);
    	mHSV = new Mat();

    	mIntermediateMat = new Mat(height, width, CvType.CV_8UC3);
    	mGray = new Mat(height, width, CvType.CV_8UC1);  

    	mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);        
    }

    public void onCameraViewStopped() {
    	 // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();
        mIntermediateMat = null;
        
        if(mHSV!= null)
        	mHSV.release();
        mHSV = null;
        
        if(mGray!= null)
        	mGray.release();
        mGray = null;
        
        if (mIntermediateMat != null)
            mIntermediateMat.release();
        mIntermediateMat = null;
        
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	mRgba = inputFrame.rgba();    	

    	switch (MainActivity.viewMode) {
    	
    	case MainActivity.VIEW_MODE_A:
    		homogeneousBlurTutorial();
    		break;
    	case MainActivity.VIEW_MODE_B:
    		gaussianBlurTutorial();
    		break;
		case MainActivity.VIEW_MODE_C:
			medianBlurTutorial();
		    break;
		case MainActivity.VIEW_MODE_D:
			bilateralFilterTutorial();
			break;
		case MainActivity.VIEW_MODE_E:
			erosionTutorial(0);
			break;
		case MainActivity.VIEW_MODE_F:
			dilationTutorial(0);
			break;
		case MainActivity.VIEW_MODE_G:
			morphologyOperations(Imgproc.MORPH_GRADIENT, Imgproc.MORPH_RECT);
			break;
		case MainActivity.VIEW_MODE_H:
    		findContoursTutorial();
    		break;
	    case MainActivity.VIEW_MODE_I:
			findContoursTutorial();
			break;
    	case MainActivity.VIEW_MODE_J:
    		findContoursTutorial();
    		break;    		
    	}
    	
    	
        return mRgba;
    }

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {

		// Fill and get the mask
		//Point seed = UtilsOpenCV.getImageCoordinates(getWindowManager(), mRgba, arg1.getX(), arg1.getY());
		
		//Log.d(TAG, "seed: "+seed +" type: "+CvType.typeToString(mRgba.type()));		

		return true;
	}
	
	private void homogeneousBlurTutorial(){
		Mat ROI = getProcessingROI(mRgba);
		
		/// Applying Homogeneous blur    		   
		if( GLOBAL_ITER < MAX_KERNEL_LENGTH ){ 
			Imgproc.blur( ROI, ROI, new Size( GLOBAL_ITER, GLOBAL_ITER ), new Point(-1,-1) );
			GLOBAL_ITER = GLOBAL_ITER + 2; 
		} else {
			GLOBAL_ITER = 1;
		}
	}
	
	private void gaussianBlurTutorial(){
		Mat ROI = getProcessingROI(mRgba);
		
		if( GLOBAL_ITER < MAX_KERNEL_LENGTH){ 
			Imgproc.GaussianBlur( ROI, ROI, new Size( GLOBAL_ITER, GLOBAL_ITER ), 0, 0 );
			GLOBAL_ITER = GLOBAL_ITER + 2; 
		} else {
			GLOBAL_ITER = 1;
		}
	}
	
	private void medianBlurTutorial(){
		Mat ROI = getProcessingROI(mRgba);
		
		if( GLOBAL_ITER < MAX_KERNEL_LENGTH){ 
			Imgproc.medianBlur( ROI, ROI, GLOBAL_ITER );
			GLOBAL_ITER = GLOBAL_ITER + 2; 
		} else {
			GLOBAL_ITER = 1;
		}
	}
	
	private void bilateralFilterTutorial(){
		// bilateralFilter expects 3 channels, mRgba originaly has 4.
		Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGBA2RGB);
		
		Mat ROI = getProcessingROI(mRgba);
		Mat outputROI = getProcessingROI(mIntermediateMat);		
		
		if( GLOBAL_ITER < MAX_KERNEL_LENGTH){ 
			Imgproc.bilateralFilter( ROI, outputROI, GLOBAL_ITER, GLOBAL_ITER*2, GLOBAL_ITER/2);
			GLOBAL_ITER = GLOBAL_ITER + 2; 
		} else {
			GLOBAL_ITER = 1;
		}
		
		Imgproc.cvtColor(outputROI, ROI, Imgproc.COLOR_RGB2RGBA);
	}
	
	private void erosionTutorial(int erosion_elem){
		int erosion_type = Imgproc.MORPH_RECT;
		if( erosion_elem == 0 ){ erosion_type = Imgproc.MORPH_RECT; }
		else if( erosion_elem == 1 ){ erosion_type = Imgproc.MORPH_CROSS; }
		else if( erosion_elem == 2) { erosion_type = Imgproc.MORPH_ELLIPSE; }

		int erosion_size = 1;

		Mat element = Imgproc.getStructuringElement( erosion_type,
				new Size( 2*erosion_size + 1, 2*erosion_size+1 ),
				new Point( erosion_size, erosion_size ) );

		Mat ROI = getProcessingROI(mRgba);
		Mat grayROI = getProcessingROI(mGray);

		Imgproc.cvtColor(ROI, grayROI, Imgproc.COLOR_RGBA2GRAY);

		// Morphology operations expect a binary image:
		Imgproc.threshold(grayROI, grayROI, 100, 255, Imgproc.THRESH_OTSU);

		// Apply the erosion operation
		Imgproc.erode( grayROI, grayROI, element );

		Imgproc.cvtColor(grayROI, ROI, Imgproc.COLOR_GRAY2RGBA);
	}
	
	private void dilationTutorial(int dilation_elem){
		int dilation_type = Imgproc.MORPH_RECT;
		if( dilation_elem == 0 ){ dilation_type = Imgproc.MORPH_RECT; }
		else if( dilation_elem == 1 ){ dilation_type = Imgproc.MORPH_CROSS; }
		else if( dilation_elem == 2) { dilation_type = Imgproc.MORPH_ELLIPSE; }

		int dilation_size = 1;

		Mat element = Imgproc.getStructuringElement( dilation_type,
				new Size( 2*dilation_size + 1, 2*dilation_size+1 ),
				new Point( dilation_size, dilation_size ) );

		Mat ROI = getProcessingROI(mRgba);
		Mat grayROI = getProcessingROI(mGray);

		Imgproc.cvtColor(ROI, grayROI, Imgproc.COLOR_RGBA2GRAY);

		// Morphology operations expect a binary image:
		Imgproc.threshold(grayROI, grayROI, 100, 255, Imgproc.THRESH_OTSU);

		// Apply the dilation operation
		Imgproc.dilate( grayROI, grayROI, element );

		Imgproc.cvtColor(grayROI, ROI, Imgproc.COLOR_GRAY2RGBA);
	}
	
	private void morphologyOperations( int morph_operator, int morph_elem ){
		// Since MORPH_X : 2,3,4,5 and 6
		//int operation = morph_operator + 2;
		
		int morph_size = 2;

		Mat element = Imgproc.getStructuringElement( morph_elem, 
				new Size( 2*morph_size + 1, 2*morph_size+1 ), 
				new Point( morph_size, morph_size ) );

		Mat ROI = getProcessingROI(mRgba);
		Mat grayROI = getProcessingROI(mGray);

		Imgproc.cvtColor(ROI, grayROI, Imgproc.COLOR_RGBA2GRAY);
		
		// Morphology operations expect a binary image:
		Imgproc.threshold(grayROI, grayROI, 100, 255, Imgproc.THRESH_OTSU);

		/// Apply the specified morphology operation
		Imgproc.morphologyEx( grayROI, grayROI, morph_operator, element );
		
		Imgproc.cvtColor(grayROI, ROI, Imgproc.COLOR_GRAY2RGBA);
	}
	
	private void findContoursTutorial(){
		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);
		
		Imgproc.blur(mGray, mGray, new Size(3, 3));
		
		Mat canny_output = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		/// Detect edges using canny
		Imgproc.Canny( mGray, canny_output, thresh, thresh*2, 3, false);

		/// Find contours
		Imgproc.findContours( canny_output, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );

		/// Draw contours
		mIntermediateMat = Mat.zeros( canny_output.size(), CvType.CV_8UC3 );
		for(int i = 0; i<contours.size(); i++) {
			Scalar color = new Scalar( getRandomUniformInt(0, 255), getRandomUniformInt(0,255), getRandomUniformInt(0,255) );
			Imgproc.drawContours( mIntermediateMat, contours, i, color, 2, 8, hierarchy, 0, new Point() );
		}
		
		mRgba = mIntermediateMat;
	}
	
	
	private Mat getProcessingROI(Mat src){
		Size size = src.size();

        int rows = (int) size.height;
        int cols = (int) size.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;
        
        
        return src.submat(top, top + height, left, left + width);
	}
	private int getRandomUniformInt(int min, int max) {
		Random r1 = new Random();
        return r1.nextInt() * (max - min) + min;
	}
}