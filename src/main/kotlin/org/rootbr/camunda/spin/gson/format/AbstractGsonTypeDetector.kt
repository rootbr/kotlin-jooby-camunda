package org.rootbr.camunda.spin.gson.format

import org.camunda.spin.spi.DataFormat
import org.camunda.spin.spi.TypeDetector

abstract class AbstractGsonTypeDetector : TypeDetector {
    fun appliesTo(dataFormat: DataFormat<*>)= dataFormat is GsonDataFormat
}
