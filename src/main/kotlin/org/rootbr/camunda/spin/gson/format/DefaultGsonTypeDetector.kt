package org.rootbr.camunda.spin.gson.format


class DefaultGsonTypeDetector : AbstractGsonTypeDetector() {

    override fun canHandle(`object`: Any): Boolean {
        return true
    }

    override fun detectType(`object`: Any): String {
        val type = `object`.javaClass
        return TypeFactory.defaultInstance().constructType(type).toCanonical()
    }

}
