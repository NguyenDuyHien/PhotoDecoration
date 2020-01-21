package com.hmman.photodecoration.util

open class SingletonHolder<out T, in A>(private val constructor: (A) -> T) {
    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A?): T {
        return when {
            instance != null -> instance!!
            else -> synchronized(this) {

                if (instance == null) {
                    if (arg == null) throw Throwable("Need pass not null argument for constructor at first time call. - TaiLV")
                    instance = constructor(arg)
                }
                instance!!
            }
        }
    }
}
