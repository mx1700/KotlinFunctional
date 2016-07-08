package me.mx1700.kotlinfunctional

interface Input
object Coin : Input
object Turn : Input

data class Machine(val locked: Boolean, val candies: Int, val coins: Int)

fun Machine.simulate(inputs: List<Input>): (Machine) -> Pair<Int, Machine> = { machine ->
    inputs.map<Input, (Machine) -> Pair<Int, Machine>> { when(it) {
        is Coin -> Machine::coin as (Machine) -> Pair<Int, Machine>     //编译器类型推断有bug，必须加强制转换才能编译通过
        else -> Machine::turn as (Machine) -> Pair<Int, Machine>
    } }.foldLeft(0 to this, { f, m -> f(m.second) })
}

fun <A, S>sequence_m(fs: List<(S) -> Pair<A, S>>): (S) -> Pair<List<A>, S> = { S ->
    fs.foldLeft<(S) -> Pair<A, S>, Pair<List<A>, S>>(Nil to S, { f, pair ->
        val (list, r) = pair
        val (v, r2) = f(r)
        LinkList(v, list) to r2
    })
}

fun Machine.coin() = when {
    this.locked && this.candies > 0 -> 0 to Machine(false, this.candies, this.coins)
    else -> 0 to this
}

fun Machine.turn() = when {
    !this.locked -> 1 to Machine(true, this.candies - 1, this.coins + 1)
    else -> 1 to this
}

fun foo(a: Int): Int {
    return a
}

fun bar(a: Int): Int {
    return a
}

fun main(args: Array<String>) {

    val m = Machine(true, 10, 0)
    val (candies, m1) = m.simulate(listOf(Coin, Turn))(m)
    println(candies to m1)

}