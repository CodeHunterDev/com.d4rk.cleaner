package com.d4rk.cleaner.invalid.model
import android.database.Cursor
import android.os.Parcelable
import android.os.Parcel
import android.provider.MediaStore.Images.ImageColumns
import java.util.*
class MediaItem : Parcelable {
    @JvmField
    var id: Long = 0
    @JvmField
    var path: String? = null
    @JvmField
    var displayName: String? = null
    var addTime: Date? = null
    @JvmField
    var isChecked = false
    constructor()
    private constructor(`in`: Parcel) {
        id = `in`.readLong()
        path = `in`.readString()
        displayName = `in`.readString()
        val addTimeLong = `in`.readLong()
        if (addTimeLong > 0) {
            addTime = Date(addTimeLong)
        }
        isChecked = `in`.readByte().toInt() != 0
    }
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is MediaItem) return false
        return other.id == id
    }
    override fun toString(): String {
        return "MediaItem[id=" + id + ", " +
                "path=" + path + ", " +
                "displayName=" + displayName + ", " +
                "addTime=" + addTime + ", " +
                "isChecked=" + isChecked +
                "]"
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(path)
        dest.writeString(displayName)
        dest.writeLong(if (addTime != null) addTime!!.time else 0)
        dest.writeByte(if (isChecked) 1.toByte() else 0.toByte())
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (displayName?.hashCode() ?: 0)
        result = 31 * result + (addTime?.hashCode() ?: 0)
        result = 31 * result + isChecked.hashCode()
        return result
    }
    companion object {
        @JvmStatic
        fun fromCursor(cur: Cursor): MediaItem {
            val item = MediaItem()
            var index: Int
            if (cur.getColumnIndex(ImageColumns._ID).also { index = it } != -1) {
                item.id = cur.getLong(index)
            }
            if (cur.getColumnIndex(ImageColumns.DATA).also { index = it } != -1) {
                item.path = cur.getString(index)
            }
            if (cur.getColumnIndex(ImageColumns.DISPLAY_NAME).also { index = it } != -1) {
                item.displayName = cur.getString(index)
            }
            if (cur.getColumnIndex(ImageColumns.DISPLAY_NAME).also { index = it } != -1) {
                item.addTime = Date(cur.getLong(index))
            }
            return item
        }
    }
}