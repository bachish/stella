package org.pl

abstract class StellaType

data object Bool : StellaType()

data object Nat : StellaType()

data object StellaUnit : StellaType()

data class Fun(val arg: List<StellaType>, val res: StellaType) : StellaType()

data class Tuple(val items: List<StellaType>) : StellaType()

data class Record(val items: HashMap<String, StellaType>) : StellaType()

data class StellaList(val elemType: StellaType) : StellaType()





