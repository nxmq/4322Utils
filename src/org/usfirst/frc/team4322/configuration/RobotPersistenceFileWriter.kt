package org.usfirst.frc.team4322.configuration

import java.io.IOException
import java.io.PrintWriter
import java.util.HashMap

/**
 * Created by teresamachado on 12/27/16.
 */
object RobotPersistenceFileWriter {
    val CONFIG_FILE = "/home/lvuser/persistence.ini"
    private val values: HashMap<String, String>? = null
    operator fun set(key: String, value: String) {
        values!!.put(key, value)
    }

    fun write() {
        try {
            PrintWriter(CONFIG_FILE).use { out -> values!!.forEach { k, v -> out.printf("%s=%s\n", k, v) } }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
