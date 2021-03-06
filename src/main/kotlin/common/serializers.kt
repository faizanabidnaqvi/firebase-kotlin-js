package common

import kotlinx.serialization.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@InternalSerializationApi
@ExperimentalSerializationApi
@Suppress("UNCHECKED_CAST")
fun <T: Any> T.firebaseSerializer() = (this::class.serializerOrNull() ?: when(this) {
    is Map<*, *> -> FirebaseMapSerializer()
    is List<*> -> FirebaseListSerializer()
    is Set<*> -> FirebaseListSerializer()
    else -> throw SerializationException("Can't locate argument-less serializer for $this. For generic classes, such as lists, please provide serializer explicitly.")
}) as SerializationStrategy<T>


@InternalSerializationApi
@ExperimentalSerializationApi
class FirebaseMapSerializer : KSerializer<Map<String, Any?>> {

    lateinit var keys: List<String>
    lateinit var map: Map<String, Any?>

    override val descriptor = object : SerialDescriptor {
        override val kind = StructureKind.MAP
        override val serialName = "kotlin.Map<String, Any>"
        override val elementsCount get() = map.size
        override fun getElementIndex(name: String) = keys.indexOf(name)
        override fun getElementName(index: Int) = keys[index]
        override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = false

    }

    override fun deserialize(decoder: Decoder): Map<String, Any?> {
        val collectionDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val map = mutableMapOf<String, Any?>()
        for(index in 0 until collectionDecoder.decodeCollectionSize(descriptor) * 2 step 2) {
//            map[collectionDecoder.decodeNullableSerializableElement(index) as String] =
//                collectionDecoder.decodeNullableSerializableElement(index + 1)
        }
        return map
    }

    override fun serialize(encoder: Encoder, value: Map<String, Any?>) {
        map = value
        keys = value.keys.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, value.size)
        keys.forEachIndexed { index, key ->
            val listValue = map.getValue(key)
            val serializer = (listValue?.firebaseSerializer() ?: Unit.serializer().nullable) as KSerializer<Any?>
            String.serializer().let {
                collectionEncoder.encodeSerializableElement(it.descriptor, index * 2, it, key)
            }
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor, index * 2 + 1, serializer, listValue
            )
        }
        collectionEncoder.endStructure(descriptor)
    }

}

@InternalSerializationApi
@ExperimentalSerializationApi
class FirebaseListSerializer : KSerializer<Iterable<Any?>> {

    lateinit var list: List<Any?>

    override val descriptor = object : SerialDescriptor {
        override val kind = StructureKind.LIST
        override val serialName = "kotlin.List<Any>"
        override val elementsCount get() = list.size
        override fun getElementIndex(name: String) = throw NotImplementedError()
        override fun getElementName(index: Int) = throw NotImplementedError()
        override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = false
    }

    override fun serialize(encoder: Encoder, value: Iterable<Any?>) {
        list = value.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, list.size)
        list.forEachIndexed { index, listValue ->
            val serializer = (listValue?.firebaseSerializer() ?: Unit.serializer().nullable) as KSerializer<Any>
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor, index, serializer, listValue
            )
        }
        collectionEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<Any?> {
        throw NotImplementedError()
//        val collectionDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
//        val list = mutableListOf<Any?>()
//        list.forEachIndexed { index, _ ->
//            list.add(index, collectionDecoder.decodeNullableSerializableElement(index))
//        }
//        return list
    }
}