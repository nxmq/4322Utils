package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking

val s1 = Subsystem()

class TestCommand(val name: String, val timeout: Long) : Command(0) {
    var counter = 0

    init {
        this.require(s1)
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
        return counter > timeout
    }
}


fun main(args: Array<String>) {
    runBlocking {
        async {
            var t2 = TestCommand("instance 2", 10)()
            delay(10)
            var t1 = TestCommand("instance 1", 10)()
            t1.await()
            t2.await()
        }.await()
    }
}