package org.usfirst.frc.team4322.vision;

/**
 * Created by nicolasmachado on 9/4/16.
 */
public class CVCamera implements Camera
{
	private OpenCVFrameGrabber grab;
	public CVCamera()
	{
		grab = new OpenCVFrameGrabber(0);
		try
		{
			grab.start();
		}
		catch(FrameGrabber.Exception e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public Frame getFrame()
	{
		try
		{
			return grab.grab();
		}
		catch(FrameGrabber.Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	public void stop()
	{
		try
		{
			grab.stop();
		}
		catch(FrameGrabber.Exception e)
		{
			e.printStackTrace();
		}
	}
}
