@file:Suppress("unused")
package com.d4rk.cleaner.clipboard
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.preference.PreferenceManager
import com.d4rk.cleaner.R
fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.toast(@StringRes id: Int) = toast(getString(id))
fun Context.toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
fun Context.longToast(@StringRes id: Int) = longToast(getString(id))
fun Context.longToast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
@SuppressLint("ObsoleteSdkInt")
fun isKitkatOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
fun isNOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun isOOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun Context.safeContext(): Context =
    takeIf { isNOrLater() && !isDeviceProtectedStorage }?.let {
        ContextCompat.createDeviceProtectedStorageContext(it) ?: it
    } ?: this
fun Context.getSafeSharedPreference(): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(safeContext())
@SuppressLint("UnspecifiedImmutableFlag")
fun Context.pendingActivityIntent(intent: Intent): PendingIntent {
    return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}
fun Context.requestInput(
    @StringRes titleRes: Int,
    inputType: Int = InputType.TYPE_CLASS_TEXT,
    callback: (String) -> Unit
) {
    val dialog = AlertDialog.Builder(this)
        .setTitle(titleRes)
        .setView(R.layout.dialog_input)
        .setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, null)
        .show()
    val input = dialog.findViewById<EditText>(R.id.editDialogInput)!!
    input.inputType = inputType
    dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok)) { _, _ ->
        input.text?.toString()
            .takeIf { it != null && it.isNotEmpty() }
            ?.let { callback.invoke(it) }
    }
}
@SuppressLint("UnspecifiedImmutableFlag")
private fun Context.createShortcut(
    id: String,
    @StringRes shortLabelRes: Int, @StringRes longLabelRes: Int,
    @DrawableRes iconRes: Int, action: String
) {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
        ShortcutManagerCompat.requestPinShortcut(
            this,
            ShortcutInfoCompat.Builder(this, id)
                .setShortLabel(getString(shortLabelRes))
                .setLongLabel(getString(longLabelRes))
                .setDisabledMessage(getString(R.string.clipboard_shortcut_disabled))
                .setIcon(IconCompat.createWithResource(this, iconRes))
                .setIntent(IntentActivity.activityIntent(this, action))
                .build(), PendingIntent.getBroadcast(
                this, 0,
                IntentActivity.activityIntent(this, ACTION_CONTENT), 0
            ).intentSender
        )
    } else {
        toast(R.string.clipboard_shortcut_no_permission)
    }
}