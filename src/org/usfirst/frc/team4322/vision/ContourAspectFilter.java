package org.usfirst.frc.team4322.vision;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by nicolasmachado on 6/19/16.
 */
public class ContourAspectFilter extends Transform<MatVector,MatVector>
{
	private double target = 0;
	private double error = .1;

	public ContourAspectFilter(double target)
	{
		this.target = target;
	}

	public ContourAspectFilter(double error, double target)
	{
		this.error = error;
		this.target = target;
	}
	@Override
	public MatVector apply(MatVector in)
	{
		MatVector out = new MatVector();
		for(int i = 0; i < in.size(); i++)
		{
			Mat c = in.get(i);
			Rect rc =  boundingRect(c);
			double aspect = (rc.width() > rc.height()) ? rc.width()/rc.height() : rc.height()/rc.width();
			if(Math.abs(target-aspect) <= error)
			{
				out.put(c);
			}
		}
		return out;
	}
}
