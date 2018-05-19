package org.usfirst.frc.team4322.command

import java.util.*

/**
 * Created by nicolasmachado on 4/20/16.
 */
class CommandGroup : Command() {
    private val queue = ArrayDeque<Task>()

    public override val isFinished: Boolean
        get() = synchronized(queue) { queue.isEmpty() }

    inner class Task : Command() {
        internal var parallel: Boolean = false
        private val inputQueue = ArrayDeque<Any>()
        private val toDo = ArrayDeque<Command>()

        override val isFinished: Boolean
            get() = synchronized(toDo) { toDo.isEmpty() }

        fun add(c: Command) {
            inputQueue.add(c)
        }
        fun add(r: Router) {
            inputQueue.add(r)
        }

        fun add(c: CommandSet) {
            inputQueue.add(c.synthesize())
        }

        override fun initialize() {
            toDo.clear()
            inputQueue.forEach {
                when (it) {
                    is Command -> toDo.add(it)
                    is Router -> toDo.add(it.route())
                }
            }
            toDo.forEach { it.start() }
        }

        override fun execute() {
            toDo.forEach {cmd ->
                 if(cmd.isDone)
                 {
                     cmd.cancel()
                     toDo.remove(cmd)
                 }
            }
        }

        override fun end() {
        }

        override fun interrupted() {

        }
    }

    init {
        setTimeout(-1)
    }

    fun addSequential(r: Router) {
        val t = Task()
        t.parallel = false
        t.add(r)
        queue.add(t)
    }

    fun addSequential(c: Command) {
        val t = Task()
        t.parallel = false
        t.add(c)
        queue.add(t)
    }

    fun addParallel(r: Router) {
        if (queue.isEmpty() || !queue.last.parallel) {
            val t = Task()
            t.parallel = true
            t.add(r)
            queue.add(t)
        } else {
            queue.last.add(r)
        }
    }

    fun addParallel(c: Command) {
        if (queue.isEmpty() || !queue.last.parallel) {
            val t = Task()
            t.parallel = true
            t.add(c)
            queue.add(t)
        } else {
            queue.last.add(c)
        }
    }

    override fun execute() {
        if (queue.all {c -> c.isDone }) {
            cancel()
        } else {
            if (!queue.first.isStarted) {
                queue.first.start()
            } else {
                if (!queue.first.shouldRun())
                {
                    queue.remove()
                }
            }
        }
    }

    override fun initialize() {

    }

    override fun end() {
        println("CommandDSL ending!")
    }

    override fun interrupted() {

    }
}
