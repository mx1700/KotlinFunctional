package me.mx1700.kotlinfunctional

interface List<out T>
object Nil : List<Nothing>

data class LinkList<out T>(val head: T, val tail: List<T>) : List<T> {
    override fun toString() = when {
        this.tail is Nil -> "${this.head}"
        else -> "${this.head}, ${this.tail.toString()}"
    }
}

fun <T>listOf(element: T): List<T> = LinkList(element, Nil)

fun <T>listOf(vararg elements: T): List<T> = when (elements.count()) {
    0 -> Nil
    1 -> LinkList(elements[0], Nil)
    else -> elements.foldRight<T, List<T>>(Nil) { t, r -> LinkList(t ,r) }
}


/**
 * 删除第一个元素
 */
fun <T>List<T>.removeHead() = when {
    this is LinkList -> this.tail
    else -> this
}

/**
 * 修改第一个元素
 */
fun <T>List<T>.setHead(v: T) = when {
    this is LinkList -> LinkList(v, this.tail)
    else -> this
}

/**
 * 从左丢弃元素
 */
fun <T>List<T>.dropLeft(n: Int): List<T> = when {
    this is LinkList && n == 1 -> this.tail
    this is LinkList && n > 1 -> this.tail.dropLeft(n - 1)
    else -> this
}

/**
 * 删除列表中前缀全部符合判定的元素
 */
fun <T>List<T>.dropWhile(filter: (T) -> Boolean): List<T> = when {
    this is LinkList && filter(this.head) -> this.tail.dropWhile(filter)
    else -> this
}

/**
 * 追加列表
 */
fun <T>List<T>.append(list: List<T>): List<T> = when {
    this is LinkList -> LinkList(this.head, this.tail.append(list))
    else -> list
}

/**
 * 所有元素求和
 */
fun List<Int>.sum(): Int = when (this) {
    is LinkList -> this.head + this.tail.sum()
    else -> 0
}

/**
 * 右折叠
 */
fun <T, R>List<T>.foldRight(init: R, f: (T, R) -> R): R = when {
    this is LinkList -> f(this.head, this.tail.foldRight(init, f))
    else -> init
}

/**
 * 左折叠
 */
fun <T, R>List<T>.foldLeft(init: R, f: (T, R) -> R): R = when {
    this is LinkList -> this.tail.foldLeft(f(this.head, init), f)
    else -> init
}

/**
 * 长度
 */
fun <T>List<T>.count() = when(this) {
    is LinkList -> this.foldRight(0, { a, b -> b + 1 })
    else -> 0
}

/**
 * 翻转列表
 */
fun <T>List<T>.reversed(): List<T> = this.foldLeft<T, List<T>>(Nil, { a, l -> LinkList(a, l) })

/**
 * 用 foldLeft 实现的 foldRight
 */
fun <T, R>List<T>.foldRight2(init: R, f: (T, R) -> R): R = this.reversed().foldLeft(init, f)

/**
 * map
 */
fun <T, R>List<T>.map(f: (a: T) -> R): List<R> = this.foldRight<T, List<R>>(Nil, { a, l -> LinkList(f(a), l) })

/**
 * filter
 */
//fun <T>List<T>.filter(f: (a: T) -> Boolean): List<T> = this.foldRight<T, List<T>>(Nil, { a, l -> LinkList(a, l).dropWhile { !f(it) } })
fun <T>List<T>.filter(f: (a: T) -> Boolean): List<T> = this.foldRight<T, List<T>>(Nil, { a, l -> if (f(a)) LinkList(a, l) else l })

/**
 * flatMap
 */
fun <T, R>List<T>.flatMap(f: (a: T) -> List<R>): List<R> = this.foldRight<T, List<R>>(Nil, { a, l -> f(a).append(l) })

/**
 * 用 flatMap 实现的 filter
 */
fun <T>List<T>.filter2(f: (a: T) -> Boolean): List<T> = this.flatMap { if (f(it)) listOf(it) else Nil }

/**
 * 拉链操作
 */
fun <T, R>List<T>.zipWith(l: List<T>, f: (a: T, b: T) -> R): List<R> = when {
    this is LinkList && l is LinkList -> LinkList(f(this.head, l.head), this.tail.zipWith(l.tail, f))
    else -> Nil
}

/**
 * 是否包含子序列
 */
fun <T>List<T>.hasSubSequence(l: List<T>): Boolean = when {
    l is Nil -> true
    this is LinkList && l is LinkList -> if (this.startWith(l)) true else this.tail.hasSubSequence(l)
    else -> false
}

/**
 *  是否已指定列表开头
 */
fun <T>List<T>.startWith(l2: List<T>): Boolean = when {
    this is LinkList && l2 is LinkList -> if (this.head == l2.head) this.tail.startWith(l2.tail) else false
    else -> true
}

fun main(args: Array<String>) {
    println(listOf(1, 2, 3))
    println(listOf(1, 2, 3).sum())
    println(listOf(1, 2, 3).removeHead())
    println(listOf(1, 2, 3).setHead(5))
    println(listOf(1, 2, 3, 4, 5).dropLeft(3))
    println(listOf(1, 1, 1, 2, 3, 1, 4, 5).dropWhile { it == 1 })
    println(listOf(1, 2, 3).append(listOf(4, 5, 6)))
    println(listOf(1, 2, 3, 10).foldRight(0, { a, b -> a + b }))
    println(listOf(1, 2, 3, 10).foldRight(1, { a, b -> a * b }))
    println(listOf(1, 2, 3, 10).foldRight2(0, { a, b -> a + b }))
    println(listOf(1, 1, 1).count())
    println(listOf(1, 2, 3, 4, 5).foldLeft(0, { a, b -> a + b }))
    println(listOf(1, 2, 3, 4, 5).reversed())
    println(listOf(1, 2, 3, 4, 5).map { it * 2 })
    println(listOf(1, 2, 3, 4, 5).filter { it % 2 == 0 })
    println(listOf(1, 2, 3).flatMap { listOf(it, it * 2) })
    println(listOf(1, 2, 3, 4, 5).filter2 { it % 2 == 0 })
    println(listOf(1, 2, 3).zipWith(listOf(1, 4, 6, 8), { a, b -> a + b }))
    println(listOf(1, 2, 3, 4, 5, 6).hasSubSequence(listOf(5, 6)))
}
