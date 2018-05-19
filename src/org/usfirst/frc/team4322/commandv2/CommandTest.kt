package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking


class TestCommand(val name: String, val maxCounts: Long, subsystem: Subsystem) : Command(0) {
    var counter = 0

    init {
        this.require(subsystem)
    }

    override fun initialize() {
        println("$name Starting!")
    }

    override fun end() {
        println("$name Ending!")
    }

    override fun interrupted() {
        println("$name Suspended!")
    }

    override fun resumed() {
        println("$name Resumed!")

    }

    override fun execute() {
        println("$name Running!\n counter:$counter")
        counter++
    }

    override fun isFinished(): Boolean {
        return counter > maxCounts
    }
}


fun main(args: Array<String>) {

    val s1 = Subsystem()
    val s2 = Subsystem()
    val foo = async(start = CoroutineStart.LAZY) {
        async {
            val a1 = async { TestCommand("inst1", 10, s1)().await() }
            val a2 = async { TestCommand("inst2", 25, s2)().await() }
            val a3 = async {
                TestCommand("inst3", 10, s1)().await()
                TestCommand("inst4", 10, s2)().await()
            }
            a1.await()
            a2.await()
            a3.await()

        }.await()
        TestCommand("inst5", 10, s1)().await()
        TestCommand("inst6", 10, s2)().await()
    }

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
    runBlocking {
        bar().await()
    }
}