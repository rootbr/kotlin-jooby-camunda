package org.rootbr.camunda.spin.gson


import com.google.gson.*
import com.jayway.jsonpath.JsonPath
import org.camunda.commons.utils.EnsureUtil.ensureNotNull
import org.camunda.spin.Spin
import org.camunda.spin.SpinList
import org.camunda.spin.impl.SpinListImpl
import org.camunda.spin.json.SpinJsonDataFormatException
import org.camunda.spin.json.SpinJsonException
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.json.SpinJsonPathQuery
import org.rootbr.camunda.spin.gson.format.GsonDataFormat
import org.rootbr.camunda.spin.gson.query.GsonPathQuery
import java.io.StringWriter
import java.io.Writer
import java.nio.file.InvalidPathException
import java.util.*

class GsonNode(protected val jsonNode: JsonElement, protected val dataFormat: GsonDataFormat) : SpinJsonNodeAdapter() {

    override fun getDataFormatName() = dataFormat.name
    override fun unwrap(): JsonElement = jsonNode

    override fun toString(): String {
        val writer = StringWriter()
        writeToWriter(writer)
        return writer.toString()
    }

    override fun writeToWriter(writer: Writer) = dataFormat.writer.writeToWriter(writer, jsonNode)

    protected fun getCorrectIndex(index: Int): Int {
        jsonNode as JsonArray
        val size = jsonNode.size()
        var newIndex = index

        // reverse walking through the array
        if (index < 0) {
            newIndex = size + index
        }

        // the negative index would be greater than the size a second time!
        if (newIndex < 0) {
            throw LOG.indexOutOfBounds(index, size)
        }

        // the index is greater as the actual size
        if (index > size) {
            throw LOG.indexOutOfBounds(index, size)
        }
        return newIndex
    }

    override fun indexOf(searchObject: Any): Int? {
        ensureNotNull("searchObject", searchObject)
        if (jsonNode is JsonArray) {
            val node = dataFormat.createGsonNode(searchObject)
            val i = jsonNode.indexOf(node)
            // when searchObject is not found
            if (i == -1) throw LOG.unableToFindProperty(node.toString())
            return i
        } else {
            throw LOG.unableToGetIndex(jsonNode.javaClass.simpleName)
        }
    }

    override fun lastIndexOf(searchObject: Any): Int? {
        ensureNotNull("searchObject", searchObject)
        if (jsonNode is JsonArray) {
            val node = dataFormat.createGsonNode(searchObject)
            val i = jsonNode.lastIndexOf(node)
            // when searchObject is not found
            if (i == -1) {
                throw LOG.unableToFindProperty(node.javaClass.simpleName)
            }
            return i
        } else {
            throw LOG.unableToGetIndex(jsonNode.javaClass.simpleName)
        }
    }

    override fun isObject(): Boolean {
        return jsonNode.isJsonObject
    }

    override fun hasProp(name: String): Boolean {
        jsonNode as JsonObject
        return jsonNode.has(name)
    }

    override fun prop(name: String): SpinJsonNode {
        ensureNotNull("name", name)
        jsonNode as JsonObject
        if (jsonNode.has(name)) {
            val property = jsonNode.get(name)
            return Spin.S(property)
        } else {
            throw LOG.unableToFindProperty(name)
        }
    }

    override fun prop(name: String, newProperty: String?): SpinJsonNode {
        jsonNode as JsonObject
        jsonNode.addProperty(name, newProperty)
        return this
    }

    override fun prop(name: String, newProperty: Number?): SpinJsonNode {
        jsonNode as JsonObject
        jsonNode.addProperty(name, newProperty)
        return this
    }

    override fun prop(name: String, newProperty: Int): SpinJsonNode {
        return prop(name, newProperty as? Number)
    }

    override fun prop(name: String, newProperty: Float): SpinJsonNode {
        return prop(name, newProperty as? Number)
    }

    override fun prop(name: String, newProperty: Long): SpinJsonNode {
        return prop(name, newProperty as? Number)
    }

    override fun prop(name: String, newProperty: Boolean?): SpinJsonNode {
        jsonNode as JsonObject
        jsonNode.addProperty(name, newProperty)
        return this
    }

    override fun prop(name: String, newProperty: List<Any>): SpinJsonNode {
        jsonNode as JsonObject
        jsonNode.add(name, dataFormat.createGsonNode(newProperty))
        return this
    }

    override fun prop(name: String, newProperty: Map<String, Any>): SpinJsonNode {
        jsonNode as JsonObject
        jsonNode.add(name, dataFormat.createGsonNode(newProperty))
        return this
    }

    override fun prop(name: String, newProperty: SpinJsonNode?): SpinJsonNode {
        jsonNode as JsonObject
        jsonNode.add(name, newProperty?.unwrap() as JsonElement)
        return this
    }

    override fun deleteProp(name: String): SpinJsonNode {
        ensureNotNull("name", name)
        jsonNode as JsonObject
        if (jsonNode.has(name)) {
            jsonNode.remove(name)
            return this
        } else {
            throw LOG.unableToFindProperty(name)
        }
    }

    override fun deleteProp(names: List<String>): SpinJsonNode {
        ensureNotNull("names", names)
        names.forEach { deleteProp(it) }
        return this
    }

    override fun append(property: Any): SpinJsonNode {
        ensureNotNull("property", property)
        if (jsonNode.isJsonArray) {
            jsonNode as JsonArray
            jsonNode.add(dataFormat.createGsonNode(property))
            return this
        } else {
            throw LOG.unableToModifyNode(jsonNode.javaClass.simpleName)
        }
    }

    override fun insertAt(index: Int, property: Any): SpinJsonNode {
        ensureNotNull("property", property)
        if (jsonNode is JsonArray) {
            val index = getCorrectIndex(index)
            jsonNode.set(index, dataFormat.createGsonNode(property))
            return this
        } else {
            throw LOG.unableToModifyNode(jsonNode.javaClass.simpleName)
        }
    }

    override fun insertBefore(searchObject: Any, insertObject: Any): SpinJsonNode {
        ensureNotNull("searchObject", searchObject)
        ensureNotNull("insertObject", insertObject)
        if (this.isArray) {
            val i = indexOf(searchObject)
            return insertAt(i!!, insertObject)
        } else {
            throw LOG.unableToCreateNode(jsonNode.javaClass.simpleName)
        }
    }

    override fun insertAfter(searchObject: Any, insertObject: Any): SpinJsonNode {
        ensureNotNull("searchObject", searchObject)
        ensureNotNull("insertObject", insertObject)
        if (this.isArray) {
            val i = indexOf(searchObject)
            return insertAt(i!! + 1, insertObject)
        } else {
            throw LOG.unableToCreateNode(jsonNode.javaClass.simpleName)
        }
    }

    override fun remove(property: Any): SpinJsonNode {
        return removeAt(indexOf(property)!!)
    }

    override fun removeLast(property: Any): SpinJsonNode {
        return removeAt(lastIndexOf(property)!!)
    }

    override fun removeAt(index: Int): SpinJsonNode {
        if (jsonNode is JsonArray) {
            jsonNode.remove(getCorrectIndex(index))
            return this
        } else {
            throw LOG.unableToModifyNode(jsonNode.javaClass.simpleName)
        }
    }

    override fun isBoolean(): Boolean {
        return jsonNode is JsonPrimitive && jsonNode.isBoolean
    }

    override fun boolValue(): Boolean? {
        return if (isBoolean) {
            jsonNode.asBoolean
        } else {
            throw LOG.unableToParseValue(Boolean::class.java.simpleName, jsonNode.javaClass.simpleName)
        }
    }

    override fun isNumber(): Boolean {
        return jsonNode is JsonPrimitive && jsonNode.isNumber
    }

    override fun numberValue(): Number? {
        return if (isNumber) {
            jsonNode.asNumber
        } else {
            throw LOG.unableToParseValue(Number::class.java.simpleName, jsonNode.javaClass.simpleName)
        }
    }

    override fun isString(): Boolean {
        return jsonNode is JsonPrimitive && jsonNode.isString
    }

    override fun stringValue(): String {
        return if (isString) {
            jsonNode.asString
        } else {
            throw LOG.unableToParseValue(String::class.java.simpleName, jsonNode.javaClass.simpleName)
        }
    }

    override fun isNull(): Boolean {
        return jsonNode is JsonNull
    }

    override fun isValue(): Boolean {
        return jsonNode.isJsonPrimitive
    }

    override fun value(): Any? {
        if (jsonNode is JsonPrimitive) {
            if (jsonNode.isBoolean) return jsonNode.asBoolean
            if (jsonNode.isNumber) return jsonNode.asNumber
            if (jsonNode.isString) return jsonNode.asString
            if (jsonNode.isJsonNull) return null
        }
        throw LOG.unableToParseValue("String/Number/Boolean/Null", jsonNode.javaClass.simpleName)
    }

    override fun isArray(): Boolean {
        return jsonNode is JsonArray
    }

    override fun elements(): SpinList<SpinJsonNode> {
        if (jsonNode is JsonArray) {
            val iterator = jsonNode.iterator()
            val list = SpinListImpl<SpinJsonNode>()
            while (iterator.hasNext()) {
                list.add(dataFormat.createWrapperInstance(iterator.next()))
            }
            return list
        } else {
            throw LOG.unableToParseValue("SpinList", jsonNode.javaClass.simpleName)
        }
    }

    override fun fieldNames(): List<String> {
        if (jsonNode is JsonObject) {
            return ArrayList<String>(jsonNode.keySet())
        } else {
            // TODO how can JsonArray?
            throw LOG.unableToParseValue("Array/Object", jsonNode.javaClass.simpleName)
        }
    }

    override fun jsonPath(expression: String): SpinJsonPathQuery {
        ensureNotNull("expression", expression)
        try {
            val query = JsonPath.compile(expression)
            return GsonPathQuery(this, query, dataFormat.gson)
        } catch (pex: InvalidPathException) {
            throw LOG.unableToCompileJsonPathExpression(expression, pex)
        } catch (aex: IllegalArgumentException) {
            throw LOG.unableToCompileJsonPathExpression(expression, aex)
        }

    }

    /**
     * Maps the json represented by this object to a java object of the given type.
     *
     * @throws SpinJsonException if the json representation cannot be mapped to the specified type
     */
    override fun <C> mapTo(type: Class<C>): C {
        return dataFormat.gson.fromJson(jsonNode, type)
    }

    /**
     * Maps the json represented by this object to a java object of the given type.
     * Argument is to be supplied in Jackson's canonical type string format
     * (see [JavaType.toCanonical]).
     *
     * @throws SpinJsonException if the json representation cannot be mapped to the specified type
     * @throws SpinJsonDataFormatException if the parameter does not match a valid type
     */
    override fun <C> mapTo(type: String): C {
        return dataFormat.gson.fromJson(jsonNode, Class.forName(type)) as C
    }

    companion object {
        private val LOG = GsonLogger.JSON_TREE_LOGGER
    }
}
