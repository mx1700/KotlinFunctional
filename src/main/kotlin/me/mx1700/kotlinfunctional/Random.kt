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

fun RNG.nonNegativeInt(): Pair<Int, RNG> {
    val (v, r) = this.nextInt()
    return Math.abs(v) to r
}

fun RNG.double(): Pair<Double, RNG> {
    val (v, r) = this.nonNegativeInt()
    return (v.toDouble() / Int.MAX_VALUE.toDouble()) to r
}

/**
 * 方向是反的
 */
fun RNG.ints(count: Int): Pair<List<Int>, RNG> {
    return (1..count).fold<Int, Pair<List<Int>, RNG>>(Nil to this, { item, i ->
        val (list, r2) = item
        val (v2, r3) = r2.nextInt()
        LinkList(v2, list) to r3
    })
}

fun <A>unit(a: A): (RNG) -> Pair<A, RNG> = { rng -> a to rng }
fun <A, B>map(s: (RNG) -> Pair<A, RNG>, f: (A) -> B): (RNG) -> Pair<B, RNG> = { rng ->
    val (a, r) = s(rng)
    f(a) to r
}
fun double2(rng: RNG): Pair<Double, RNG> = map(RNG::nonNegativeInt, { i -> i.toDouble() / Int.MAX_VALUE.toDouble() })(rng)

fun <A, B, C>map2(ra: (RNG) -> Pair<A, RNG>, rb: (RNG) -> Pair<B, RNG>, f: (A, B) -> C): (RNG) -> Pair<C, RNG> = { rng ->
    val (a, r1) = ra(rng)
    val (b ,r2) = rb(r1)
    f(a, b) to r2
}
fun <A, B>both(ra: (RNG) -> Pair<A, RNG>, rb: (RNG) -> Pair<B, RNG>): (RNG) -> Pair<Pair<A, B>, RNG> =
        map2(ra, rb, { a, b -> a to b })
fun randIntDouble(): (RNG) -> Pair<Pair<Int, Double>, RNG> = both(RNG::nextInt, RNG::double)
fun randDoubleInt(): (RNG) -> Pair<Pair<Double, Int>, RNG> = both(RNG::double, RNG::nextInt)

fun <A>sequence(fs: List<(RNG) -> Pair<A, RNG>>): (RNG) -> Pair<List<A>, RNG> = { rng ->
    fs.foldLeft<(RNG) -> Pair<A, RNG>, Pair<List<A>, RNG>>(Nil to rng, { f, pair ->
        val (list, r) = pair
        val (v, r2) = f(r)
        LinkList(v, list) to r2
    })
}
fun RNG.ints2(count: Int): Pair<List<Int>, RNG> = sequence(fillList(count, RNG::nonNegativeInt))(this)


fun <A, B>flatMap(f: (RNG) -> Pair<A, RNG>, g: (A) -> (RNG) -> Pair<B, RNG>): (RNG) -> Pair<B, RNG> = { rng ->
    val (v, r) = f(rng)
    g(v)(r)
}
fun nonNegativeLessThan(n: Int): (RNG) -> Pair<Int, RNG> = flatMap(RNG::nonNegativeInt, { v ->
    { rng: RNG ->
        val mod = v % n
        if (v + (n - 1) - mod >= 0)
            mod to rng
        else
            nonNegativeLessThan(n)(rng)
    }
})
fun <A, B>map_1(s: (RNG) -> Pair<A, RNG>, f: (A) -> B): (RNG) -> Pair<B, RNG> = flatMap(s, { v ->
    { rng: RNG ->
        f(v) to rng
    }
})
fun <A, B, C>map2_1(ra: (RNG) -> Pair<A, RNG>, rb: (RNG) -> Pair<B, RNG>, f: (A, B) -> C): (RNG) -> Pair<C, RNG> = flatMap(ra, { a ->
    { rng: RNG ->
        val (b, r) = rb(rng)
        f(a, b) to r
    }
})

//---------------------------------

interface Rand<A> {
    fun call(rng: RNG): Pair<A, RNG>
}

object Test2 {
    fun callFun(f: Func) {
        f.call()
    }
}

fun main(args: Array<String>) {
    val r = SimpleRNG(1234567)
    val (v, r2) = r.nextInt()
    val (v2, r3) = r2.nextInt()
    println(v)
    println(v2)

    val (v4, r4) = r2.nonNegativeInt()
    println(v4)

    val (v5, r5) = r4.double()
    println(v5)
    println(double2(r4))

    println(r5.ints(5))

    println(randIntDouble()(r5))
    println(randDoubleInt()(r5))

    println(r.ints2(3))

    println(nonNegativeLessThan(100)(r5))
    println(map_1(nonNegativeLessThan(100), { v -> "val:$v" })(r5))
    println(map2_1(nonNegativeLessThan(100), nonNegativeLessThan(100), { a, b -> a to b })(r5))


    //test<Int>({ r -> 1 to r })
    //test({  })

    /**
     * kotlin 调用 java 的方法,如果方法参数是 SAM , 则可以使用 lambda 函数代替
     * 但是调用 kotlin 的 SAM 方法却不行
     */

    //val int: Rand<Int> = RNG::nextInt
}