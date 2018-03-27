package org.usfirst.frc.team4322.command

abstract class Trigger {
    private val prevState: Boolean = false
    private var onPress: Any? = null
    private lateinit var pressCmd: Command
    private var onRelease: Any? = null
    private lateinit var releaseCmd: Command
    private var duringHold: Any? = null
    private lateinit var holdCmd: Command
    private var holdStarted : Boolean = false

    abstract fun get() : Boolean

    fun whenPressed(c: Command) {
        onPress = c
    }

    fun whenPressed(r: Router) {
        onPress = r
    }

    fun whileHeld(c: Command) {
        duringHold = c
    }

    fun whileHeld(r: Router) {
        duringHold = r
    }

    fun whenReleased(c: Command) {
        onRelease = c
    }

    fun whenReleased(r: Router) {
        onRelease = r
    }

    fun poll() {
        if(get() && prevState)
        {
            if(!holdStarted && duringHold != null)
            {
                holdStarted = true
                val dh = duringHold
                when(dh) {
                    is Command -> holdCmd = dh
                    is Router -> holdCmd = dh.route()
                }
                holdCmd.start()
            }
            else
            {
                if(!holdCmd.isStarted)
                {
                    holdCmd.start()
                }
            }

        }
        else if(get() && !prevState)
        {
            if(onPress != null) {
                val pr = onPress
                when (pr) {
                    is Command -> pressCmd = pr
                    is Router -> pressCmd = pr.route()
                }
                pressCmd.start()
            }
        }
        else if(!get() && prevState)
        {
            if(duringHold != null) {
                holdStarted = false
                holdCmd.cancel()
            }
            if(onRelease != null) {
                val rl = onRelease
                when (rl) {
                    is Command -> releaseCmd = rl
                    is Router -> releaseCmd = rl.route()
                }
                releaseCmd.start()
            }
        }
    }
}