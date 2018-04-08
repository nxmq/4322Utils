package org.usfirst.frc.team4322.math


class Transform(val rotation : Rotation, val translation: Translation) : Interpolable<Transform> {

    constructor() : this(Rotation(), Translation())

    constructor(other : Transform) : this(other.rotation,other.translation)

    constructor(translation: Translation) : this(Rotation(),translation)

    constructor(rotation: Rotation) : this(rotation,Translation())

    companion object {
        fun fromArc(delta : Arc) : Transform {
            val sin = Math.sin(delta.dθ)
            val cos = Math.cos(delta.dθ)
            val s: Double
            val c: Double
            if (Math.abs(delta.dθ) < 1E-9) {
                s = 1.0 - 1.0 / 6.0 * delta.dθ * delta.dθ
                c = .5 * delta.dθ
            } else {
                s = sin / delta.dθ
                c = (1.0 - cos) / delta.dθ
            }
            return Transform(Rotation(cos, sin, false), Translation(delta.dx * s - delta.dy * c, delta.dx * c + delta.dy * s))
        }
        private fun intersectionInternal(a: Transform, b: Transform): Translation {
            val tanB = b.rotation.tan()
            val t = ((a.translation.x - b.translation.x) * tanB + b.translation.y - a.translation.y) / (a.rotation.sin - a.rotation.cos * tanB)
            return a.translation+a.rotation.toTranslation().scale(t)
        }
    }

    fun toArc() : Arc {
        val dtheta = rotation.radians()
        val halfDTheta = 0.5 * dtheta
        val cosMinusOne = rotation.cos - 1.0
        val halfThetaByTanOfHalfDTheta: Double
        halfThetaByTanOfHalfDTheta = if (Math.abs(cosMinusOne) < 1E-9) {
            1.0 - 1.0 / 12.0 * dtheta * dtheta
        } else {
            -(halfDTheta * rotation.sin) / cosMinusOne
        }
        val translationPart = translation.rotateBy(Rotation(halfThetaByTanOfHalfDTheta, -halfDTheta, false))
        return Arc(translationPart.x, translationPart.y, dtheta)
    }

    fun transformBy(other: Transform) : Transform {
        return Transform(rotation.rotateBy(other.rotation),translation+other.translation.rotateBy(rotation))
    }

    fun inverse() : Transform {
        return Transform(rotation.inverse(),translation.inverse().rotateBy(rotation.inverse()))
    }

    fun normal() : Transform {
        return Transform(rotation.normal(),translation)
    }

    fun intersection(other: Transform) : Translation {
        if(other.rotation.isParallel(rotation))
        {
            return Translation(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        }
        return if(Math.abs(rotation.cos) < Math.abs(rotation.cos))
        {
            intersectionInternal(this, other)
        }
        else
        {
            intersectionInternal(other,this)
        }
    }

    fun isColinear(other: Transform): Boolean {
        val twist = inverse().transformBy(other).toArc()
        return Math.abs(twist.dy) < 1E-9 && Math.abs(twist.dθ) < 1E-9
    }

    override fun lerp(other: Transform, x: Double): Transform {
        if (x <= 0) {
            return Transform(this)
        } else if (x >= 1) {
            return Transform(other)
        }
        val twist = inverse().transformBy(other).toArc()
        return transformBy(Transform.fromArc(twist.scale(x)))
    }
}