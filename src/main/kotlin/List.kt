interface List<out T>
class Nil<T> : List<T>

data class LinkList<out T>(val head: T, val tail: List<T>) : List<T> {
    override fun toString() = when {
        this.tail is Nil -> "${this.head}"
        else -> "${this.head}, ${this.tail.toString()}"
    }
}

fun <T>listOf(element: T): List<T> = LinkList(element, Nil())

fun <T>listOf(vararg elements: T): List<T> = when (elements.count()) {
    0 -> Nil()
    1 -> LinkList(elements[0], Nil())
    else -> elements.foldRight<T, List<T>>(Nil<T>()) { t, r -> LinkList(t ,r) }
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
    this is LinkList<T> && n == 1 -> this.tail
    this is LinkList<T> && n > 1 -> this.tail.dropLeft(n - 1)
    else -> this
}

/**
 * 删除列表中前缀全部符合判定的元素
 */
fun <T>List<T>.dropWhile(filter: (T) -> Boolean): List<T> = when {
    this is LinkList<T> && filter(this.head) -> this.tail.dropWhile(filter)
    else -> this
}

/**
 * 追加列表
 */
fun <T>List<T>.append(list: List<T>): List<T> = when {
    this is LinkList<T> -> LinkList(this.head, this.tail.append(list))
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
    this is LinkList<T> -> f(this.head, this.tail.foldRight(init, f))
    else -> init
}

/**
 * 左折叠
 */
fun <T, R>List<T>.foldLeft(init: R, f: (T, R) -> R): R = when {
    this is LinkList<T> -> this.tail.foldLeft(f(this.head, init), f)
    else -> init
}

/**
 * 长度
 */
fun <T>List<T>.count() = when(this) {
    is LinkList<T> -> this.foldRight(0, { a, b -> b + 1 })
    else -> 0
}

/**
 * 翻转列表
 */
fun <T>List<T>.reversed(): List<T> = this.foldLeft<T, List<T>>(Nil(), { a, l -> LinkList(a, l) })

/**
 * map
 */
fun <T, R>List<T>.map(f: (a: T) -> R): List<R> = this.foldRight<T, List<R>>(Nil(), { a, l -> LinkList(f(a), l) })

/**
 * filter
 */
fun <T>List<T>.filter(f: (a: T) -> Boolean): List<T> = this.foldRight<T, List<T>>(Nil(), { a, l -> LinkList(a, l).dropWhile { !f(it) } })

/**
 * flatMap
 */
fun <T, R>List<T>.flatMap(f: (a: T) -> List<R>): List<R> = this.foldRight<T, List<R>>(Nil(), { a, l -> f(a).append(l) })


fun main(args: Array<String>) {
    println(listOf(1, 2, 3))
    println(listOf(1, 2, 3).sum())
    println(listOf(1, 2, 3).removeHead())
    println(listOf(1, 2, 3, 4, 5).dropLeft(3))
    println(listOf(1, 1, 1, 2, 3, 1, 4, 5).dropWhile { it == 1 })
    println(listOf(1, 2, 3).append(listOf(4, 5, 6)))
    println(listOf(1, 2, 3, 10).foldRight(0, { a, b -> a + b }))
    println(listOf(1, 2, 3, 10).foldRight(1, { a, b -> a * b }))
    println(listOf(1, 1, 1).count())
    println(listOf(1, 2, 3, 4, 5).foldLeft(0, { a, b -> a + b }))
    println(listOf(1, 2, 3, 4, 5).reversed())
    //TODO: 3.13 & 3.15 未完成
    println(listOf(1, 2, 3, 4, 5).map { it * 2 })
    println(listOf(1, 2, 3, 4, 5).filter { it % 2 == 0 })
    println(listOf(1, 2, 3).flatMap { listOf(it, it * 2) })
    //TODO 3.21 未完成
}
