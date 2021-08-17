package com.d4rk.cleaner.clipboard.tile
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.d4rk.cleaner.clipboard.ACTION_CONTENT
import com.d4rk.cleaner.clipboard.IntentActivity
@RequiresApi(Build.VERSION_CODES.N)
class ContentTileService : TileService() {
    override fun onClick() {
        startActivityAndCollapse(
            IntentActivity.activityIntent(
                this,
                ACTION_CONTENT,
                newTask = true
            )
        )
    }
}