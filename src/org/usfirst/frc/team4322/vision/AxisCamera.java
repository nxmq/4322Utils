package org.usfirst.frc.team4322.vision;

import java.io.IOException;
import java.util.HashMap;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB24;

/**
 * Created by nicolasmachado on 4/28/16.
 */
public class AxisCamera extends Camera
{
	private FFmpegFrameGrabber src;
	public enum AxisSetting
	{
		RESOLUTION,
		COMPRESSION,
		MIRROR,
		ROTATION,
		FPS,
		VIDEOBITRATE,
		KEYFRAME_INTERVAL
	}
	private String accessURLBase = "rtsp://axis-camera.local:554/axis-media/media.amp?videocodec=h264";
	private HashMap<AxisSetting,String> optHash = new HashMap<>();

	public AxisCamera()
	{
		optHash.put(AxisSetting.RESOLUTION,"640x480");
		optHash.put(AxisSetting.COMPRESSION,"85");
		optHash.put(AxisSetting.MIRROR,"0");
		optHash.put(AxisSetting.ROTATION,"0");
		optHash.put(AxisSetting.FPS,"15");
		optHash.put(AxisSetting.VIDEOBITRATE,"0");
		optHash.put(AxisSetting.KEYFRAME_INTERVAL,"32");
	}
	public void start() throws IOException
	{
		src = new FFmpegFrameGrabber(getURL());
		try
		{
			src.setPixelFormat(AV_PIX_FMT_RGB24);
			src.stop();
		}
		catch(FrameGrabber.Exception e)
		{
			throw new IOException(e);
		}
	}
	public void stop() throws IOException
	{
		try
		{
			src.stop();
		}
		catch(FrameGrabber.Exception e)
		{
			throw new IOException(e);
		}
	}
	public void setOption(AxisSetting s, String v)
	{
		optHash.put(s,v);
	}
	public String getURL()
	{
		String accessURL = accessURLBase;
		for(HashMap.Entry<AxisSetting,String> e : optHash.entrySet())
		{
			accessURL += ("?" + e.getKey().name().toLowerCase() + "="+ e.getValue());
		}
		return accessURL;
	}
	@Override
	public Frame getFrame()
	{
		try
		{
			Frame fr =  src.grabFrame(false,true,true,true);
		}
		catch(FrameGrabber.Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
