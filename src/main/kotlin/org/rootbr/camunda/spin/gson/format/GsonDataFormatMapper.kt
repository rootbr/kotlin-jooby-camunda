package org.rootbr.camunda.spin.gson.format

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.jayway.jsonpath.internal.filter.ValueNode
import jdk.vm.ci.meta.JavaType
import java.io.IOException
import org.camunda.spin.impl.json.jackson.JacksonJsonLogger
import org.camunda.spin.spi.DataFormatMapper

class GsonDataFormatMapper(protected var format: GsonDataFormat) : DataFormatMapper {

    override fun canMap(parameter: Any?): Boolean {
        // Jackson ObjectMapper#canSerialize() method was removed
        // due to causing performance issues in high load scenarios
        return parameter != null
    }

    override fun getCanonicalTypeName(`object`: Any): String {
        return format.getCanonicalTypeName(`object`)
    }

    override fun mapJavaToInternal(parameter: Any): Any {
        val mapper = format.objectMapper
        try {
            return mapper.valueToTree(parameter)
        } catch (e: IllegalArgumentException) {
            throw LOG.unableToMapInput(parameter, e)
        }

    }

    override fun <T> mapInternalToJava(parameter: Any, type: Class<T>): T {
        val javaType = TypeFactory.defaultInstance().constructType(type)
        return mapInternalToJava(parameter, javaType)
    }

    override fun <T> mapInternalToJava(parameter: Any, typeIdentifier: String): T {
        try {
            //sometimes the class identifier is at once a fully qualified class name
            val aClass = Class.forName(typeIdentifier, true, Thread.currentThread().contextClassLoader)
            return mapInternalToJava<*>(parameter, aClass) as T
        } catch (e: ClassNotFoundException) {
            val javaType = format.constructJavaTypeFromCanonicalString(typeIdentifier)
            return mapInternalToJava(parameter, javaType)
        }

    }

    fun <C> mapInternalToJava(parameter: Any, type: JavaType): C {
        val jsonNode = parameter as ValueNode.JsonNode
        val mapper = format.objectMapper
        try {
            return mapper.readValue(mapper.treeAsTokens(jsonNode), type)
        } catch (e: IOException) {
            throw LOG.unableToDeserialize(jsonNode, type, e)
        }

    }

    companion object {

        private val LOG = JacksonJsonLogger.JSON_TREE_LOGGER
    }

}
