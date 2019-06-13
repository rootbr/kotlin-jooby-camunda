package org.rootbr.camunda.spin.gson.format

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.io.Reader
import java.util.regex.Pattern
import org.camunda.spin.impl.json.jackson.JacksonJsonLogger
import org.camunda.spin.spi.TextBasedDataFormatReader

/**
 * @author Thorben Lindhauer
 */
class GsonDataFormatReader(protected var format: GsonDataFormat) : TextBasedDataFormatReader() {

    override fun readInput(input: Reader): Any {
        val mapper = format.objectMapper

        try {
            return mapper.readTree(input) ?: throw IOException("Input is empty")
        } catch (e: JsonProcessingException) {
            throw JSON_LOGGER.unableToParseInput(e)
        } catch (e: IOException) {
            throw JSON_LOGGER.unableToParseInput(e)
        }

    }

    override fun getInputDetectionPattern(): Pattern {
        return INPUT_MATCHING_PATTERN
    }

    companion object {

        private val JSON_LOGGER = JacksonJsonLogger.JSON_TREE_LOGGER
        private val INPUT_MATCHING_PATTERN = Pattern.compile("\\A(\\s)*[{\\[]")
    }

}
