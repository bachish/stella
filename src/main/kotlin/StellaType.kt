package org.pl

abstract class StellaType

data object Bool : StellaType()

data object Nat : StellaType()

data object StellaUnit : StellaType()

data class Fun(val args: List<StellaType>, val ret: StellaType) : StellaType()

data class Tuple(val items: List<StellaType>) : StellaType(){
    fun checkLength(length: Int, error: TypeCheckingError){
        if (length != items.size) {
            throw error
        }
    }
}

data class Record(val items: Map<String, StellaType>, val labelOrder: List<String>) : StellaType()

data class StellaList(val elemType: StellaType) : StellaType() {
    override fun toString(): String {
        return "[$elemType]"
    }
}

data class Sum(val left: StellaType, val right: StellaType) : StellaType()

data class Variant(val labelToType: Map<String, StellaType?>, val labelOrder: List<String>) : StellaType() {
    fun getType(label: String, error: TypeCheckingError): StellaType? {
        if (!labelToType.keys.contains(label)) {
            throw error
        }
        return labelToType[label]
    }
}

data class Ref(val type: StellaType) : StellaType()

object Top: StellaType()
object Bot: StellaType()


