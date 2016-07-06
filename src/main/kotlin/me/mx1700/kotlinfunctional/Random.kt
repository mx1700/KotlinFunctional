package me.mx1700.kotlinfunctional

interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed shr 16).toInt()
        return n to nextRNG
    }
}

fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (v, r) = rng.nextInt()
    return Math.abs(v) to r
}

fun main(args: Array<String>) {
    val s = SimpleRNG(1)
    val (v, s2) = s.nextInt()
    val (v2, s3) = s2.nextInt()
    println(v)
    println(v2)
}