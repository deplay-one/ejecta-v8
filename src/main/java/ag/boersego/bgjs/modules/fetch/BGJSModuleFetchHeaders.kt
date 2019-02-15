package ag.boersego.bgjs.modules

import ag.boersego.bgjs.JNIV8Array
import ag.boersego.bgjs.JNIV8Iterator
import ag.boersego.bgjs.JNIV8Object
import ag.boersego.bgjs.V8Engine
import ag.boersego.v8annotations.V8Function
import ag.boersego.v8annotations.V8Symbols
import okhttp3.Headers
import okhttp3.Request
import java.util.*
import kotlin.collections.HashMap

class BGJSModuleFetchHeaders @JvmOverloads constructor(v8Engine: V8Engine, jsPtr: Long = 0, args: Array<Any>? = null) : JNIV8Object(v8Engine, jsPtr, args) {

    private val headers = HashMap<String, ArrayList<Any>>()

    private fun normalizePotentialValue(ptValue: String): String {
        return ptValue.trim()
    }

    private fun normalizeName(name: String): String {
        return name.trim().toLowerCase(Locale.ROOT)
    }

    @V8Function
    fun append(rawName: String, rawValue: String) {
        val value = normalizePotentialValue(rawValue)
        if (value.contains(ZERO_BYTE) || value.contains('\n') || value.contains('\r')) {
            throw RuntimeException("TypeError: illegal character in value")
        }
        val name = normalizeName(rawName)

        var currentList = headers[name]

        if (currentList == null) {
            currentList = ArrayList()
            headers.put(name, currentList)
        }
        currentList.add(value)
    }

    /**
     * Overwrite a header in the header list
     */
    @V8Function
    fun set(rawName: String, rawValue: String) {
        val value = normalizePotentialValue(rawValue)
        if (value.contains(ZERO_BYTE) || value.contains('\n') || value.contains('\r')) {
            throw RuntimeException("TypeError: illegal character in value")
        }
        val name = normalizeName(rawName)

        var currentList = headers[name]

        if (currentList == null) {
            currentList = ArrayList()
            headers.put(name, currentList)
        } else {
            currentList.clear()
        }
        currentList.add(value)
    }

    @V8Function
    fun delete(name: String) {
        headers.remove(normalizeName(name))
    }

    @V8Function
    fun get(name: String): String? {
        return headers.get(normalizeName(name))?.joinToString(",")
    }

    @V8Function
    fun has(name: String): Boolean {
        return headers.containsKey(normalizeName(name))
    }

    @V8Function
    fun entries(): JNIV8Array {
        val result = arrayOfNulls<JNIV8Array>(headers.size)
        var i = 0
        for (header in headers) {
            val innerList = JNIV8Array.CreateWithElements(v8Engine, header.key, header.value.joinToString(", "))
            result[i++] = innerList
        }

        return JNIV8Array.CreateWithArray(v8Engine, result)
    }

    @V8Function(symbol = V8Symbols.ITERATOR)
    fun iterator() : JNIV8Iterator {
        val it = headers.entries.iterator()
        return JNIV8Iterator(v8Engine, object: Iterator<Any> {
            override fun hasNext(): Boolean {
                return it.hasNext()
            }

            override fun next(): Any {
                val nextVal = it.next()
                return JNIV8Array.CreateWithElements(v8Engine, nextVal.key, nextVal.value.joinToString(", "))
            }

        })
    }

    @V8Function
    fun keys(): JNIV8Array {
        return JNIV8Array.CreateWithArray(v8Engine, headers.keys.toTypedArray())
    }

    @V8Function
    fun values(): JNIV8Array {
        val valueList = ArrayList<Any>()
        for (header in headers.values) {
            valueList.add(header.joinToString(","))
        }

        return JNIV8Array.CreateWithArray(v8Engine, valueList.toTypedArray())
    }

    fun clone(): BGJSModuleFetchHeaders {
        val clone = BGJSModuleFetchHeaders(v8Engine)
        clone.headers.putAll(headers)

        return clone
    }

    fun applyToRequest(builder: Request.Builder) {
        for (header in headers.entries) {
            builder.addHeader(header.key, header.value.joinToString(","))
        }
    }

    companion object {
        fun createFrom(headerRaw: JNIV8Object): BGJSModuleFetchHeaders {
            val fields = headerRaw.v8Fields

            val headers = BGJSModuleFetchHeaders(headerRaw.v8Engine)
            for (entry in fields) {
                if (entry.value !is String) {
                    throw RuntimeException("init.headers object: values must be Strings (problem with key '${entry.key}'")
                }
                headers.set(entry.key, entry.value as String)
            }

            return headers
        }

        fun createFrom(v8Engine: V8Engine, httpHeaders: Headers): BGJSModuleFetchHeaders {
            val headers = BGJSModuleFetchHeaders(v8Engine)
            for (entry in httpHeaders.toMultimap()) {
                headers.headers.set(entry.key, ArrayList<Any>(entry.value))
            }

            return headers
        }

        val ZERO_BYTE = '\u0000'
        val CONTENT_TYPE = "content-type"
    }
}