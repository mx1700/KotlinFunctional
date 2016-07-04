package me.mx1700.kotlinfunctional

interface Stream<out T> {
    val head: T?
        get() = when {
            this is Cons -> this.head
            else -> null
        }
}
object Empty : Stream<Nothing> {
    override fun toString() = "empty"
}

var count = 0;
data class Cons<out T>(private val h: () -> T, private val t: () -> Stream<T>) : Stream<T> {
    override val head by lazy { h() }
    val tail by lazy { t() }
    init {
        count++
    }
    override fun toString() = "Stream(${this.head}, ...)"

    fun finalize() {
        count--
    }
}

fun <T>Stream<T>.toList(): List<T> = when {
    this is Cons -> LinkList(this.head, this.tail.toList())
    else -> Nil
}

/**
 * 此种方式只生成一个 stream 对象，直到获取 tail，才会生成下一个
 */
fun <T>List<T>.toStream(): Stream<T> = when {
    this is LinkList -> Cons({ println("get head ${this.head}"); this.head }, { println("get tail"); this.tail.toStream() })
    else -> Empty
}

fun <T>streamOf(vararg elements: T): Stream<T> = listOf(*elements).toStream()

/**
 * 是否存在某元素
 */
fun <T>Stream<T>.exists(p: (T) -> Boolean): Boolean = when {
    this is Cons -> p(this.head) || this.tail.exists(p)
    else -> false
}

/**
 * 返回前 n 个元素
 */
fun <T>Stream<T>.take(n: Int): Stream<T> = when {
    n == 0 -> Empty
    this is Empty -> Empty
    this is Cons && n > 0 -> Cons({ this.head }, { this.tail.take(n - 1) })
    else -> Empty
}

/**
 * drop
 */
fun <T>Stream<T>.drop(n: Int): Stream<T> = when {
    n == 0 -> this
    this is Empty -> Empty
    this is Cons && n > 0 -> {
        val tail = this.tail
        when (tail) {
            is Cons -> Cons({ tail.head }, { tail.tail }).drop(n - 1)
            else -> Empty
        }
    }
    else -> Empty
}

/**
 * 返回从起始元素，连续满足条件的素有元素
 */
fun <T>Stream<T>.takeWhile(f: (T) -> Boolean): Stream<T> = when {
    this is Empty -> Empty
    this is Cons -> {
        if (f(this.head)) {
            Cons({ this.head }, { this.tail.takeWhile(f) })
        } else {
            Empty
        }
    }
    else -> Empty
}

/**
 * foldRight
 */
fun <T, R>Stream<T>.foldRight(init: R, f: (T, () -> R) -> R): R = when(this) {
    is Cons -> f(head, { tail.foldRight(init, f) })
    else -> init
}

/**
 * foldRight2 于上边一个不同的是 初始值 init 是个懒值
 */
fun <T, R>Stream<T>.foldRight2(init: () -> R, f: (T, () -> R) -> R): R = when(this) {
    is Cons -> f(head, { tail.foldRight(init(), f) })
    else -> init()
}

fun <T>Stream<T>.exists2(p: (T) -> Boolean): Boolean = this.foldRight(false, { a, t -> p(a) || t() })
fun <T>Stream<T>.forAll(p: (T) -> Boolean): Boolean = this.foldRight(true, { a, t -> p(a) && t() })
fun <T>Stream<T>.takeWhile2(f: (T) -> Boolean): Stream<T> = this.foldRight<T, Stream<T>>(Empty, { a, t -> if (f(a)) Cons({ a }, t) else Empty })
fun <T>Stream<T>.head(): T? = this.foldRight<T, T?>(null, { a, t -> a })
fun <T, R>Stream<T>.map(f: (T) -> R): Stream<R> = this.foldRight<T, Stream<R>>(Empty, { a, t -> Cons({ f(a) }, t) })
fun <T>Stream<T>.filter(f: (T) -> Boolean): Stream<T> = this.foldRight<T, Stream<T>>(Empty, { a, t -> if (f(a)) Cons({ a }, t) else t().filter { f(it) } })
fun <T>Stream<T>.append(l: Stream<T>): Stream<T> = this.foldRight<T, Stream<T>>(l, { a, t -> Cons({ a }, t) })
//结果正确，但是遍历前就会执行 n 次
fun <T, R>Stream<T>.flatMap(f: (a: T) -> Stream<R>): Stream<R> = this.foldRight<T, Stream<R>>(Empty, { a, l -> f(a).append(l()) })
//比上一个优化，遍历前只执行一次
fun <T, R>Stream<T>.flatMap2(f: (a: T) -> Stream<R>): Stream<R> = this.foldRight<T, Stream<R>>(Empty, { a, l -> f(a).foldRight2({ l() }, { a, l -> Cons( { a }, { l() })}) })
fun <T>Stream<T>.find(f: (T) -> Boolean): T? = this.filter(f).head()

/**
 * 产生一个无限流
 */
fun <T>constant(a: T): Stream<T> {
    var s14: Stream<T>? = null
    s14 = Cons({ a }, { s14?: Empty })
    return s14
}

/**
 * 生成一个从 n 开始的无限流
 */
fun from(start: Int): Stream<Int> = Cons({ start }, { from(start + 1) })

/**
 * 斐波那契数列
 */
fun fibonacci(a: Int = 0, b: Int = 1): Stream<Int> = Cons({ a }, { Cons( { b }, { fibonacci(a + b, a + 2 * b) }) })

/**
 * 根据初始状态和生成下一个状态和值的函数，产生一个无限流
 */
fun <T, R>unfold(first: T, f: (T) -> Pair<T, R>?): Stream<R> {
    val fr = f(first)
    if (fr != null) {
        val (t, r) = fr
        return Cons({ r }, { unfold(t, f) })
    } else {
        return Empty
    }
}

/**
 * 用 unfold 实现斐波那契数列
 */
fun fibs() = unfold(0 to 1, { i ->  (i.second to i.first + i.second) to i.first  })
fun from2(start: Int) = unfold(start, { i -> i + 1 to i })
fun <T>constant2(a: T): Stream<T> = unfold(a, { i -> a to a })
fun <T, R>Stream<T>.map2(f: (T) -> R): Stream<R> = unfold(this, { i -> when(i) {
    is Cons -> i.tail to f(i.head)
    else -> null
} })
fun <T>Stream<T>.take2(n: Int): Stream<T> = unfold(this to n, { s ->
    val (stream, i) = s
    when {
        stream is Cons && i > 0 -> stream.tail to i - 1 to stream.head
        else -> null
    }
})
fun <T>Stream<T>.takeWhile3(f: (T) -> Boolean): Stream<T> = unfold(this, { s ->
    when {
        s is Cons && f(s.head) -> s.tail to s.head
        else -> null
    }
})
fun <T, R>Stream<T>.zipWith(l: Stream<T>, f: (a: T, b: T) -> R): Stream<R> = unfold(this to l, { s ->
    val (s1, s2) = s
    when {
        s1 is Cons && s2 is Cons -> s1.tail to s2.tail to f(s1.head, s2.head)
        else -> null
    }
})
fun <T, T2>Stream<T>.zipAll(l: Stream<T2>): Stream<Pair<T?, T2?>> = unfold(this to l, { s ->
    val (s1, s2) = s
    when {
        s1 is Cons && s2 is Cons -> s1.tail to s2.tail to (s1.head to s2.head)
        s1 is Cons -> s1.tail to s2 to (s1.head to s2.head)
        s2 is Cons -> s1 to s2.tail to (s1.head to s2.head)
        else -> null
    }
});
fun <T>Stream<T>.startWith(l: Stream<T>): Boolean = this.zipWith(l, { l, r -> l to r}).forAll { it.first == it.second }
fun <T>Stream<T>.hasSubSequence(l: Stream<T>): Boolean = when {
    l is Empty -> true
    this is Cons && l is Cons -> if (this.startWith(l)) true else this.tail.hasSubSequence(l)
    else -> false
}
fun <T>Stream<T>.tails(): Stream<Stream<T>> = unfold(this, { s ->  when {
    s is Cons -> s.tail to s
    else -> null
}})

/**
 * 书中方法实现 hasSubSequence
 */
fun <T>Stream<T>.hasSubSequence2(l: Stream<T>): Boolean = this.tails().exists { it.startWith(l) }

/**
 * 有问题，没有复用中间结果
 */
fun <T, R>Stream<T>.scanRight(init: R, f: (T, R) -> R): Stream<R> = this.tails().map { it.foldRight(init, { l, r -> f(l,r()) }) }

/**
 * 只计算一次，中间结果可复用
 * TODO:书上问是否可以用 unfold 实现，我没想出来
 */
fun <T, R>Stream<T>.scanRight2(init: R, f: (T, R) -> R): Stream<R> = this.foldRight<T, Stream<R>>(Empty, { v, _s ->
    val s = _s()
    when {
        s is Cons -> Cons({ f(v, s.head) }, { s })
        else -> Cons({ f(v, init) }, { s })
    }
})

/**
 * 为实现 scanRight 做的实验
 */
fun test():Stream<Int> {
    return listOf(1,2,3,4,5).foldRight<Int, Stream<Int>>(Empty, { s, init ->
        when {
            init is Cons -> Cons({ s + init.head }, { init })
            else -> Cons({ s + 0 }, { init })
        }
    })
}


fun main(args: Array<String>) {

    val stream = Cons({ 1 }, { Cons({ 2 }, { Empty }) })
    println(streamOf(1,2,3).toList())
    println(streamOf(1,2,3,4).exists { it == 2 })
    println(listOf(1,2,3,4,5).toStream().exists { it == 2 })
    println(streamOf(1,2,3,4,5,6).take(2))
    val l7 = streamOf(1,2,3,4,5,6).drop(3).take(2)

    val l5 = streamOf(1,2,3,4,5,6,7,8,9,10).takeWhile({ it <= 3 })
    println("tolist")
    println(l5.toList())

    val s6 = streamOf(1,2,3,4,5)
    println(s6.foldRight(0, { a, s -> a + s() }))

    println(streamOf(1,2,3,4,5).exists2 { it == 2 })
    println(streamOf(1,2,3,4,5).forAll { it < 3 })

    val l6 = streamOf(1,2,3,4,5,6,7,8,9,10).takeWhile2({ it <= 3 })
    println(l6.toList())

    println(streamOf(1,2,3).head())
    println(Empty.head())
    println(streamOf(1,2,3,4,5).map { it + 1 }.take(2).toList())

    streamOf(1,2,3,4,5,6,7).filter { it > 3 && it < 5 }
    val s11 = streamOf(1,2,3).append(streamOf(4,5,6))
    println(s11.toList())

    val s12 = streamOf(1,2,3,4,5).flatMap2 { streamOf(it, it * 2) }
    val l = streamOf(1,2,3,4,5).flatMap2 { streamOf(it, it * 2) }.toList()
    val s13 = streamOf(1,2,3,4).find { it == 3 }

//    无法在初始化的时候自己引用自己
//    var s: Stream<Int> = Cons<Int>({ 1 }, { s })

    var s14: Stream<Int>? = null
    s14 = Cons({ 1 }, { s14?: Empty })

    println(s14.map { it + 1 }.take(10).toList())
    println(constant("aaa").take(3).toList())

    println(from(1).filter { it % 7 == 0 }.take(5).toList())

    println(fibonacci().take(10).toList())
    println(unfold(0, { i ->
        if (i < 26)
            i + 1 to (i + 97).toChar()
        else
            null
    }).take(100).toList())

    println(fibs().take(10).toList())
    println(from2(10).take(10).toList())
    println(streamOf(1,2,3,4,5).map2 { it * 2 }.toList())
    println(streamOf(1,2,3,4,5).take2(3).toList())
    println(streamOf(1,2,3,4,5,6).takeWhile3 { it < 3 }.toList())
    println(streamOf(1,2,3,4,5).zipWith(streamOf(10,11,12),{ a, b -> a + b }).toList())
    println(streamOf(1,2,3,4,5).zipAll(streamOf(10,11,12)).toList())

    println(streamOf(1,2,3,4,5).startWith(streamOf(1,2,3)))
    println(streamOf(1,2,3,4,5).hasSubSequence(streamOf(2,3)))

    println(streamOf(1,2,3).tails().toList())
    println(streamOf(1,2,3,4,5).hasSubSequence2(streamOf(2,3)))
    println(streamOf(1,2,3).scanRight(0, { l, r -> l + r }).toList())

    println(streamOf(1,2,3,4,5).scanRight2(0, { a, b -> a + b }).toList())

}


