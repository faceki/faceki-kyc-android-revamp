package com.faceki.android.util

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.DisplayCutout
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar


fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}

fun View.hideSoftKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun Context.fromDpToPx(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

fun Context.getContextCompactDrawable(@DrawableRes drawableId: Int): Drawable {
    return ContextCompat.getDrawable(this, drawableId)!!
}

fun Context.getContextCompactColor(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.getContextCompactColorStateList(@ColorRes colorRes: Int): ColorStateList {
    return ContextCompat.getColorStateList(this, colorRes)!!
}

fun View.setBackgroundTintColor(backgroundTintColor: Int?) {
    if (backgroundTintColor != null && backgroundTintColor != -1) this.backgroundTintList =
        this.context.getContextCompactColorStateList(backgroundTintColor)
}


/** Milliseconds used for UI animations */
const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 100L

/**
 * Simulate a button click, including a small delay while it is being pressed to trigger the
 * appropriate animations.
 */
fun ImageButton.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}

/** Pad this view with the insets provided by the device cutout (i.e. notch) */
@RequiresApi(Build.VERSION_CODES.P)
fun View.padWithDisplayCutout() {

    /** Helper method that applies padding from cutout's safe insets */
    fun doPadding(cutout: DisplayCutout) = setPadding(
        cutout.safeInsetLeft, cutout.safeInsetTop, cutout.safeInsetRight, cutout.safeInsetBottom
    )

    // Apply padding using the display cutout designated "safe area"
    rootWindowInsets?.displayCutout?.let { doPadding(it) }

    // Set a listener for window insets since view.rootWindowInsets may not be ready yet
    setOnApplyWindowInsetsListener { _, insets ->
        insets.displayCutout?.let { doPadding(it) }
        insets
    }
}

/** Same as [AlertDialog.show] but setting immersive mode in the dialog's window */
fun AlertDialog.showImmersive() {
    // Set the dialog to not focusable
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    )

    // Make sure that the dialog's window is in full screen
    window?.let { hideSystemUI(it) }

    // Show the dialog while still in immersive mode
    show()

    // Set the dialog to focusable again
    window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
}

private fun hideSystemUI(window: Window) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        // For api level 29 and before, set deprecated systemUiVisibility to the combination of all
        // flags required to put activity into immersive mode.
        @Suppress("DEPRECATION") val fullscreenFlags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = fullscreenFlags
    }
}

fun Fragment.showSnackBar(
    message: String,
    actionText: String = "OK",
    duration: Int = Snackbar.LENGTH_LONG
) {
    runOnUiThread {
        activity?.window?.decorView?.findViewById<View?>(R.id.content)?.apply {
            Snackbar.make(this, message, duration).apply {
                setAction(actionText) {
                    dismiss()
                }
                show()
            }
        }
    }
}


internal fun Fragment.showMaterialDialog(message: String, onTryAgainClick: () -> Unit) {
    MaterialAlertDialogBuilder(requireContext()).setMessage(message)
        .setPositiveButton("Try Again") { dialog, _ ->
            onTryAgainClick.invoke()
            dialog.dismiss()
        }.setCancelable(false).show()
}
