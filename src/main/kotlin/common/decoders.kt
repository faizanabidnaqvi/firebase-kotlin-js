package common

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

@ExperimentalSerializationApi
@Suppress("UNCHECKED_CAST")
inline fun <reified T> decode(value: Any?): T {
    val strategy = EmptySerializersModule.getContextual(T::class as KClass<*>)
    return decode(strategy as DeserializationStrategy<T>, value)
}

fun <T> decode(strategy: DeserializationStrategy<T>, value: Any?): T {
    require(value != null || strategy.descriptor.isNullable) { "Value was null for non-nullable type ${strategy.descriptor.serialName}" }
    return FirebaseDecoder(value).decodeSerializableValue(strategy)
}

//expect fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder

class FirebaseDecoder(internal val value: Any?) : Decoder {
    @ExperimentalSerializationApi
    override val serializersModule: SerializersModule
        get() = EmptySerializersModule

    @ExperimentalSerializationApi
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = structureDecoder(descriptor)

    override fun decodeBoolean() = decodeBoolean(value)

    override fun decodeByte() = decodeByte(value)

    override fun decodeChar() = decodeChar(value)

    override fun decodeDouble() = decodeDouble(value)

    @ExperimentalSerializationApi
    override fun decodeEnum(enumDescriptor: SerialDescriptor) = decodeEnum(value, enumDescriptor)

    override fun decodeFloat() = decodeFloat(value)

    override fun decodeInt() = decodeInt(value)

    override fun decodeLong() = decodeLong(value)

    @ExperimentalSerializationApi
    override fun decodeNotNullMark() = decodeNotNullMark(value)

    @ExperimentalSerializationApi
    override fun decodeNull() = decodeNull(value)

    override fun decodeShort() = decodeShort(value)

    override fun decodeString() = decodeString(value)

}

open class FirebaseCompositeDecoder constructor(
    private val size: Int,
    private val get: (descriptor: SerialDescriptor, index: Int) -> Any?
) : CompositeDecoder {
    override val serializersModule: SerializersModule
        get() = EmptySerializersModule

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor) = size

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T = deserializer.deserialize(FirebaseDecoder(get(descriptor, index)))

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? = if(decodeNotNullMark(get(descriptor, index))) decodeSerializableElement(descriptor, index, deserializer) else decodeNull(get(descriptor, index))

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int) = decodeBoolean(get(descriptor, index))

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int) = decodeByte(get(descriptor, index))

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int) = decodeChar(get(descriptor, index))

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int) = decodeDouble(get(descriptor, index))

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw NotImplementedError()

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int) = decodeFloat(get(descriptor, index))

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int) = decodeInt(get(descriptor, index))

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int) = decodeLong(get(descriptor, index))

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int) = decodeShort(get(descriptor, index))

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int) = decodeString(get(descriptor, index))

    override fun endStructure(descriptor: SerialDescriptor) {
    }

}

class FirebaseClassDecoder(
    size: Int,
    private val containsKey: (name: String) -> Boolean,
    get: (descriptor: SerialDescriptor, index: Int) -> Any?
) : FirebaseCompositeDecoder(size, get) {
    private var index: Int = 0

    override fun decodeSequentially() = false

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        (index until descriptor.elementsCount)
            .firstOrNull { !descriptor.isElementOptional(it) || containsKey(descriptor.getElementName(it)) }
            ?.also { index = it + 1 }
            ?: CompositeDecoder.DECODE_DONE
}

private fun decodeString(value: Any?) = value.toString()

private fun decodeDouble(value: Any?) = when(value) {
    is Number -> value.toDouble()
    is String -> value.toDouble()
    else -> throw SerializationException("Expected $value to be double")
}

private fun decodeLong(value: Any?) = when(value) {
    is Number -> value.toLong()
    is String -> value.toLong()
    else -> throw SerializationException("Expected $value to be long")
}

private fun decodeByte(value: Any?) = when(value) {
    is Number -> value.toByte()
    is String -> value.toByte()
    else -> throw SerializationException("Expected $value to be byte")
}

private fun decodeFloat(value: Any?) = when(value) {
    is Number -> value.toFloat()
    is String -> value.toFloat()
    else -> throw SerializationException("Expected $value to be float")
}

private fun decodeInt(value: Any?) = when(value) {
    is Number -> value.toInt()
    is String -> value.toInt()
    else -> throw SerializationException("Expected $value to be int")
}

private fun decodeShort(value: Any?) = when(value) {
    is Number -> value.toShort()
    is String -> value.toShort()
    else -> throw SerializationException("Expected $value to be short")
}

private fun decodeBoolean(value: Any?) = value as Boolean

private fun decodeChar(value: Any?) = when(value) {
    is Number -> value.toChar()
    is String -> value[0]
    else -> throw SerializationException("Expected $value to be char")
}

@ExperimentalSerializationApi
private fun decodeEnum(value: Any?, enumDescriptor: SerialDescriptor) = when(value) {
    is Number -> value.toInt()
    is String -> enumDescriptor.getElementIndex(value)
    else -> throw SerializationException("Expected $value to be enum")
}

private fun decodeNotNullMark(value: Any?) = value != null

private fun decodeNull(value: Any?) = value as Nothing?

private fun decodeUnit(value: Any?) = value as Unit