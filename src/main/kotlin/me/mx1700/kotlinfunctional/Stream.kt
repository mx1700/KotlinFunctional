package me.mx1700.kotlinfunctional

interface Stream<out T> {
    val head: T?
        get() = when {
            this is Cons -> this.head
            else -> null
        }
}
object Empty : Stream<Nothing>

data class Cons<out T>(private val h: () -> T, private val t: () -> Stream<T>) : Stream<T> {
    override val head by lazy { h() }
    val tail by lazy { t() }

    override fun toString() = when {
        this.tail is Empty -> "${this.head}, nil"
        else -> "${this.head}, ..."
    }
}

fun <T>Stream<T>.toList(): List<T> = when {
    this is Cons -> LinkList(this.head, this.tail.toList())
    else -> Nil
}

/**
 * 此种方式会直接生成多个 stream 对象
 */
//fun <T>List<T>.toStream() = this.foldRight<T, Stream<T>>(Empty, { v, s -> println(123); Cons({ v }, { s })})

/**
 * 此种方式只生成一个 stream 对象，直到获取 tail，才会生成下一个
 */
fun <T>List<T>.toStream(): Stream<T> = when {
    this is LinkList -> Cons({ println("get head"); this.head }, { println("get tail"); this.tail.toStream() })
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
 * TODO: drop 会计算 n 个元素，不知道是否符合规范
 */
fun <T>Stream<T>.drop(n: Int): Stream<T> = when {
    n == 0 -> this
    this is Empty -> Empty
    this is Cons && n > 0 -> this.tail.drop(n - 1)
    else -> Empty
}

/**
 * 返回前 n 个元素
 * TODO:未完成
 */
//fun <T>Stream<T>.takeWhile(n: Int, f: (T) -> Boolean): Stream<T> = when {
//    n == 0 -> Empty
//    this is Empty -> Empty
//    this is Cons && n > 0 -> Cons({ this.head }, { this.tail.take(n - 1) })
//    else -> Empty
//}

fun main(args: Array<String>) {
    //val stream = Cons({ 1 }, { Cons({ 2 }, { Empty }) })
    //println(streamOf(1,2,3).toList())
    //println(listOf(1,2,3,4,5).toStream().exists { it == 2 })
    //println(streamOf(1,2,3,4,5,6).take(2))
    val l = streamOf(1,2,3,4,5,6).drop(2)
    println()


//    println(s2.toList())
    //println(streamOf(1,2,3,4).exists { it == 2 })
//
////    println(stream)
////    println(stream.toList())

}


