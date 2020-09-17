package common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlin.js.Json

@ExperimentalSerializationApi
fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder = when(descriptor.kind as StructureKind) {
    StructureKind.CLASS, StructureKind.OBJECT -> (value as Json).let { json ->
        FirebaseClassDecoder(js("Object").keys(value).length as Int, { json[it] != undefined }) {
                desc, index -> json[desc.getElementName(index)]
        }
    }
    StructureKind.LIST -> (value as Array<*>).let {
        FirebaseCompositeDecoder(it.size) { _, index -> it[index] }
    }
    StructureKind.MAP -> (js("Object").entries(value) as Array<Array<Any>>).let {
        FirebaseCompositeDecoder(it.size) { _, index -> it[index/2].run { if(index % 2 == 0) get(0) else get(1) } }
    }
}