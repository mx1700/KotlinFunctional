package me.mx1700.kotlinfunctional
import java.util.concurrent.*
import kotlin.reflect.KFunction

//interface Par1<A> {
//
//    fun unit(a: () -> A): Par<A>
//    fun get(a: Par<A>): A
//}

object Par {
    fun <A>unit(a: A): (ExecutorService) -> Future<A> = { es -> UnitFuture(a) }
    fun <A>run(s: ExecutorService, par: (ExecutorService) -> Future<A>): Future<A> = par(s)

    fun <A, B, C>map2(a: (ExecutorService) -> Future<A>, b: (ExecutorService) -> Future<B>, f: (A, B) -> C): (ExecutorService) -> Future<C> = { es ->
        val af = a(es)
        val bf = b(es)
        UnitFuture(f(af.get(), bf.get()))
    }

    fun <A>fork(a: () -> (ExecutorService) -> Future<A>): (ExecutorService) -> Future<A> = { es ->
        es.submit<A> { a()(es).get() }
    }

    fun <A, B>asyncFuture(f: (A) -> B): (A) -> (ExecutorService) -> Future<B> = { a ->
        { es ->
            fork { unit(f(a)) }(es)
        }
    }

    private data class UnitFuture<A>(private val get: A) : Future<A> {

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean  = false

        override fun isDone(): Boolean = true

        override fun get(timeout: Long, unit: TimeUnit): A = get

        override fun get(): A {
            println("get")
            Thread.sleep(2000);
            return get
        }

        override fun isCancelled(): Boolean = false

    }
}

fun main(args: Array<String>) {
    val executorService = Executors.newCachedThreadPool();
    val f = Par.asyncFuture<Int, Int> { it * 2 }(5)(executorService)
    println(f.get())
}


