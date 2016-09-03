package org.usfirst.frc.team4322.vision;

import static org.bytedeco.javacpp.opencv_core.*;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Created by nicolasmachado on 4/21/16.
 */
public class VisionPipeline
{
	Camera cam = null;
	private ArrayList<Transform<Frame,Frame>> frameOps;
	private ArrayList<Transform<MatVector,MatVector>> matOps;
	private Transform<Frame,MatVector> frameToMat;
	public static void initVision()
	{
	}
	public VisionPipeline(Camera c)
	{
		cam = c;
	}
	public void addFrameTransform(Transform<Frame,Frame> t)
	{
		frameOps.add(t);
	}
	public void addContourTransform(Transform<MatVector,MatVector> t)
	{
		matOps.add(t);
	}
	public void setVectorizer(Transform<Frame,MatVector> t) { frameToMat = t; }

	public MatVector run()
	{
		Frame temp = cam.getFrame();
		MatVector tempVec;
		for(Transform<Frame,Frame> t : frameOps)
		{
			temp = t.apply(temp);
		}
		tempVec = frameToMat.apply(temp);
		for(Transform<MatVector,MatVector> t : matOps)
		{
			tempVec = t.apply(tempVec);
		}
		return tempVec;
	}
}
