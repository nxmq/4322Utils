package org.usfirst.frc.team4322.command

interface Element {
    fun synthesize(parent : CommandGroup  = CommandGroup()) : Command
}

class Group : CommandSet()

@DslMarker
annotation class CommandMarker

@CommandMarker
abstract class CommandSet : Element {
    private val children = arrayListOf<Element>()

    fun parallel(init: Parallel.() -> Unit) = initTag(Parallel(), init)
    fun sequential(init: Sequential.() -> Unit) = initTag(Sequential(), init)

    private fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun synthesize(parent : CommandGroup) : Command {
        for(child in children)
        {
            child.synthesize(parent)
        }
        return parent
    }
}

abstract class SubSet : CommandSet() {
    val commands = arrayListOf<Command>()
    operator fun Command.unaryPlus() {
        commands.add(this)
    }
}
class Parallel : SubSet() {
    override fun synthesize(parent: CommandGroup): Command {
        val group = CommandGroup()
        for(command in commands)
            group.addParallel(command)
        parent.addSequential(group)
        return parent
    }
}
class Sequential : SubSet() {
    override fun synthesize(parent: CommandGroup): Command {
        val group = CommandGroup()
        for(command in commands)
            group.addSequential(command)
        parent.addSequential(group)
        return parent
    }
}

fun group(init: Group.() -> Unit): Group {
    val set = Group()
    set.init()
    return set
}

/* Example:

val foo = group {
    parallel {
        +CommandBuilder.create().build()
        +CommandBuilder.create().build()
        sequential {
            +CommandBuilder.create().build()
            +CommandBuilder.create().build()
        }
    }
    sequential {
        +CommandBuilder.create().build()
        +CommandBuilder.create().build()
    }
}

remember to call .synthesize() on the group to turn the DSL into CommandGroup instances.
*/