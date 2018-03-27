package org.usfirst.frc.team4322.unitsuppliers

import tec.uom.se.ComparableQuantity

interface DifferentialVelocitySupplier {
    fun getLeft() : ComparableQuantity<*>
    fun getRight() : ComparableQuantity<*>
}