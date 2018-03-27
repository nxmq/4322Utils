package org.usfirst.frc.team4322.unitsuppliers;

import tec.uom.se.ComparableQuantity;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

@FunctionalInterface
public interface AngleSupplier
{
    ComparableQuantity<Angle> getAngle();
}
