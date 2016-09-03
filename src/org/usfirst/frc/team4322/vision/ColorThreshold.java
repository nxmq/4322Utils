package org.usfirst.frc.team4322.vision;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by nicolasmachado on 4/28/16.
 */
public class ColorThreshold extends Transform<Frame,Frame>
{
	Mat threshLow = new Mat(1,3,CV_8U);
	Mat threshHigh = new Mat(1,3,CV_8U);
	public enum ThresholdFormat
	{
		RGB,
		HSV,
	}
	private ThresholdFormat thresh;

	public ColorThreshold(int x1, int x2, int y1, int y2, int z1, int z2, ThresholdFormat thresh)
	{
		threshLow.arrayData().asBuffer().putInt(0,x1);
		threshHigh.arrayData().asBuffer().putInt(0,x2);
		threshLow.arrayData().asBuffer().putInt(1,y1);
		threshHigh.arrayData().asBuffer().putInt(1,y2);
		threshLow.arrayData().asBuffer().putInt(2,z1);
		threshHigh.arrayData().asBuffer().putInt(2,z2);
		this.thresh = thresh;
	}

	@Override
	public Frame apply(Frame in)
	{
		Mat inMat = fconv.convert(in);
		Mat out = new Mat(inMat.rows(),inMat.cols(), CV_8U);
		switch(thresh)
		{
			case RGB:
				inRange(inMat,threshLow,threshHigh,out);
				break;
			case HSV:
				Mat hsv = new Mat(inMat.rows(),inMat.cols(), inMat.type());
				cvtColor(inMat,hsv,COLOR_RGB2HSV);
				inRange(inMat,threshLow,threshHigh,out);
				break;
		}
		return fconv.convert(out);
	}
}
