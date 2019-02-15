package org.usfirst.frc.team4322.math

import java.util.concurrent.ConcurrentSkipListMap

class TemporalLerpMap<V : Interpolable<V>> {
    private val list : ConcurrentSkipListMap<Double,V> = ConcurrentSkipListMap()

    operator fun set(timestamp: Double, value : V) {
        list[timestamp] = value
    }

    operator fun get(timestamp: Double) : V {
        return if(list.containsKey(timestamp)) {
            list[timestamp]!!
        }
        else {
            val floor = list.floorEntry(timestamp)
            val ceil = list.ceilingEntry(timestamp)
            val diff = ceil.key - floor.key
            floor.value.lerp(ceil.value,(timestamp-floor.key)/diff)
        }
    }

    fun getLastKey(): Double {
        if (list.isEmpty()) {
            return 0.0
        }
        return list.lastKey()
    }

    fun clear() {
        list.clear()
    }
}