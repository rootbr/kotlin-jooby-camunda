package org.rootbr.camunda.spin.gson.format

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.Configuration.ConfigurationBuilder
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import org.camunda.spin.DataFormats
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.spi.DataFormat
import org.rootbr.camunda.spin.gson.GsonNode

class GsonDataFormat
@JvmOverloads constructor(
        protected val nameDataFormat: String = DataFormats.JSON_DATAFORMAT_NAME,
        val gson: Gson = GsonBuilder().create(),
        val jsonPathConfiguration: Configuration = ConfigurationBuilder()
                .jsonProvider(GsonJsonProvider(gson))
                .mappingProvider(GsonMappingProvider(gson))
                .build()
) : DataFormat<SpinJsonNode> {
    protected var dataFormatReader = GsonDataFormatReader(this)
    protected var dataFormatWriter = GsonDataFormatWriter(this)
    protected var dataFormatMapper = GsonDataFormatMapper(this)
    override fun getName() = nameDataFormat
    override fun getWrapperType(): Class<out SpinJsonNode> = GsonNode::class.java
    override fun createWrapperInstance(parameter: Any): SpinJsonNode  = GsonNode(createGsonNode(parameter), this)
    override fun getMapper() =  dataFormatMapper
    override fun getReader()= dataFormatReader
    override fun getWriter()= dataFormatWriter
    fun createGsonNode(parameter: Any?) = when (parameter) {
        is SpinJsonNode -> parameter.unwrap() as JsonElement
        else -> gson.toJsonTree(parameter)
    }
}
