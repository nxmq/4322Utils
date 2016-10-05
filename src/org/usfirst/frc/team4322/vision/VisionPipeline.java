package org.usfirst.frc.team4322.vision;

import org.usfirst.frc.team4322.logging.RobotLogger;

import static org.bytedeco.javacpp.opencv_core.*;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Created by nicolasmachado on 4/21/16.
 */
public class VisionPipeline
{
	Camera cam = null;
	private ArrayList<Transform<Mat,Mat>> frameOps = new ArrayList<>();
	private ArrayList<Transform<MatVector,MatVector>> matOps= new ArrayList<>();
	private Transform<Mat,MatVector> frameToMat;
	public VisionPipeline(Camera c)
	{
		cam = c;
	}
	public void addFrameTransform(Transform<Mat,Mat> t)
	{
		frameOps.add(t);
	}
	public void addContourTransform(Transform<MatVector,MatVector> t)
	{
		matOps.add(t);
	}
	public void setVectorizer(Transform<Mat,MatVector> t) { frameToMat = t; }

	public MatVector run()
	{
		Mat temp = cam.getFrame();
		MatVector tempVec;
		for(Transform<Mat,Mat> t : frameOps)
		{
			temp = t.apply(temp);
			RobotLogger.getInstance().debug("VisionPipeline: A Frame Transform has finished!");

		}
		tempVec = frameToMat.apply(temp);
		RobotLogger.getInstance().debug("VisionPipeline: Frame converted to Contour!");
		for(Transform<MatVector,MatVector> t : matOps)
		{
			tempVec = t.apply(tempVec);
			RobotLogger.getInstance().debug("VisionPipeline: A Contour Transform has finished!");

		}
		return tempVec;
	}
}
