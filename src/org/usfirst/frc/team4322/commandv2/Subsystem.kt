package org.usfirst.frc.team4322.commandv2

import edu.wpi.first.wpilibj.Sendable
import edu.wpi.first.wpilibj.SendableBase
import edu.wpi.first.wpilibj.livewindow.LiveWindow
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder
import kotlinx.coroutines.Deferred
import java.util.concurrent.ConcurrentLinkedDeque


open class Subsystem : SendableBase() {
    val commandStack: ConcurrentLinkedDeque<Deferred<Unit>> = ConcurrentLinkedDeque()
    var defaultCommand: Command? = null
        protected set
    var defaultCommandInitialized = false
    var currentCommandName: String = ""

    init {
        Scheduler.subsystems.add(this)
        this.subsystem = javaClass.simpleName
    }

    fun resetCommandQueue() {
        commandStack.forEach { it.cancel() }
        commandStack.clear()
    }

    open fun initDefaultCommand() {
    }

    open fun periodic() {

    }

    internal fun pump() {
        if (commandStack.isEmpty()) {
            if (defaultCommand == null && !defaultCommandInitialized) {
                initDefaultCommand()
                defaultCommandInitialized = true
            }
            defaultCommand?.start()
        }
    }

    /**
     * Returns the default command title, or empty string is there is none.
     *
     * @return the default command title
     */
    fun getDefaultCommandName(): String {
        val defaultCommand = this.defaultCommand
        return defaultCommand?.javaClass?.name ?: ""
    }

    /**
     * Associate a [Sendable] with this Subsystem.
     * Also update the child's title.
     *
     * @param name title to give child
     * @param child sendable
     */
    fun addChild(name: String, child: Sendable) {
        child.setName(subsystem, name)
        LiveWindow.add(child)
    }

    /**
     * Associate a [Sendable] with this Subsystem.
     *
     * @param child sendable
     */
    fun addChild(child: Sendable) {
        child.subsystem = subsystem
        LiveWindow.add(child)
    }

    override fun toString(): String {
        return subsystem
    }

    override fun initSendable(builder: SendableBuilder) {
        builder.setSmartDashboardType("Subsystem")
        builder.addBooleanProperty(".hasDefault", { defaultCommand != null }, null)
        builder.addStringProperty(".default", { getDefaultCommandName() }, null)
        builder.addBooleanProperty(".hasCommand", { !commandStack.isEmpty() }, null)
        builder.addStringProperty(".command", { currentCommandName }, null)
    }
}