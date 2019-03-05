package org.usfirst.frc.team4322.dashboard

import edu.wpi.first.networktables.*
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4322.configuration.RobotConfigFileReader.primitiveMap
import org.usfirst.frc.team4322.configuration.RobotPersistenceFileWriter
import org.usfirst.frc.team4322.logging.RobotLogger
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Created by nicolasmachado on 3/30/16.
 */

object MapSynchronizer {

    private val valMap = HashMap<String, FieldInfo>()

    private class FieldInfo(internal var persistent: Boolean, internal var field: Field)

    private class KeyListener(private val store: Field, private val persistent: Boolean) : TableEntryListener {
        private val type: Class<*> = store.type

        override fun valueChanged(table: NetworkTable, key: String, entry: NetworkTableEntry, value: NetworkTableValue, flags: Int) {
            try {

                if (type.isPrimitive) {
                    RobotLogger.debug("Reflection field is a primitive!")
                    when (type) {
                        Int::class.javaPrimitiveType -> store.setInt(null, value.double.toInt())
                        Byte::class.javaPrimitiveType -> store.setByte(null, value.double.toByte())
                        Boolean::class.javaPrimitiveType -> store.set(null, value.boolean)
                        Short::class.javaPrimitiveType -> store.setShort(null, value.double.toShort())
                        Long::class.javaPrimitiveType -> store.setLong(null, value.double.toLong())
                        Float::class.javaPrimitiveType -> store.setFloat(null, value.double.toFloat())
                        Double::class.javaPrimitiveType -> store.setDouble(null, value.double)
                    }
                } else {
                    store.set(null, value)
                }
                RobotLogger.info("Setting field \"%s\" to \"%s\".", key, value.toString())
                if (persistent) {
                    if (type.isArray) {
                        RobotPersistenceFileWriter[key] = Arrays.toString(store.get(null)!! as Array<*>).replace('[', '{').replace(']', '}')
                    } else {
                        RobotPersistenceFileWriter[key] = store.get(null).toString()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun loadPersistentValues() {
        try {
            FileInputStream(RobotPersistenceFileWriter.CONFIG_FILE).use { fstream ->
                val p = Properties()
                p.load(fstream)
                p.forEach { k, v ->
                    if (valMap.containsKey(k)) {
                        val current = valMap[k]!!.field
                        try {
                            current.set(null, if (current.type == String::class.java) v else primitiveMap[current.type]?.invoke(null, v))
                        } catch (e: IllegalAccessException) {
                            e.printStackTrace()
                        } catch (e: InvocationTargetException) {
                            e.printStackTrace()
                        }

                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun link(robotMap: Class<*>) {
        for (f in robotMap.fields) {
            val tag: DashboardInputField
            if (f.isAnnotationPresent(DashboardInputField::class.java)) {
                tag = f.getAnnotation<DashboardInputField>(DashboardInputField::class.java)
            } else {
                continue
            }
            val type = f.type
            if (type.isArray) {
                RobotLogger.err("Arrays are not supported by MapSynchronizer at this time. The field %s will not be synchronized with the SmartDashboard.", f.name)
            }
            try {
                if (type == Double::class.javaPrimitiveType || type == Float::class.javaPrimitiveType) {
                    SmartDashboard.putNumber(tag.field, f.getDouble(null))
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(tag.field, KeyListener(f, tag.persistent), TableEntryListener.kUpdate)
                } else if (type == Long::class.javaPrimitiveType || type == Int::class.javaPrimitiveType || type == Short::class.javaPrimitiveType || type == Byte::class.javaPrimitiveType) {
                    SmartDashboard.putNumber(tag.field, f.getLong(null).toDouble())
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(tag.field, KeyListener(f, tag.persistent), TableEntryListener.kUpdate)
                } else if (type == Boolean::class.javaPrimitiveType) {
                    SmartDashboard.putBoolean(tag.field, f.getBoolean(null))
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(tag.field, KeyListener(f, tag.persistent), TableEntryListener.kUpdate)
                } else if (type == String::class.java) {
                    SmartDashboard.putString(tag.field, f.get(null) as String)
                    NetworkTableInstance.getDefault().getTable("SmartDashboard").addEntryListener(tag.field, KeyListener(f, tag.persistent), TableEntryListener.kUpdate)

                } else if (type.isEnum) {
                    if (tag.persistent) {
                        RobotLogger.err("SendableChoosers cannot be persistent. Sorry.")
                        continue
                    }
                    val enumChooser = SendableChooser<Any>()
                    for (i in 0 until type.enumConstants.size) {
                        enumChooser.addOption(type.enumConstants[i].toString(), type.enumConstants[i])
                    }
                    SmartDashboard.putData(tag.field, enumChooser)
                    SendableChooserListener(enumChooser, f, tag.field)
                } else {
                    RobotLogger.err("The type of field %s is unsupported by MapUtils at this time. This will not be synchronized with the SmartDashboard.", f.name)
                }

            } catch (ex: IllegalAccessException) {
                RobotLogger.err("MapUtils.initUpdater()", ex)
            }

        }
    }
}
