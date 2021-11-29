@file:Suppress("unused")

package com.d4rk.cleaner.invalid.ui

import android.R
import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class RaisedViewScrollListener(targetView: View) : RecyclerView.OnScrollListener() {
    private val mTargetView: View = Objects.requireNonNull(targetView)
    private val mStatedElevation: MutableMap<Boolean, Float> = HashMap()
    private val mDuration: Long
    private var mElevationAnimator: Animator? = null
    private var mAnimatorDirection: Boolean? = null
    private fun getElevationInState(state: Boolean): Float {
        return Objects.requireNonNull(mStatedElevation[state])!!
    }
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager: LinearLayoutManager? = if (recyclerView.layoutManager is LinearLayoutManager) {
            recyclerView.layoutManager as LinearLayoutManager?
        } else {
            throw IllegalArgumentException(
                RaisedViewScrollListener::class.java.simpleName
                        + " currently supports LinearLayoutManager only."
            )
        }
        val shouldRaise = layoutManager!!.findFirstCompletelyVisibleItemPosition() > 0
        if (mAnimatorDirection == null || mAnimatorDirection != shouldRaise || mElevationAnimator != null && mElevationAnimator!!.isRunning) {
            mElevationAnimator!!.cancel()
        }
        mAnimatorDirection = shouldRaise
        if (mElevationAnimator == null || !mElevationAnimator!!.isRunning || mTargetView.elevation != getElevationInState(
                shouldRaise
            )
        ) {
            mElevationAnimator = ObjectAnimator.ofFloat(
                mTargetView,
                "elevation",
                getElevationInState(!shouldRaise),
                getElevationInState(shouldRaise)
            ).setDuration(mDuration)
            (mElevationAnimator as ObjectAnimator).start()
        }
    }

    init {
        val context = targetView.context
        mStatedElevation[false] = 0f
        mDuration = context.resources.getInteger(R.integer.config_shortAnimTime).toLong()
    }
}