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

data class Cons<out T>(private val h: () -> T, private val t: () -> Stream<T>) : Stream<T> {
    override val head by lazy { h() }
    val tail by lazy { t() }

    override fun toString() = "${this.head}, ..."
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
 * foldRight
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


fun main(args: Array<String>) {
    //val stream = Cons({ 1 }, { Cons({ 2 }, { Empty }) })
    //println(streamOf(1,2,3).toList())
    //println(listOf(1,2,3,4,5).toStream().exists { it == 2 })
    //println(streamOf(1,2,3,4,5,6).take(2))
    //val l = streamOf(1,2,3,4,5,6).drop(3).take(2)

//    val l5 = streamOf(1,2,3,4,5,6,7,8,9,10).takeWhile({ it <= 3 })
//    println("tolist")
//    println(l5.toList())

    //val s6 = streamOf(1,2,3,4,5)
    //println(s6.foldRight(0, { a, s -> a + s() }))

//    println(streamOf(1,2,3,4,5).exists2 { it == 2 })
//    println(streamOf(1,2,3,4,5).forAll { it < 3 })

//    val l6 = streamOf(1,2,3,4,5,6,7,8,9,10).takeWhile2({ it <= 3 })
//    println("tolist")
//    println(l6.toList())

    //println(Empty.exists2 { it == 2 })
//    println(streamOf(1,2,3).head())
//    println(Empty.head())
//    println(streamOf(1,2,3,4,5).map { it + 1 }.take(2).toList())

    //streamOf(1,2,3,4,5,6,7).filter { it > 3 && it < 5 }
//    val s11 = streamOf(1,2,3).append(streamOf(4,5,6))
//    println(s11.toList())

    //val s12 = streamOf(1,2,3,4,5).flatMap2 { streamOf(it, it * 2) }
    //println(s12.toList())

    val s13 = streamOf(1,2,3,4).find { it == 3 }

//    println(s2.toList())
    //println(streamOf(1,2,3,4).exists { it == 2 })
//
////    println(stream)
////    println(stream.toList())

}


