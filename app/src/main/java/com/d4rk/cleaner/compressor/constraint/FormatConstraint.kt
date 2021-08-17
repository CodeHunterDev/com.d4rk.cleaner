package com.d4rk.cleaner.compressor.constraint
import android.graphics.Bitmap
import com.d4rk.cleaner.compressor.compressFormat
import com.d4rk.cleaner.compressor.loadBitmap
import com.d4rk.cleaner.compressor.overWrite
import java.io.File
class FormatConstraint(private val format: Bitmap.CompressFormat): Constraint {
    override fun isSatisfied(imageFile: File): Boolean {
        return format == imageFile.compressFormat()
    }
    override fun satisfy(imageFile: File): File {
        return overWrite(imageFile, loadBitmap(imageFile), format)
    }
}
fun Compression.format(format: Bitmap.CompressFormat) {
    constraint(FormatConstraint(format))
}