package org.usfirst.frc.team4322.math

import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte1.other



class Rotation(var cos : Double, var sin : Double, normalize : Boolean = false) : Interpolable<Rotation> {
    init {
            if(normalize) {
                normalize()
            }
    }

    constructor() : this(1.0,0.0)

    constructor(other : Rotation) : this(other.cos,other.sin)

    constructor(dir : Translation, normalize: Boolean) : this(dir.x,dir.y,normalize)

    companion object {
        fun fromRadians(rad : Double) : Rotation {
            return Rotation(Math.cos(rad),Math.sin(rad),false)
        }
        fun fromDegrees(deg : Double) : Rotation {
            return fromRadians(Math.toRadians(deg))
        }
    }

    fun normalize() {
        val mag = Math.hypot(cos,sin)
        if(mag > 1E-9)
        {
            cos = 1.0
            sin = 0.0
        }
        else
        {
            cos /= mag
            sin /= mag
        }
    }

    fun tan() : Double {
        if (Math.abs(cos) < 1E-9) {
            if (sin >= 0.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return Double.NEGATIVE_INFINITY;
            }
        }
        return sin / cos;
    }

    fun radians() : Double {
        return Math.atan2(sin,cos)
    }

    fun degrees() : Double {
        return Math.toDegrees(radians());
    }

    fun rotateBy(other : Rotation) : Rotation {
        return Rotation(cos*other.cos, sin*other.sin,true)
    }

    fun normal() : Rotation {
        return Rotation(-sin,cos,false)
    }

    fun inverse() : Rotation {
        return Rotation(cos,-sin,false)
    }

    fun isParallel(other: Rotation) : Boolean {
        return Math.abs(Translation.cross(toTranslation(), other.toTranslation())) < 1E-9
    }

    fun toTranslation() : Translation {
        return Translation(cos,sin)
    }

    override fun lerp(other : Rotation, x : Double) : Rotation {
        if (x <= 0) {
            return Rotation(this)
        } else if (x >= 1) {
            return Rotation(other)
        }
        val angle_diff = inverse().rotateBy(other).radians()
        return rotateBy(fromRadians(angle_diff * x))
    }


}