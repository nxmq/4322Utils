package org.usfirst.frc.team4322.motion

import tec.uom.se.ComparableQuantity
import javax.measure.quantity.Length

data class Waypoint(var x : ComparableQuantity<Length>, var y : ComparableQuantity<Length>, var vel : ComparableQuantity<*>)