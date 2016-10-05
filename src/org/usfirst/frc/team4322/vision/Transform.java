package org.usfirst.frc.team4322.vision;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by nicolasmachado on 4/28/16.
 */
public abstract class Transform<T,R>
{
	protected OpenCVFrameConverter<opencv_core.Mat> fconv = new OpenCVFrameConverter.ToMat();
	public abstract R apply(T in);
}
