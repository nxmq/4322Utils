package org.usfirst.frc.team4322.commandv2


class TestCommand(val title: String, val maxCounts: Long, subsystem: Subsystem) : Command() {
    var counter = 0

    init {
        require(subsystem)
        interruptBehavior = InterruptBehavior.Suspend
    }

    override fun initialize() {
        println("$title Starting!")
    }

    override fun end() {
        println("$title Ending!")
    }

    override fun interrupted() {
        println("$title Suspended!")
    }

    override fun resumed() {
        println("$title Resumed!")

    }

    override fun execute() {
        println("$title Running!\n counter:$counter")
        counter++
    }

    override fun isFinished(): Boolean {
        return counter > maxCounts
    }
}


suspend fun main(args: Array<String>) {

    val s1 = Subsystem()
    val s2 = Subsystem()

    val bar = group {
        parallel {
            +TestCommand("inst1", 10, s1)
            +TestCommand("inst2", 25, s2)
            sequential {
                +TestCommand("inst3", 10, s1)
                +TestCommand("inst4", 10, s2)
            }
        }
        sequential {
            +TestCommand("inst5", 10, s1)
            +TestCommand("inst6", 10, s2)
        }
    }
    bar().await()
}