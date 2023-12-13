package com.faceki.android.util

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.faceki.android.R

enum class AnimationTypes {
    SLIDE_ANIM, FADE_SLIDE_ANIM
}

fun NavController.navigateTo(
    @IdRes destinationId: Int,
    args: Bundle? = null,
    @IdRes popUpFragId: Int? = null,
    animType: AnimationTypes? = AnimationTypes.FADE_SLIDE_ANIM,
) {
    val navOptionsBuilder = NavOptions.Builder()

    when (animType) {
        AnimationTypes.SLIDE_ANIM -> navOptionsBuilder.setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)

        AnimationTypes.FADE_SLIDE_ANIM -> navOptionsBuilder.setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.fade_out).setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.slide_out)

        else -> {}
    }

    popUpFragId?.let {
        navOptionsBuilder.setPopUpTo(it, true)
    }
    val navOptions = navOptionsBuilder.build()

    navigate(destinationId, args, navOptions)
}
