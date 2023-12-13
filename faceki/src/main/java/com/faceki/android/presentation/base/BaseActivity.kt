package com.faceki.android.presentation.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.faceki.android.util.AnalyzingProgressDialogFragment
import com.faceki.android.util.getContextCompactColor
import kotlinx.coroutines.runBlocking

internal abstract class BaseActivity<T : ViewBinding>(
    private val inflateMethod: (LayoutInflater) -> T,
) : AppCompatActivity() {

    private var _binding: T? = null
    val binding: T get() = _binding!!
    protected var navController: NavController? = null

    private var progressDialog: AnalyzingProgressDialogFragment? = null

    protected var isLoading: Boolean = false

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            initBinding()
            onBackPressedCallback = getOnBackPressedCallback()
            onBackPressedDispatcher.addCallback(onBackPressedCallback)
            getNavControllerViewId()?.let { if (it != -1) navController = findNavController(it) }
            setupViews()
            setupNavigation()
            setupClickListeners()
            observeData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog()
        _binding = null
    }

    private fun initBinding() {
        _binding = inflateMethod.invoke(layoutInflater)
        setContentView(binding.root)
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        super.onBackPressed()
    }

    abstract fun getNavControllerViewId(): Int?


    abstract fun setupViews()

    abstract fun setupNavigation()

    open fun setupClickListeners() {}

    open fun observeData() {}
    open suspend fun setupThemes() {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }


    open fun onCreateBeforeBinding() {}

    abstract fun getOnBackPressedCallback(): OnBackPressedCallback


    private fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = AnalyzingProgressDialogFragment.newInstance().also {
                it.show(supportFragmentManager, "progressDialog")
            }
        }
    }

    // Method to dismiss the progress dialog
    private fun dismissProgressDialog() {
        progressDialog?.let {
            it.dismissAllowingStateLoss()
            progressDialog = null
        }
    }

    // Call this function at the appropriate lifecycle stage or when you need to show the loading state
    internal fun showLoading() {
        runOnUiThread {
            showProgressDialog()
        }
    }

    // Call this function to hide the loading state
    internal fun hideLoading() {
        runOnUiThread {
            dismissProgressDialog()
        }
    }

}

fun Activity.setStatusBarColorRes(@ColorRes color: Int) {
    setStatusBarColor(getContextCompactColor(color))
}

fun Activity.setStatusBarColor(color: Int) {
    window?.statusBarColor = color
}

fun Activity.statusBarColor() = window?.statusBarColor