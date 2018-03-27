package org.usfirst.frc.team4322.unitsuppliers;

import tec.uom.se.ComparableQuantity;
import javax.measure.quantity.Length;

@FunctionalInterface
public interface DistanceSupplier
{
    ComparableQuantity<Length> getDistance();
}
