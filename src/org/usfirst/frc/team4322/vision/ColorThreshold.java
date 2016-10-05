package org.usfirst.frc.team4322.vision;

import org.bytedeco.javacpp.Pointer;
import org.usfirst.frc.team4322.logging.RobotLogger;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by nicolasmachado on 4/28/16.
 */
public class ColorThreshold extends Transform<Mat,Mat>
{
	Scalar threshLow;
	Scalar threshHigh;
	public enum ThresholdFormat
	{
		RGB,
		HSV,
	}
	private ThresholdFormat thresh;

	public ColorThreshold(int x1, int x2, int y1, int y2, int z1, int z2, ThresholdFormat thresh)
	{
		threshLow = new Scalar(x1,y1,z1,0);
		threshHigh = new Scalar(x2,y2,z2,0);
		this.thresh = thresh;
	}

	@Override
	public Mat apply(Mat inMat)
	{
		Mat tmp = new Mat(inMat.rows(),inMat.cols(), CV_8UC3);
		Mat bin = new Mat(inMat.rows(),inMat.cols(), CV_8UC1);
		Mat out = new Mat(inMat.rows(),inMat.cols(), CV_8UC1);
		switch(thresh)
		{
			case RGB:
				inRange(tmp, new Mat(inMat.size(),inMat.type(),threshLow),new Mat(inMat.size(),inMat.type(),threshHigh),inMat);
				threshold(bin,out,254,255,THRESH_BINARY_INV);
				break;
			case HSV:
				Mat hsv = new Mat(inMat.rows(),inMat.cols(), inMat.type());
				cvtColor(inMat,hsv,COLOR_RGB2HSV);
				inRange(hsv,new Mat(inMat.size(),inMat.type(),threshLow),new Mat(inMat.size(),inMat.type(),threshHigh),tmp);
				threshold(bin,out,254,255,THRESH_BINARY_INV);
				break;
		}
		return out;
	}
}
