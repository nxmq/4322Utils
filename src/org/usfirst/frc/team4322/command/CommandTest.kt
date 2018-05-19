package org.usfirst.frc.team4322.command

import java.util.*

object CommandTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = CommandBuilder.create().execute { _ -> println("A!") }.runForTime(1000).onEnd { _ -> println("A ending!") }.build()
        val b = CommandBuilder.create().execute { _ -> println("B!") }.runForTime(1500).onEnd { _ -> println("B ending!") }.build()
        val c = CommandBuilder.create().execute { _ -> println("C!") }.runForTime( 500).onEnd { _ -> println("C ending!") }.build()
        val d = CommandBuilder.create().execute { _ -> println("D!") }.runForTime(2000).onEnd { _ -> println("D ending!") }.build()
        val e = CommandBuilder.create().execute { _ -> println("E!") }.runForTime( 750).onEnd { _ -> println("E ending!") }.build()
        val f = CommandBuilder.create().execute { _ -> println("F!") }.runForTime( 250).onEnd { _ -> println("F ending!") }.build()
        val g = CommandBuilder.create().execute { _ -> println("G!") }.runForTime( 250).onEnd { _ -> println("G ending!") }.build()
        val cg = group {
            parallel {
                +a
                +b
                +c
            }
            sequential {
                +d
                +e
                +Router {
                    if (Calendar.getInstance().get(Calendar.MINUTE) % 2 == 0) {
                        f
                    } else {
                        g
                    }
                }
            }
        }.synthesize()
        cg.start()

        System.out.println("Finished!")
        Scheduler.shutdown()
    }
}
