package org.rootbr.camunda.spin.gson.query

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.InvalidPathException
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import org.camunda.spin.Spin.JSON
import org.camunda.spin.SpinList
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.json.SpinJsonPathQuery
import org.rootbr.camunda.spin.gson.GsonLogger
import org.rootbr.camunda.spin.gson.GsonNode


class GsonPathQuery(protected val spinJsonNode: SpinJsonNode, protected val query: JsonPath, protected val gson: Gson = GsonBuilder().create()) : SpinJsonPathQuery {

    override fun element(): SpinJsonNode {
        try {
            val result: Any = query.read(spinJsonNode.toString(), Configuration.defaultConfiguration())
            val node: JsonElement = gson.toJsonTree(result)
            return JSON(node)
        } catch (pex: PathNotFoundException) {
            throw LOG.unableToEvaluateJsonPathExpressionOnNode(spinJsonNode, pex)
        } catch (cex: ClassCastException) {
            throw LOG.unableToCastJsonPathResultTo(SpinJsonNode::class.java, cex)
        } catch (iex: InvalidPathException) {
            throw LOG.invalidJsonPath(SpinJsonNode::class.java, iex)
        }
    }

    override fun elementList(): SpinList<SpinJsonNode> {
        val node = element() as GsonNode
        return if (node.isArray) {
            node.elements()
        } else {
            throw LOG.unableToParseValue("SpinList", node.javaClass.simpleName)
        }
    }

    override fun stringValue(): String {
        val node = element() as GsonNode
        return if (node.isString) {
            node.stringValue()
        } else {
            throw LOG.unableToParseValue(String::class.java.simpleName, node.javaClass.simpleName)
        }
    }

    override fun numberValue(): Number? {
        val node = element() as GsonNode
        return if (node.isNumber) {
            node.numberValue()
        } else {
            throw LOG.unableToParseValue(Number::class.java.simpleName, node.javaClass.simpleName)
        }
    }

    override fun boolValue(): Boolean? {
        val node = element() as GsonNode
        return if (node.isBoolean) {
            node.boolValue()
        } else {
            throw LOG.unableToParseValue(Boolean::class.java.simpleName, node.javaClass.simpleName)
        }
    }

    companion object {
        private val LOG = GsonLogger.JSON_TREE_LOGGER
    }
}
