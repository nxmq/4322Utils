package org.usfirst.frc.team4322.vision;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by nicolasmachado on 4/28/16.
 */
public class ContourFinder extends Transform<Mat,MatVector>
{
	@Override
	public MatVector apply(Mat in)
	{
		MatVector out = new MatVector();
		Mat hierarchy = new Mat();
		findContours(in,out,hierarchy,CV_RETR_TREE,CV_CHAIN_APPROX_SIMPLE,new Point(0,0));
		return out;
	}
}
