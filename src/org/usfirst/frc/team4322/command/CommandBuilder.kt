package org.usfirst.frc.team4322.command

import java.util.ArrayList
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * Created by nicolasmachado on 4/20/16.
 */
class CommandBuilder {
    private var act : (Command) -> Unit = {}
    private var init : (Command) -> Unit = {}
    private var cond : (Command) -> Boolean = { false }
    internal var subsystems = ArrayList<Subsystem>()
    private var onEnd : (Command) -> Unit = {}
    private var onInt : (Command) -> Unit = {}
    internal var timeout: Long = 0
    fun execute(act: (Command) -> Unit): CommandBuilder {
        this.act = act
        return this
    }

    fun onInit(init: (Command) -> Unit): CommandBuilder {
        this.init = init
        return this
    }

    fun runForTime(millis: Long): CommandBuilder {
        this.cond = { c: Command -> c.runTimeMillis() < millis }
        return this
    }

    fun withTimeout(millis: Long): CommandBuilder {
        this.timeout = millis
        return this
    }

    fun runWhile(cond: (Command) -> Boolean): CommandBuilder {
        this.cond = cond
        return this
    }

    fun onEnd(onEnd: (Command) -> Unit): CommandBuilder {
        this.onEnd = onEnd
        return this
    }

    fun onInterrupt(onInt: (Command) -> Unit): CommandBuilder {
        this.onInt = onInt
        return this
    }

    fun require(s: Subsystem): CommandBuilder {
        subsystems.add(s)
        return this
    }

    fun requires(vararg s: Subsystem): CommandBuilder {
        for (sys in s) {
            subsystems.add(sys)
        }
        return this
    }

    fun build(): Command {
        return object : Command(subsystems, timeout) {

            override val isFinished: Boolean
                get() = !cond(this)

            override fun initialize() {
                init(this)
            }

            override fun end() {
                onEnd(this)
            }

            override fun interrupted() {
                onInt(this)
            }

            override fun execute() {
                act(this)
            }
        }
    }

    companion object {
        fun create(): CommandBuilder {
            return CommandBuilder()
        }
    }
}
