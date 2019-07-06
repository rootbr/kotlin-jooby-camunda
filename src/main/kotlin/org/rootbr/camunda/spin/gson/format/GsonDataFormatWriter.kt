package org.rootbr.camunda.spin.gson.format

import com.google.gson.JsonElement
import org.camunda.spin.spi.DataFormatWriter
import org.rootbr.camunda.spin.gson.GsonLogger
import java.io.IOException
import java.io.Writer

class GsonDataFormatWriter(protected var dataFormat: GsonDataFormat) : DataFormatWriter {
    private val LOG = GsonLogger.JSON_TREE_LOGGER
    override fun writeToWriter(writer: Writer, input: Any) {
        try {
            dataFormat.gson.toJson(input as JsonElement, writer)
            writer.flush()
        } catch (e: IOException) {
            throw LOG.unableToWriteJsonNode(e)
        }
    }
}
