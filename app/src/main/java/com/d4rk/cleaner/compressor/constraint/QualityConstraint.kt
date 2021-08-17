package com.d4rk.cleaner.compressor.constraint
import com.d4rk.cleaner.compressor.loadBitmap
import com.d4rk.cleaner.compressor.overWrite
import java.io.File
class QualityConstraint(private val quality: Int): Constraint {
    private
    var isResolved = false

    override fun isSatisfied(imageFile: File): Boolean {
        return isResolved
    }
    override fun satisfy(imageFile: File): File {
        val result = overWrite(imageFile, loadBitmap(imageFile), quality = quality)
        isResolved = true
        return result
    }
}
fun Compression.quality(quality: Int) {
    constraint(QualityConstraint(quality))
}