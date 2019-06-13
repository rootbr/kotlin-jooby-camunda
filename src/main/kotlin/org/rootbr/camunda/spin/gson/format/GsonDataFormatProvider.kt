package org.rootbr.camunda.spin.gson.format

import org.camunda.spin.DataFormats.JSON_DATAFORMAT_NAME
import org.camunda.spin.spi.DataFormat
import org.camunda.spin.spi.DataFormatProvider

/**
 * Provides the [GsonDataFormat] with default configuration.
 *
 * @author Daniel Meyer
 */
class GsonDataFormatProvider : DataFormatProvider {

    protected var names: Set<String>? = null

    override fun getDataFormatName(): String {
        return JSON_DATAFORMAT_NAME
    }

    override fun createInstance(): DataFormat<*> {
        return GsonDataFormat(JSON_DATAFORMAT_NAME)
    }
}
