package org.usfirst.frc.team4322.configuration

import org.usfirst.frc.team4322.logging.RobotLogger
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Pattern

/**
 * Created by nicolasmachado on 2/19/15.
 */

object RobotConfigFileReader {
    private const val CONFIG_FILE = "/home/lvuser/robotConfig.ini"
    private var _instance: RobotConfigFileReader? = null
    private val arrayFinder = Pattern.compile("\\{\\s*([^}]+)\\s*\\}")
    var primitiveMap: MutableMap<Class<*>, Method> = HashMap()

    init {
        try {
            primitiveMap[Boolean::class.javaPrimitiveType!!] = Boolean::class.java.getMethod("parseBoolean", String::class.java)
            primitiveMap[Byte::class.javaPrimitiveType!!] = Byte::class.java.getMethod("parseByte", String::class.java)
            primitiveMap[Short::class.javaPrimitiveType!!] = Short::class.java.getMethod("parseShort", String::class.java)
            primitiveMap[Int::class.javaPrimitiveType!!] = Int::class.java.getMethod("parseInt", String::class.java)
            primitiveMap[Long::class.javaPrimitiveType!!] = Long::class.java.getMethod("parseLong", String::class.java)
            primitiveMap[Float::class.javaPrimitiveType!!] = Float::class.java.getMethod("parseFloat", String::class.java)
            primitiveMap[Double::class.javaPrimitiveType!!] = Double::class.java.getMethod("parseDouble", String::class.java)
        } catch (ex: NoSuchMethodException) {
            RobotLogger.exc("Exception caught in RobotConfigFileReader", ex)
        } catch (ex: SecurityException) {
            RobotLogger.exc("Exception caught in RobotConfigFileReader", ex)
        }

    }

    /**
     * Behold the magical update method.
     * It reads new constants from robotConfig.ini
     */
    fun runRobotFileReader(toFill: Class<*>) {
        RobotLogger.info("Started Config Update.")
        //Initialize INI value holder.
        val p = Properties()
        try {
            FileInputStream(CONFIG_FILE).use { file ->
                //load Values from File.
                p.load(file)
            }
        } catch (ex: IOException) {
            RobotLogger.err("Failed to load robotConfig.ini")
            return
        }

        try {
            //Make an enumerable list of keys in the INI.
            val enumeration = p.propertyNames()
            //While there are unparsed keys;
            while (enumeration.hasMoreElements()) {
                //Get the title of the next key.
                val key = enumeration.nextElement() as String
                //Grab the value for the key.
                val value = p.getProperty(key)
                //create a field to store the RobotMap var.
                var current: Field
                try {
                    //Attempt to get the field for the key title.
                    current = toFill.getField(key)
                } catch (ex: NoSuchFieldException) {
                    RobotLogger.warn("The field \"%s\" doesnt exist in RobotMap!", key)
                    RobotLogger.exc("RobotConfigFileReader.runRobotFileReader()", ex)
                    continue
                }
                //If the field doesnt exist, log it as a warning.
                //if the field is an array....
                if (current.type.isArray) {
                    //use the array Finder to split the values.
                    val m = arrayFinder.matcher(value)
                    //apply the matcher to the string.
                    m.find()
                    //get our values into a string array.
                    val arrayValues = m.group().split("[\\s,]+".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    //remove the brackets from the first and last values.
                    arrayValues[0] = arrayValues[0].replace("{", "")
                    arrayValues[arrayValues.size - 1] = arrayValues[arrayValues.size - 1].replace("}", "")
                    //instantiate an array.
                    val elemClass: Class<*> = current.type.componentType
                    val elementArray = java.lang.reflect.Array.newInstance(elemClass,arrayValues.size)
                    try {
                        //If we are dealing with a string array, directly set the values.
                        if (current.type.componentType == String::class.java) {
                            //Set each value in the array.
                            for (i in arrayValues.indices) {
                                java.lang.reflect.Array.set(elementArray, i, arrayValues[i])
                            }
                        } else {
                            //set each value in the array.
                            for (i in arrayValues.indices) {
                                java.lang.reflect.Array.set(elementArray, i, primitiveMap[current.type.componentType]?.invoke(null, arrayValues[i]))
                            }
                        }//if not, cast appropriately.
                    } catch (ex: InvocationTargetException) {
                        RobotLogger.warn("Unable to set property \"%s\" to \"%s\". Target type was %s[].", key, value, current.type.componentType.simpleName)
                        RobotLogger.exc("RobotConfigFileReader.runRobotFileReader()", ex)
                        continue
                    }
                    //If we cant set it, log the error.
                    //update the field.
                    current.set(null, elementArray)
                } else {
                    try {
                        //set it, with a cast if necessary.
                        current.set(null, if (current.type == String::class.java) value else primitiveMap[current.type]?.invoke(null, value))
                    } catch (ex: InvocationTargetException) {
                        RobotLogger.warn("Unable to set property \"%s\" to \"%s\". Target type was %s.", key, value, current.type.simpleName)
                        RobotLogger.exc("RobotConfigFileReader.runRobotFileReader()", ex)
                        continue
                    }

                }//If we are dealing with a single value....
            }
        } catch (ex: IllegalArgumentException) {
            RobotLogger.exc("Exception caught in runRobotFileReader()", ex)
        } catch (ex: IllegalAccessException) {
            RobotLogger.exc("Exception caught in runRobotFileReader()", ex)
        } catch (ex: SecurityException) {
            RobotLogger.exc("Exception caught in runRobotFileReader()", ex)
        }
        //Deal with misc errors.
        RobotLogger.info("Finished Config Update.")
    }
}