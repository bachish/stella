package org.pl

abstract class StellaType

data object Bool : StellaType()

data object Nat : StellaType()

data object StellaUnit : StellaType()

data class Fun(val args: List<StellaType>, val ret: StellaType) : StellaType()

data class Tuple(val items: List<StellaType>) : StellaType()

data class Record(val items: Map<String, StellaType>, val labelOrder: List<String>) : StellaType()

data class StellaList(val elemType: StellaType) : StellaType()

data class Sum(val left: StellaType, val right: StellaType) : StellaType()

data class Variant(val labelToType: Map<String, StellaType?>) : StellaType()





