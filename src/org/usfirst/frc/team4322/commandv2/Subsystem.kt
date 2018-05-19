package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.Deferred
import java.util.concurrent.ConcurrentLinkedDeque


class Subsystem {
    val commandStack: ConcurrentLinkedDeque<Deferred<Unit>> = ConcurrentLinkedDeque()
}