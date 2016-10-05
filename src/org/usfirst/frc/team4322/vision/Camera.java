package org.usfirst.frc.team4322.vision;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by nicolasmachado on 4/22/16.
 */
public interface Camera
{
	opencv_core.Mat getFrame();
}
