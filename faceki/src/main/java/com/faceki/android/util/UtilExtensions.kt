package com.faceki.android.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.DialogPermissionExplainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

internal fun OkHttpClient.Builder.addLoggingInterceptorIfInDevelopmentMode(): OkHttpClient.Builder {
    addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    return this
}


internal fun Long.hasTokenExpired(token: String?, expiresInSec: Int): Boolean {
    if (token == null) {
        return true
    }

    val expirationTimeMillis = this + TimeUnit.SECONDS.toMillis(expiresInSec.toLong())
    val currentTimeMillis = System.currentTimeMillis()
    return currentTimeMillis >= expirationTimeMillis
}

internal fun Boolean?.isTrue(): Boolean {
    return this ?: false
}

internal fun Context.showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}

internal fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

internal fun Fragment.showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.let {
        Toast.makeText(it, msg, duration).show()
    }
}

internal fun Fragment.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    context?.let {
        Toast.makeText(it, resId, duration).show()
    }
}


fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    } else {
        @Suppress("DEPRECATION") val activeNetworkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION") return activeNetworkInfo.isConnected
    }
}

fun Context.isNetworkNotConnected() = !isNetworkConnected()
fun Fragment.isNetworkNotConnected() = !isNetworkConnected()

fun Fragment.isNetworkConnected(): Boolean {
    return context?.isNetworkConnected() ?: false
}

fun <T> T.isNotNull(): Boolean = this != null
fun <T> T.isNull(): Boolean = this == null

fun FaceKi.ColorValue?.getColorIntOrNull(): Int? {
    return when (this) {
        is FaceKi.ColorValue.IntColor -> this.value
        is FaceKi.ColorValue.StringColor -> try {
            Color.parseColor(this.value)
        } catch (e: IllegalArgumentException) {
            null
        }

        else -> null
    }
}

fun ImageView.loadIcon(
    iconValue: FaceKi.IconValue?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
) {
    val imageSource = when (iconValue) {
        is FaceKi.IconValue.Resource -> {
            this.setImageResource(iconValue.resId)
            onSuccess()
            return
        }

        is FaceKi.IconValue.Url -> iconValue.url
        else -> {
            onFailure(Exception("Invalid icon value"))
            return
        }
    }

    Glide.with(this.context).load(imageSource).listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
        ): Boolean {
            onFailure(e ?: Exception("Image load failed"))
            return false
        }

        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable>?,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            onSuccess()
            return false
        }
    }).into(this)
}

fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

internal fun File.asMultipart(
    partName: String, mediaType: String = "multipart/form-data"
): MultipartBody.Part {
    val requestFile = this.asRequestBody(mediaType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, this.name, requestFile)
}

fun Fragment.runOnUiThread(runnable: Runnable) {
    activity?.runOnUiThread {
        runnable.run()
    }
}


fun Fragment.showPermissionExplainDialog(
    title: String, message: String, onAllow: () -> Unit, onDismiss: (() -> Unit?)? = null
) {
    val context = context ?: return
    val binding = DialogPermissionExplainBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.dialog_permission_explain, null)
    )
    binding.tvTitle.text = title
    binding.tvMessage.text = message

    val alertDialog = MaterialAlertDialogBuilder(context).setView(binding.root).create()

    alertDialog.setOnDismissListener {
        if (onDismiss != null) {
            onDismiss()
        }
    }

    binding.allowBtn.setOnClickListener {
        onAllow()
        alertDialog.dismiss()
    }

    alertDialog.show()
}

fun Fragment.isGranted(permission: String): Boolean {
    return context?.let {
        PermissionChecker.checkSelfPermission(
            it, permission
        ) == PermissionChecker.PERMISSION_GRANTED
    } ?: false
}

fun Activity.isGranted(permission: String): Boolean {
    return PermissionChecker.checkSelfPermission(
        this, permission
    ) == PermissionChecker.PERMISSION_GRANTED
}


fun Fragment.shouldShowRationale(permission: String) =
    shouldShowRequestPermissionRationale(permission)


fun Fragment.handlePermission(
    permission: String,
    onNotGranted: (String) -> Unit,
    onRationaleNeeded: ((String) -> Unit)? = null
) {
    when {
        shouldShowRationale(permission) -> onRationaleNeeded?.invoke(permission)
        !isGranted(permission) -> onNotGranted.invoke(permission)
        else -> Unit
    }
}


fun Fragment.goToAppDetailsSettings(requestCode: Int) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context?.packageName, null)
    }
    activity?.startActivityForResult(intent, requestCode)
}

fun Activity.goToAppDetailsSettings(requestCode: Int) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivityForResult(intent, requestCode)
}

fun File?.delete() {
    this?.apply {
        if (exists()) {
            delete()
        }
    }
}

fun String.asFile(): File {
    return File(this)
}
