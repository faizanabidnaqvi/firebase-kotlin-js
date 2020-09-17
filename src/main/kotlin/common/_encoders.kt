package common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlin.js.json

//js related only. In future we might move it to js source
@ExperimentalSerializationApi
fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder = when(descriptor.kind as StructureKind) {
    StructureKind.LIST -> Array<Any?>(descriptor.elementsCount) { null }
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> it[index] = value } }
    StructureKind.MAP -> {
        val map = json()
        var lastKey: String = ""
        value = map
        FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> if(index % 2 == 0) lastKey = value as String else map[lastKey] = value }
    }
    StructureKind.CLASS,  StructureKind.OBJECT -> json()
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> it[descriptor.getElementName(index)] = value } }
}