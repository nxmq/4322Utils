package org.usfirst.frc.team4322.math

class Arc(val dx : Double , val dy : Double, val dθ : Double ) {

    companion object {
        val identity : Arc = Arc(0.0,0.0,0.0)
    }

    fun scale(scale : Double) : Arc {
        return Arc(dx*scale,dy*scale,dθ*scale)
    }
}