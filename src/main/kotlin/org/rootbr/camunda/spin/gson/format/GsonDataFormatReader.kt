package org.rootbr.camunda.spin.gson.format

import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import org.camunda.spin.spi.TextBasedDataFormatReader
import org.rootbr.camunda.spin.gson.GsonLogger
import java.io.IOException
import java.io.Reader
import java.util.regex.Pattern

class GsonDataFormatReader(
        protected var dataFormat: GsonDataFormat
) : TextBasedDataFormatReader() {
    private val JSON_LOGGER = GsonLogger.JSON_TREE_LOGGER
    private val INPUT_MATCHING_PATTERN = Pattern.compile("\\A(\\s)*[{\\[]")

    override fun readInput(input: Reader) = try {
        JsonParser().parse(dataFormat.gson.newJsonReader(input)) ?: throw IOException("Input is empty")
    } catch (e: JsonSyntaxException) {
        throw JSON_LOGGER.unableToParseInput(e)
    } catch (e: JsonIOException) {
        throw JSON_LOGGER.unableToParseInput(e)
    }

    override fun getInputDetectionPattern() = INPUT_MATCHING_PATTERN
}
