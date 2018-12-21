package com.tlz.audiorecorddemo.extensions

import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.transition.Transition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

/**
 * By tomlezen
 * Create at 2018/12/21
 */

/**
 * 禁止滑动关闭.
 * @receiver BottomSheetDialogFragment
 */
fun BottomSheetDialogFragment.firbidScroll() {
    view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            view?.viewTreeObserver?.removeGlobalOnLayoutListener(this)
            BottomSheetBehavior.from(view?.parent as? View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            this@apply.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                })
            }
        }
    })
}

/**
 * 执行Transition动画.
 * @receiver ViewGroup
 * @param transition Transition?
 */
fun ViewGroup.beginDelayedTransition(transition: Transition? = null) {
    TransitionManager.beginDelayedTransition(this, transition)
}