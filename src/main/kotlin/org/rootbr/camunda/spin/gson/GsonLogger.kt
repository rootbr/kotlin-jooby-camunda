package org.rootbr.camunda.spin.gson

import com.google.gson.JsonElement
import org.camunda.commons.logging.BaseLogger
import org.camunda.spin.impl.logging.SpinLogger
import org.camunda.spin.json.SpinJsonDataFormatException
import org.camunda.spin.json.SpinJsonException
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.json.SpinJsonPathException
import org.camunda.spin.json.SpinJsonPropertyException


class GsonLogger : SpinLogger() {

    fun unableToParseInput(e: Exception): SpinJsonDataFormatException {
        return SpinJsonDataFormatException(exceptionMessage("001", "Unable to parse input into json node"), e)
    }


    fun unableToParseValue(expectedType: String, type: String): SpinJsonDataFormatException {
        return SpinJsonDataFormatException(exceptionMessage("002", "Expected '{}', got '{}'", expectedType, type))
    }

    fun unableToWriteJsonNode(cause: Exception): SpinJsonDataFormatException {
        return SpinJsonDataFormatException(exceptionMessage("003", "Unable to write json node"), cause)
    }

    fun unableToFindProperty(propertyName: String): SpinJsonException {
        return SpinJsonPropertyException(exceptionMessage("004", "Unable to find '{}'", propertyName))
    }

    fun unableToCreateNode(objectType: String): SpinJsonException {
        return SpinJsonPropertyException(exceptionMessage("005", "Unable to create node for object of type '{}'", objectType))
    }

    fun unableToDeserialize(jsonNode: JsonElement, type: String, cause: Exception): SpinJsonException {
        return SpinJsonException(
                exceptionMessage("006", "Cannot deserialize '{}...' to java type '{}'",
                        jsonNode.toString().substring(0, 10), type), cause)
    }

    fun unableToConstructJavaType(fromString: String, cause: Exception): SpinJsonDataFormatException {
        return SpinJsonDataFormatException(
                exceptionMessage("007", "Cannot construct java type from string '{}'", fromString), cause)
    }

    fun unableToDetectCanonicalType(parameter: Any): SpinJsonDataFormatException {
        return SpinJsonDataFormatException(exceptionMessage("008", "Cannot detect canonical data type for parameter '{}'", parameter))
    }

    fun unableToMapInput(input: Any, cause: Exception): SpinJsonDataFormatException {
        return SpinJsonDataFormatException(exceptionMessage("009", "Unable to map object '{}' to json node", input), cause)
    }

    fun unableToModifyNode(nodeName: String): SpinJsonException {
        return SpinJsonException(exceptionMessage("010", "Unable to modify node of type '{}'. Node is not a list.", nodeName))
    }

    fun unableToGetIndex(nodeName: String): SpinJsonException {
        return SpinJsonException(exceptionMessage("011", "Unable to get index from '{}'. Node is not a list.", nodeName))
    }

    fun indexOutOfBounds(index: Int?, size: Int?): IndexOutOfBoundsException {
        return IndexOutOfBoundsException(exceptionMessage("012", "Index is out of bound! Index: '{}', Size: '{}'", index, size))
    }

    fun unableToEvaluateJsonPathExpressionOnNode(node: SpinJsonNode, cause: Exception): SpinJsonPathException {
        return SpinJsonPathException(
                exceptionMessage("013", "Unable to evaluate JsonPath expression on element '{}'", node.javaClass.name), cause)
    }

    fun unableToCompileJsonPathExpression(expression: String, cause: Exception): SpinJsonPathException {
        return SpinJsonPathException(
                exceptionMessage("014", "Unable to compile '{}'!", expression), cause)
    }

    fun unableToCastJsonPathResultTo(castClass: Class<*>, cause: Exception): SpinJsonPathException {
        return SpinJsonPathException(
                exceptionMessage("015", "Unable to cast JsonPath expression to '{}'", castClass.name), cause)
    }

    fun invalidJsonPath(castClass: Class<*>, cause: Exception): SpinJsonPathException {
        return SpinJsonPathException(
                exceptionMessage("017", "Invalid json path to '{}'", castClass.name), cause)
    }

    companion object {

        val PROJECT_CODE = SpinLogger.PROJECT_CODE + "/GSON-JSON"
        val JSON_TREE_LOGGER = BaseLogger.createLogger(GsonLogger::class.java, PROJECT_CODE, "org.camunda.spin.json", "01")
    }
}
