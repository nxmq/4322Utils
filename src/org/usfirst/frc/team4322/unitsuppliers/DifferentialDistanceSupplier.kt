package org.usfirst.frc.team4322.unitsuppliers

import tec.uom.se.ComparableQuantity
import javax.measure.quantity.Length

interface DifferentialDistanceSupplier {
    fun getLeft() : ComparableQuantity<Length>
    fun getRight() : ComparableQuantity<Length>
}