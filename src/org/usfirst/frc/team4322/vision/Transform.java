package org.usfirst.frc.team4322.vision;

/**
 * Created by nicolasmachado on 4/28/16.
 */
public abstract class Transform<T,R>
{
	protected OpenCVFrameConverter.ToMat fconv = new OpenCVFrameConverter.ToMat();
	public abstract R apply(T in);
}
