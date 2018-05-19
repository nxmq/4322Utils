package org.usfirst.frc.team4322.commandv2

import kotlinx.coroutines.experimental.Deferred
import java.util.*


class Subsystem {
    val commandStack: ArrayDeque<Deferred<Unit>> = ArrayDeque()
}