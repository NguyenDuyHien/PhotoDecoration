package com.hmman.photodecoration.util

class StringUtils {
    companion object {
        fun capitalize (text : String) : String{
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase()
        }
    }
}