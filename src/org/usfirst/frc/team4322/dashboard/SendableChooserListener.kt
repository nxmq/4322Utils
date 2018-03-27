package org.usfirst.frc.team4322.dashboard

import edu.wpi.first.networktables.*
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import org.usfirst.frc.team4322.logging.RobotLogger

import java.lang.reflect.Field

/**
 * Created by nicolasmachado on 9/3/16.
 */
class SendableChooserListener(private val backingChooser: SendableChooser<*>, private val backingField: Field, name: String) : TableEntryListener {

    init {
        NetworkTableInstance.getDefault().getTable("SmartDashboard").getSubTable(name).addEntryListener("selected", this, TableEntryListener.kUpdate)
        RobotLogger.debug("Field with name %s has been bound to a SendableChooserListener", backingField.name)
    }


    override fun valueChanged(source: NetworkTable, key: String, entry: NetworkTableEntry, value: NetworkTableValue, flags: Int) {
        try {
            backingField.set(null, backingChooser.selected)
            RobotLogger.debug("Set field %s to %s.\n", backingField.name, backingChooser.selected.toString())
        } catch (e: IllegalAccessException) {
            RobotLogger.exc("Exception in SendableChooserListener:", e)
        }

    }
}
