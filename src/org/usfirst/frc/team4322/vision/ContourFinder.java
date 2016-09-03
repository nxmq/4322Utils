package org.usfirst.frc.team4322.vision;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by nicolasmachado on 4/28/16.
 */
public class ContourFinder extends Transform<Frame,MatVector>
{
	@Override
	public MatVector apply(Frame in)
	{
		MatVector out = new MatVector();
		findContours(fconv.convert(in),out,CV_RETR_TREE,CV_CHAIN_APPROX_SIMPLE);
		return out;
	}
}
