package org.rootbr.camunda.spin.gson.format

import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.internal.bind.ObjectTypeAdapter
import org.camunda.spin.spi.DataFormatWriter
import org.rootbr.camunda.spin.gson.GsonLogger
import java.io.IOException
import java.io.Writer

class GsonDataFormatWriter(protected var dataFormat: GsonDataFormat) : DataFormatWriter {
    private val LOG = GsonLogger.JSON_TREE_LOGGER

    override fun writeToWriter(writer: Writer, input: Any) {
        val jsonWriter = dataFormat.gson.newJsonWriter(writer)

        try {
            val typeAdapter = dataFormat.gson.getAdapter(input.javaClass) as TypeAdapter<Any>
            if (typeAdapter is ObjectTypeAdapter) {
                jsonWriter.beginObject()
                jsonWriter.endObject()
                return
            }
            typeAdapter.write(jsonWriter, input as JsonElement)
        } catch (e: IOException) {
            throw LOG.unableToWriteJsonNode(e)
        }
    }
}
