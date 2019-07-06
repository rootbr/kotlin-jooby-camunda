package org.rootbr.camunda.spin.gson.format

import org.camunda.spin.DataFormats.JSON_DATAFORMAT_NAME
import org.camunda.spin.spi.DataFormat
import org.camunda.spin.spi.DataFormatProvider

class GsonDataFormatProvider : DataFormatProvider {
    override fun getDataFormatName() = JSON_DATAFORMAT_NAME
    override fun createInstance(): DataFormat<*> = GsonDataFormat(JSON_DATAFORMAT_NAME)
}
