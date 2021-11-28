package com.d4rk.cleaner.invalid.ui
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.d4rk.cleaner.invalid.loader.InvalidImagesLoader
import com.d4rk.cleaner.invalid.task.CleanFilesTask
import androidx.annotation.RequiresApi
import android.os.Build
import android.os.Bundle
import com.d4rk.cleaner.R
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import android.view.WindowInsets
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.Manifest.permission
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import android.content.Intent
import android.content.Loader
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.d4rk.cleaner.invalid.model.MediaItem
import java9.util.stream.StreamSupport
import java.lang.IllegalStateException
import java.util.ArrayList
class InvalidActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mFirstStepView: View? = null
    private var mProgressView: View? = null
    private var mEmptyView: View? = null
    private var mListContainer: View? = null
    private var mActionButton: TextView? = null
    private var mResetButton: View? = null
    private var mProgressText: TextView? = null
    private var mAdapter: MediaItemsAdapter? = null
    private var mState = STATE_NORMAL
    private var mItems = ArrayList<MediaItem>()
    private var mInvalidLoader: InvalidImagesLoader? = null
    private var mCleanFilesTask: CleanFilesTask? = null
    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invalid)
        mRecyclerView = findViewById(android.R.id.list)
        mFirstStepView = findViewById(R.id.first_step_view)
        mProgressView = findViewById(R.id.progress_view)
        mActionButton = findViewById(R.id.action_button)
        mEmptyView = findViewById(R.id.empty_view)
        mListContainer = findViewById(R.id.list_container)
        mResetButton = findViewById(R.id.reset_button)
        mProgressText = findViewById(R.id.progress_text)
        val firstStepText = findViewById<TextView>(R.id.first_step_text)
        firstStepText.text = HtmlCompat.fromHtml(getString(R.string.first_step_tips), 0)
        setupRecyclerView()
        setupViewCallbacks()
        mInvalidLoader = InvalidImagesLoader(this)
        mInvalidLoader!!.registerListener(0) { _: Loader<List<MediaItem>?>?, data: List<MediaItem>? ->
            if (data == null) {
                mState = STATE_NORMAL
                updateViewsByState()
                return@registerListener
            }
            if (mState != STATE_SCANNING) {
                mState = STATE_NORMAL
                updateViewsByState()
                return@registerListener
            }
            mItems = ArrayList(data)
            mState = STATE_CHOOSING
            updateViewsByState()
        }
        if (savedInstanceState != null) {
            mState = savedInstanceState.getInt(EXTRA_STATE)
            val items: ArrayList<MediaItem> = savedInstanceState.getParcelableArrayList(
                EXTRA_ITEMS
            )!!
            mItems = items
        } else {
            mState = STATE_NORMAL
            mItems.clear()
        }
        updateViewsByState()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_STATE, mState)
        outState.putParcelableArrayList(EXTRA_ITEMS, mItems)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mInvalidLoader != null && mInvalidLoader!!.isStarted) {
            mInvalidLoader!!.cancelLoad()
        }
        if (mCleanFilesTask != null && !mCleanFilesTask!!.isCancelled) {
            mCleanFilesTask!!.cancel(true)
        }
    }
    private fun setupRecyclerView() {
        if (mAdapter == null) {
            mAdapter = MediaItemsAdapter()
            mAdapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onChanged() {
                    mEmptyView!!.visibility =
                        if (mAdapter!!.itemCount == 0) View.VISIBLE else View.GONE
                }
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                    onChanged()
                }
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                    onChanged()
                }
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    onChanged()
                }
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    onChanged()
                }
                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    onChanged()
                }
            })
        }
        mRecyclerView!!.adapter = mAdapter
        val items: List<MediaItem> = ArrayList()
        mAdapter!!.submitList(items)
    }
    private fun setupViewCallbacks() {
        val rootView = findViewById<View>(R.id.root_view)
        rootView.setOnApplyWindowInsetsListener { _: View?, insets: WindowInsets -> insets.consumeSystemWindowInsets() }
        mResetButton!!.setOnClickListener {
            mState = STATE_NORMAL
            updateViewsByState()
            mItems.clear()
        }
    }
    private fun setActionButton(@DrawableRes iconRes: Int, @StringRes textRes: Int) {
        mActionButton!!.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
        mActionButton!!.setText(textRes)
    }
    fun onActionButtonClick(view: View?) {
        when (mState) {
            STATE_NORMAL -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            requestPermissions(
                                arrayOf(
                                    permission.READ_EXTERNAL_STORAGE,
                                    permission.WRITE_EXTERNAL_STORAGE,
                                    permission.MANAGE_EXTERNAL_STORAGE
                                ), 1
                            )
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (!Environment.isExternalStorageManager()) {
                                Toast.makeText(this, "Permission needed!", Toast.LENGTH_LONG).show()
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                        }
                        return
                    }
                }
                mState = STATE_SCANNING
                mInvalidLoader!!.startLoading()
            }
            STATE_SCANNING -> {
                mInvalidLoader!!.cancelLoad()
                mState = STATE_NORMAL
                updateViewsByState()
            }
            STATE_CHOOSING -> {
                val count = StreamSupport.stream(mItems)
                    .filter { item: MediaItem -> item.isChecked }
                    .count().toInt()
                if (count != 0) {
                    ConfirmCleanDialog.newInstance(count)
                        .show(fragmentManager, "confirm_clean")
                }
            }
            STATE_CLEANING -> {
                if (mCleanFilesTask != null && !mCleanFilesTask!!.isCancelled) {
                    mCleanFilesTask!!.cancel(true)
                }
                mState = STATE_NORMAL
                updateViewsByState()
            }
            else -> {
                throw IllegalStateException("Unsupported state = $mState")
            }
        }
        updateViewsByState()
    }
    private fun updateViewsByState() {
        when (mState) {
            STATE_NORMAL -> {
                setActionButton(R.drawable.ic_image_search, R.string.action_scan)
            }
            STATE_SCANNING -> {
                mProgressText!!.setText(R.string.progress_text_scanning)
                setActionButton(R.drawable.ic_stop, R.string.action_stop)
            }
            STATE_CHOOSING -> {
                mAdapter!!.submitList(ArrayList(mItems))
                setActionButton(R.drawable.ic_trash, R.string.clean)
            }
            STATE_CLEANING -> {
                mProgressText!!.setText(R.string.progress_text_cleaning)
                setActionButton(R.drawable.ic_stop, R.string.action_stop)
            }
            else -> {
                throw IllegalStateException("Unsupported state = $mState")
            }
        }
        mListContainer!!.visibility = LIST_VISIBILITY[mState]
        mFirstStepView!!.visibility = FIRST_STEP_VISIBILITY[mState]
        mProgressView!!.visibility = PROGRESS_VISIBILITY[mState]
        mResetButton!!.visibility = RESET_BUTTON_VISIBILITY[mState]
    }
    private fun startCleaning() {
        if (mState != STATE_CHOOSING) {
            return
        }
        mCleanFilesTask =
            CleanFilesTask(this) { deletedCount: Int -> onFinishedClean(deletedCount) }
        mCleanFilesTask!!.execute(
            StreamSupport.stream(mItems).filter { item: MediaItem -> item.isChecked })
        mState = STATE_CLEANING
        updateViewsByState()
    }
    private fun onFinishedClean(deletedCount: Int) {
        FinishedCleanDialog.newInstance(deletedCount)
            .show(fragmentManager, "finished_clean")
        mState = STATE_NORMAL
        updateViewsByState()
    }
    class ConfirmCleanDialog : DialogFragment() {
        private var mCount = 0
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (arguments != null) {
                mCount = arguments.getInt(EXTRA_COUNT)
            }
        }
        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
            return AlertDialog.Builder(activity)
                .setTitle(R.string.clean_confirm_title)
                .setMessage(getString(R.string.dialog_confirm_clean_message, mCount))
                .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                    val activity = activity as InvalidActivity
                    activity.startCleaning()
                }
                .setNegativeButton(android.R.string.no, null)
                .create()
        }
        companion object {
            private const val EXTRA_COUNT = "count"
            fun newInstance(itemCount: Int): ConfirmCleanDialog {
                val dialog = ConfirmCleanDialog()
                val arguments = Bundle()
                arguments.putInt(EXTRA_COUNT, itemCount)
                dialog.arguments = arguments
                return dialog
            }
        }
    }
    class FinishedCleanDialog : DialogFragment() {
        private var mCount = 0
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (arguments != null) {
                mCount = arguments.getInt(EXTRA_COUNT)
            }
        }
        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
            return AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_finished_clean_title)
                .setMessage(getString(R.string.dialog_finished_clean_message, mCount))
                .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                    val activity = activity as InvalidActivity
                    activity.startCleaning()
                }
                .setNegativeButton(android.R.string.no, null)
                .create()
        }
        companion object {
            private const val EXTRA_COUNT = "count"
            fun newInstance(itemCount: Int): FinishedCleanDialog {
                val dialog = FinishedCleanDialog()
                val arguments = Bundle()
                arguments.putInt(EXTRA_COUNT, itemCount)
                dialog.arguments = arguments
                return dialog
            }
        }
    }
    companion object {
        private val TAG = InvalidActivity::class.java.simpleName
        private val EXTRA_STATE = "$TAG.extra.STATE"
        private val EXTRA_ITEMS = "$TAG.extra.ITEMS"
        private const val STATE_NORMAL = 0
        private const val STATE_SCANNING = 1
        private const val STATE_CHOOSING = 2
        private const val STATE_CLEANING = 3
        private val FIRST_STEP_VISIBILITY = intArrayOf(View.VISIBLE, View.GONE, View.GONE, View.GONE)
        private val PROGRESS_VISIBILITY = intArrayOf(View.GONE, View.VISIBLE, View.GONE, View.VISIBLE)
        private val LIST_VISIBILITY = intArrayOf(View.GONE, View.GONE, View.VISIBLE, View.GONE)
        private val RESET_BUTTON_VISIBILITY = intArrayOf(View.GONE, View.GONE, View.VISIBLE, View.GONE)
    }
}