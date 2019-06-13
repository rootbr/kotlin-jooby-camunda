package org.rootbr.camunda.spin.gson.format

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import java.lang.reflect.TypeVariable

class ListGsonTypeDetector : AbstractGsonTypeDetector() {

    override fun canHandle(`object`: Any): Boolean {
        return `object` is List<*>
    }

    override fun detectType(`object`: Any): String {
        return constructType(`object`).toCanonical()
    }

    protected fun constructType(`object`: Any): JavaType {
        val typeFactory = TypeFactory.defaultInstance()

        if (`object` is List<*> && !`object`.isEmpty()) {
            val firstElement = `object`[0]
            if (bindingsArePresent(`object`.javaClass)) {
                val elementType = constructType(firstElement)
                return typeFactory.constructCollectionType(`object`.javaClass, elementType)
            }
        }
        return typeFactory.constructType(`object`.javaClass)
    }

    private fun bindingsArePresent(erasedType: Class<*>): Boolean {
        val vars = erasedType.typeParameters
        val varLen = vars?.size ?: 0
        if (varLen == 0) {
            return false
        }
        if (varLen != 1) {
            throw IllegalArgumentException("Cannot create TypeBindings for class " + erasedType.name + " with 1 type parameter: class expects " + varLen)
        }
        return true
    }

}
