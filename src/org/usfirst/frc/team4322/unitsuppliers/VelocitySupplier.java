package org.usfirst.frc.team4322.unitsuppliers;

import tec.uom.se.ComparableQuantity;

import javax.measure.quantity.Speed;

@FunctionalInterface
public interface VelocitySupplier
{
    ComparableQuantity<Speed> getVelocity();
}
