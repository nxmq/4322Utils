package org.usfirst.frc.team4322.unitsuppliers;

import tec.uom.se.ComparableQuantity;

import javax.measure.quantity.Angle;

@FunctionalInterface
public interface AngleSupplier
{
    ComparableQuantity<Angle> getAngle();
}
