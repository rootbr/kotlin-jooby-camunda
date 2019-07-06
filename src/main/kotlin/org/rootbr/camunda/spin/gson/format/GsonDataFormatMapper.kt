package org.rootbr.camunda.spin.gson.format

import com.google.gson.JsonElement
import org.camunda.spin.spi.DataFormatMapper
import org.rootbr.camunda.spin.gson.GsonLogger

class GsonDataFormatMapper(protected var format: GsonDataFormat) : DataFormatMapper {
    private val LOG = GsonLogger.JSON_TREE_LOGGER
    override fun canMap(parameter: Any?) = true
    override fun getCanonicalTypeName(obj: Any) = obj.javaClass.canonicalName
    override fun mapJavaToInternal(parameter: Any) = try {
        format.gson.toJsonTree(parameter)
    } catch (e: IllegalArgumentException) {
        throw LOG.unableToMapInput(parameter, e)
    }
    override fun <T> mapInternalToJava(parameter: Any, type: Class<T>): T = format.gson.fromJson(parameter as JsonElement, type)
    override fun <T> mapInternalToJava(parameter: Any, typeIdentifier: String): T {
        val aClass = Class.forName(typeIdentifier, true, Thread.currentThread().contextClassLoader)
        return format.gson.fromJson(parameter as JsonElement, aClass) as T
    }
}
