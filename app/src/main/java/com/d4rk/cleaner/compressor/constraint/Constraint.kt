package com.d4rk.cleaner.compressor.constraint
import java.io.File
 interface Constraint {
    fun isSatisfied(imageFile: File): Boolean
    fun satisfy(imageFile: File): File
}