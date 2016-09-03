package org.usfirst.frc.team4322.vision;

import static org.bytedeco.javacpp.opencv_imgproc.*;

import static org.bytedeco.javacpp.opencv_core.*;
/**
 * Created by nicolasmachado on 6/19/16.
 */
public class ContourAreaFilter extends Transform<MatVector,MatVector>
{
	private double min = -1,max = -1;

	public ContourAreaFilter(double min, double max)
	{
		this.min = min;
		this.max = max;
	}



	@Override
	public MatVector apply(MatVector in)
	{
		MatVector out = new MatVector();
		for(int i = 0; i < in.size(); i++)
		{
			Mat c = in.get(i);
			double sz = contourArea(c);
			if((min < 0 || sz >= min) && (max < 0 || sz <= max))
			{
				out.put(c);
			}
		}
		return out;
	}
}
