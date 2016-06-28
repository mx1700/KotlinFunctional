package me.mx1700.kotlinfunctional

interface Stream<out T>
object Empty : Stream<Nothing>

data class Cons<out T>(private val h: () -> T, private val t: () -> Stream<T>) : Stream<T> {
    val head by lazy { h() }
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

fun main(args: Array<String>) {
    val stream = Cons({ println("get"); 1 }, { Cons({ 2 }, { Empty }) })
    println(stream)
    println(stream.toList())
}

