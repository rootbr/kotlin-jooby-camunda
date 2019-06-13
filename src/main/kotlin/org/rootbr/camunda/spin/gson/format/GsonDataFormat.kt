package org.rootbr.camunda.spin.gson.format

import com.google.gson.JsonElement
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.Configuration.ConfigurationBuilder
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import jdk.vm.ci.meta.JavaType
import org.camunda.commons.utils.EnsureUtil.ensureNotNull
import org.camunda.spin.DataFormats
import org.camunda.spin.json.SpinJsonDataFormatException
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.spi.DataFormat
import org.camunda.spin.spi.TypeDetector
import org.rootbr.camunda.spin.gson.GsonLogger
import org.rootbr.camunda.spin.gson.GsonNode
import java.util.*


class GsonDataFormat @JvmOverloads constructor(protected val name: String,
                                               var objectMapper: ObjectMapper = ObjectMapper(),
                                               var jsonPathConfiguration: Configuration = ConfigurationBuilder()
                                                       .jsonProvider(GsonProvider(objectMapper))
                                                       .mappingProvider(JacksonMappingProvider(objectMapper))
                                                       .build()) : DataFormat<SpinJsonNode> {

    protected var typeDetectors: MutableList<TypeDetector>
    protected var dataFormatReader: GsonDataFormatReader
    protected var dataFormatWriter: GsonDataFormatWriter
    protected var dataFormatMapper: GsonDataFormatMapper

    init {
        init()
    }


    // initialization /////////////////////////////////////////////

    protected fun init() {
        initReader()
        initWriter()
        initMapper()
        initTypeDetectors()
    }

    protected fun initMapper() {
        this.dataFormatMapper = GsonDataFormatMapper(this)
    }

    protected fun initWriter() {
        this.dataFormatWriter = GsonDataFormatWriter(this)
    }

    protected fun initReader() {
        this.dataFormatReader = GsonDataFormatReader(this)
    }


    protected fun initTypeDetectors() {
        typeDetectors = ArrayList()
        typeDetectors.add(ListGsonTypeDetector())
        typeDetectors.add(DefaultGsonTypeDetector())
    }

    // interface implementation ///////////////////////////////////

    override fun getName(): String {
        return DATA_FORMAT_NAME
    }

    override fun getWrapperType(): Class<out SpinJsonNode> {
        return GsonNode::class.java
    }

    override fun createWrapperInstance(parameter: Any): SpinJsonNode {
        return GsonNode(parameter as JsonElement, this)
    }

    /**
     * Identifies the canonical type of an object heuristically.
     *
     * @return the canonical type identifier of the object's class
     * according to Jackson's type format (see [TypeFactory.constructFromCanonical])
     */
    fun getCanonicalTypeName(`object`: Any): String {
        ensureNotNull("object", `object`)

        for (typeDetector in typeDetectors) {
            if (typeDetector.canHandle(`object`)) {
                return typeDetector.detectType(`object`)
            }
        }

        throw LOG.unableToDetectCanonicalType(`object`)
    }

    /**
     * Constructs a [JavaType] object based on the parameter, which
     * has to follow Jackson's canonical type string format.
     *
     * @param canonicalString canonical string representation of the type
     * @return the constructed java type
     * @throws SpinJsonDataFormatException if no type can be constructed from the given parameter
     */
    fun constructJavaTypeFromCanonicalString(canonicalString: String): JavaType {
        try {
            return TypeFactory.defaultInstance().constructFromCanonical(canonicalString)
        } catch (e: IllegalArgumentException) {
            throw LOG.unableToConstructJavaType(canonicalString, e)
        }

    }

    fun addTypeDetector(typeDetector: TypeDetector) {
        typeDetectors.add(0, typeDetector)
    }

    override fun getMapper(): GsonDataFormatMapper {
        return dataFormatMapper
    }

    override fun getReader(): GsonDataFormatReader {
        return dataFormatReader
    }

    override fun getWriter(): GsonDataFormatWriter {
        return dataFormatWriter
    }

    // helper functions //////////////////////////////////////////

    fun createJsonNode(parameter: Any?): GsonNode {
        return if (parameter is SpinJsonNode) {
            parameter.unwrap() as GsonNode

        } else if (parameter is String) {
            createJsonNode(parameter as String?)

        } else if (parameter is Int) {
            createJsonNode(parameter as Int?)

        } else if (parameter is Boolean) {
            createJsonNode(parameter as Boolean?)

        } else if (parameter is Float) {
            createJsonNode(parameter as Float?)

        } else if (parameter is Long) {
            createJsonNode(parameter as Long?)

        } else if (parameter is Number) {
            createJsonNode(parameter.toFloat())

        } else if (parameter is List<*>) {
            createJsonNode(parameter as List<Any>?)

        } else if (parameter is Map<*, *>) {
            createJsonNode(parameter as Map<String, Any>?)

        } else if (parameter == null) {
            createNullJsonNode()
        } else {
            throw LOG.unableToCreateNode(parameter.javaClass.simpleName)
        }
    }

    fun createJsonNode(parameter: String): GsonNode {
        return objectMapper.getNodeFactory().textNode(parameter)
    }

    fun createJsonNode(parameter: Int?): GsonNode {
        return objectMapper.getNodeFactory().numberNode(parameter)
    }

    fun createJsonNode(parameter: Float?): GsonNode {
        return objectMapper.getNodeFactory().numberNode(parameter)
    }

    fun createJsonNode(parameter: Long?): GsonNode {
        return objectMapper.getNodeFactory().numberNode(parameter)
    }

    fun createJsonNode(parameter: Boolean?): GsonNode {
        return objectMapper.getNodeFactory().booleanNode(parameter)
    }

    fun createJsonNode(parameter: List<Any>?): GsonNode {
        if (parameter != null) {
            val node = objectMapper.getNodeFactory().arrayNode()
            for (entry in parameter) {
                node.add(createJsonNode(entry))
            }
            return node
        } else {
            return createNullJsonNode()
        }

    }

    fun createJsonNode(parameter: Map<String, Any>?): GsonNode {
        if (parameter != null) {
            val node = objectMapper.getNodeFactory().objectNode()
            for ((key, value) in parameter) {
                node.set(key, createJsonNode(value))
            }
            return node
        } else {
            return createNullJsonNode()
        }
    }

    fun createNullJsonNode(): GsonNode {
        return objectMapper.getNodeFactory().nullNode()
    }

    companion object {

        val DATA_FORMAT_NAME = DataFormats.JSON_DATAFORMAT_NAME

        private val LOG = GsonLogger.JSON_TREE_LOGGER
    }
}
