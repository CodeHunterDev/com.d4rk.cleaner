package com.d4rk.cleaner.compressor.constraint
class Compression {
    internal val constraints: MutableList < Constraint > = mutableListOf()
    fun constraint(constraint: Constraint) {
        constraints.add(constraint)
    }
}