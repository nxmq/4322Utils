package org.usfirst.frc.team4322.vision;

import org.usfirst.frc.team4322.logging.RobotLogger;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_videoio.VideoCapture;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_videoio.*;

/**
 * Created by nicolasmachado on 9/4/16.
 */
public class CVCamera implements Camera
{
	private VideoCapture grab;
	private Mat temp = null;
	public CVCamera()
	{
		grab = new VideoCapture(0);
		grab.set(CV_CAP_PROP_FRAME_WIDTH,1280);
		grab.set(CV_CAP_PROP_FRAME_HEIGHT,720);
		grab.open(0);
		temp = new Mat(grab.get(CV_CAP_PROP_FRAME_WIDTH),grab.get(CV_CAP_PROP_FRAME_HEIGHT),CV_8UC3);
	}
	@Override
	public Mat getFrame()
	{
		grab.grab();
		grab.read(temp);
		return temp;
	}
	public void stop()
	{
		grab.release();
	}
}
