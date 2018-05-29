package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.Deferred
import java.util.concurrent.ConcurrentLinkedDeque


open class Subsystem {
    val commandStack: ConcurrentLinkedDeque<Deferred<Unit>> = ConcurrentLinkedDeque()

    fun resetCommandQueue() {
        commandStack.forEach { it.cancel() }
        commandStack.clear()
    }
}