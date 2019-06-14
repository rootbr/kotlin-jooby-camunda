package org.rootbr.camunda.spin.gson.format

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.Configuration.ConfigurationBuilder
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import org.camunda.spin.DataFormats
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.spi.DataFormat
import org.camunda.spin.spi.TypeDetector
import org.rootbr.camunda.spin.gson.GsonLogger
import org.rootbr.camunda.spin.gson.GsonNode


class GsonDataFormat
@JvmOverloads constructor(
        var gson: Gson = GsonBuilder().create(),
        var jsonPathConfiguration: Configuration = ConfigurationBuilder()
                .jsonProvider(GsonJsonProvider(gson))
                .mappingProvider(GsonMappingProvider(gson))
                .build()
) : DataFormat<SpinJsonNode> {

    val DATA_FORMAT_NAME = DataFormats.JSON_DATAFORMAT_NAME

    protected var typeDetectors = listOf<TypeDetector>(ListGsonTypeDetector(), DefaultGsonTypeDetector())

    protected var dataFormatReader = GsonDataFormatReader(this)
    protected var dataFormatWriter = GsonDataFormatWriter(this)
    protected var dataFormatMapper = GsonDataFormatMapper(this)


    override fun getName(): String {
        return DATA_FORMAT_NAME
    }

    override fun getWrapperType(): Class<out SpinJsonNode> {
        return GsonNode::class.java
    }

    override fun createWrapperInstance(parameter: Any): SpinJsonNode {
        return GsonNode(parameter as JsonElement, this)
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

    fun createGsonNode(parameter: Any?) = when (parameter) {
        is SpinJsonNode -> parameter.unwrap() as JsonElement
        else -> gson.toJsonTree(parameter)
    }
}
