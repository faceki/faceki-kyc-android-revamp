package com.faceki.android.di

import com.faceki.android.FaceKi
import com.faceki.android.KycResponseHandler
import com.faceki.android.VerificationType

object AppConfig {

    @JvmStatic
    var clientId: String? = null

    @JvmStatic
    var clientSecret: String? = null

    @JvmStatic
    var verificationType: VerificationType? = null

    @JvmStatic
    var kycResponseHandler: KycResponseHandler? = null


    @JvmStatic
    private var colorMap: HashMap<FaceKi.ColorElement, FaceKi.ColorValue>? = null

    @JvmStatic
    private var iconMap: HashMap<FaceKi.IconElement, FaceKi.IconValue>? = null

    @JvmStatic
    private var imageMap: HashMap<String, String>? = null

    @JvmStatic
    fun clear() {
        verificationType = null
        colorMap = null
        iconMap = null
        imageMap = null
    }

    @JvmStatic
    @Synchronized
    fun addImagePath(key: String, imagePath: String) {
        if (imageMap == null) {
            imageMap = HashMap()
        }
        imageMap!![key] = imagePath
    }

    @JvmStatic
    fun getImagePath(key: String): String? {
        return imageMap?.get(key)
    }

    @JvmStatic
    @Synchronized
    fun setCustomColors(newColorMap: HashMap<FaceKi.ColorElement, FaceKi.ColorValue>) {
        colorMap = newColorMap
    }

    @JvmStatic
    @Synchronized
    fun setCustomIcons(newIconMap: HashMap<FaceKi.IconElement, FaceKi.IconValue>) {
        iconMap = newIconMap
    }

    @JvmStatic
    fun getCustomColor(element: FaceKi.ColorElement): FaceKi.ColorValue? {
        return colorMap?.get(element)
    }

    @JvmStatic
    fun getCustomIcon(element: FaceKi.IconElement): FaceKi.IconValue? {
        return iconMap?.get(element)
    }
}
