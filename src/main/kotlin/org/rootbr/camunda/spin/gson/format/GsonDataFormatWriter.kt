package org.rootbr.camunda.spin.gson.format

import org.camunda.spin.spi.DataFormatWriter
import org.rootbr.camunda.spin.gson.GsonLogger
import org.rootbr.camunda.spin.gson.GsonNode
import java.io.IOException
import java.io.Writer

class GsonDataFormatWriter(protected var dataFormat: GsonDataFormat) : DataFormatWriter {

    override fun writeToWriter(writer: Writer, input: Any) {
        val objectMapper = dataFormat.objectMapper
        val factory = objectMapper.getFactory()

        try {
            val generator = factory.createGenerator(writer)
            objectMapper.writeTree(generator, input as GsonNode)
        } catch (e: IOException) {
            throw LOG.unableToWriteJsonNode(e)
        }

    }

    companion object {

        private val LOG = GsonLogger.JSON_TREE_LOGGER
    }

}
