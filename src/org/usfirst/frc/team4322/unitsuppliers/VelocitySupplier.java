package org.usfirst.frc.team4322.unitsuppliers;

import tec.uom.se.ComparableQuantity;

@FunctionalInterface
public interface VelocitySupplier
{
    ComparableQuantity<?> getVelocity();
}
