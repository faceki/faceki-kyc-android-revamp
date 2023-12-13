package com.faceki.android.presentation.camera

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import com.bumptech.glide.Glide
import com.faceki.android.FaceKi
import com.faceki.android.R
import com.faceki.android.databinding.FragmentCameraBinding
import com.faceki.android.di.AppConfig
import com.faceki.android.domain.model.CaptureItem
import com.faceki.android.presentation.LottieAnimationType
import com.faceki.android.presentation.base.BaseFragment
import com.faceki.android.presentation.image.ImageQualityViewModel
import com.faceki.android.presentation.image.ImageQualityViewModelFactory
import com.faceki.android.presentation.welcome.TokenViewModel
import com.faceki.android.presentation.welcome.TokenViewModelFactory
import com.faceki.android.util.ANIMATION_FAST_MILLIS
import com.faceki.android.util.ANIMATION_SLOW_MILLIS
import com.faceki.android.util.AnalyzingProgressDialogFragment
import com.faceki.android.util.Constants
import com.faceki.android.util.FileManager
import com.faceki.android.util.delete
import com.faceki.android.util.getColorIntOrNull
import com.faceki.android.util.goToAppDetailsSettings
import com.faceki.android.util.handlePermission
import com.faceki.android.util.isGranted
import com.faceki.android.util.isNetworkNotConnected
import com.faceki.android.util.isNotNull
import com.faceki.android.util.isTrue
import com.faceki.android.util.loadIcon
import com.faceki.android.util.makeGone
import com.faceki.android.util.makeVisible
import com.faceki.android.util.navigateTo
import com.faceki.android.util.runOnUiThread
import com.faceki.android.util.showMaterialDialog
import com.faceki.android.util.showPermissionExplainDialog
import com.faceki.android.util.showSnackBar
import com.faceki.android.util.showToast
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * A simple [Fragment] subclass.
 * Camera Fragment for this app. Implements all camera operations including:
 * - Viewfinder
 * - Photo taking
 */

internal class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate),
    View.OnClickListener {

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var windowInfoTracker: WindowInfoTracker

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /** Blocking camera operations are performed using this executor */
    private var cameraExecutor: ExecutorService? = null

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Timber.d("Rotation changed: " + view.display.rotation)
                imageCapture?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    private var captureItems: ArrayList<CaptureItem> = arrayListOf()
    private var capturePosition: Int = -1

    private var isCaptureSelfie: Boolean = false
    private var selfieCaptureItem: CaptureItem? = null

    private var capturedImage: File? = null

    private var token: String? = null

    private var loadingDialog: AnalyzingProgressDialogFragment? = null

    private val tokenViewModel: TokenViewModel by viewModels {
        TokenViewModelFactory()
    }

    private val imageQualityViewModel: ImageQualityViewModel by viewModels {
        ImageQualityViewModelFactory()
    }

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>


    private enum class InternalCameraState {
        CAMERA_ON, // State for capturing photos using the camera
        IMAGE_DISPLAY // State for displaying a captured image
    }

    private var currentCameraState: InternalCameraState = InternalCameraState.CAMERA_ON


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->

            isCaptureSelfie = bundle.getBoolean(Constants.ARG_IS_CAPTURE_SELFIE, false)

            if (isCaptureSelfie) {
                selfieCaptureItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(
                        Constants.ARG_CAPTURE_SINGLE_DOCUMENT, CaptureItem::class.java
                    )
                } else {
                    bundle.getParcelable(Constants.ARG_CAPTURE_SINGLE_DOCUMENT)
                }!!
                return@let
            }

            capturePosition = bundle.getInt(Constants.ARG_CAPTURE_POSITION, -1)
            captureItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelableArrayList(
                    Constants.ARG_CAPTURE_DOCUMENTS, CaptureItem::class.java
                )
            } else {
                bundle.getParcelableArrayList(Constants.ARG_CAPTURE_DOCUMENTS)
            } ?: arrayListOf()

            cameraPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    checkCameraPermission()
                }
            }

        }
    }

    override fun setupViews() {

        binding.ivOverlay.setImageResource(
            if (isCaptureSelfie) R.drawable.oval_overlay else R.drawable.rectangle_overlay
        )

        setupToolbar()

        when {
            isCaptureSelfie -> {
                lensFacing = CameraSelector.LENS_FACING_FRONT
                binding.tvImgSide.setText(R.string.take_a_selfie)
            }

            else -> {
                when {
                    captureItems[capturePosition] is CaptureItem.DocumentFrontImage -> {
                        binding.tvImgSide.setText(R.string.front_side)
                    }

                    else -> {
                        binding.tvImgSide.setText(R.string.back_side)
                    }
                }
            }
        }

        handleSwitchState(currentCameraState)
    }

    private fun startCamera() {
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)

        // Initialize WindowManager to retrieve display metrics
        windowInfoTracker = WindowInfoTracker.getOrCreate(binding.root.context)

        // Wait for the views to be properly laid out
        binding.viewFinder.post {

            // Keep track of the display in which this view is attached
            displayId = binding.viewFinder.display.displayId

            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            lifecycleScope.launch {
                setUpCamera()
            }
        }
    }

    override fun observeData() {
        lifecycleScope.launch {
            imageQualityViewModel.screenState.flowWithLifecycle(
                lifecycle, Lifecycle.State.STARTED
            ).collect { state ->
                if (state.isLoading) showLoading() else hideLoading()

                if (state.isSuccess != null) {
                    if (state.isSuccess) {
                        handleSwitchState(InternalCameraState.IMAGE_DISPLAY)
                    } else {
                        state.errorMessage?.let { showSnackBar(message = it) }
                        capturedImage.delete()
                        capturedImage = null
                    }
                }
            }
        }

        lifecycleScope.launch {
            tokenViewModel.screenState.flowWithLifecycle(
                lifecycle, Lifecycle.State.STARTED
            ).collect { state ->
                if (state.isLoading) showLoading() else hideLoading()
                if (state.isSuccess.isTrue() && state.token.isNotNull().isTrue()) {

                    val captureItem = if (isCaptureSelfie) {
                        selfieCaptureItem
                    } else {
                        captureItems[capturePosition]
                    }!!

                    if (token == null || token != state.token) {

                        token = state.token
                        if (captureItem is CaptureItem.Selfie) {
                            navigateToNextAction()
                        } else {
                            imageQualityViewModel.checkImageQuality(capturedImage!!)
                        }
                    }
                }
            }
        }

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>(Constants.ARG_VERIFICATION_RESPONSE)
            ?.observe(viewLifecycleOwner) {
                it?.let {
                    if (it.isNotEmpty()) {
                        showMaterialDialog(message = it, onTryAgainClick = {
                            capturedImage.delete()
                            capturedImage = null
                        })
                    }
                }
            }


    }

    private fun getToken() {
        lifecycleScope.launch {
            token = null
            if (isNetworkNotConnected()) {
                showToast("Please connect to internet")
                return@launch
            }
            val clientId = AppConfig.clientId!!
            val clientSecret = AppConfig.clientSecret!!
            tokenViewModel.getBearerToken(
                clientId = clientId, clientSecret = clientSecret
            )
        }
    }

    private fun navigateToNextAction() {
        when (val nextCaptureItem = captureItems.getOrNull(capturePosition + 1)) {
            is CaptureItem.Selfie -> {
                navController.navigateTo(destinationId = R.id.selfieExampleFragment,
                    args = Bundle().apply {
                        putParcelable(
                            Constants.ARG_CAPTURE_SINGLE_DOCUMENT, nextCaptureItem
                        )
                    })
            }

            is CaptureItem.DocumentFrontImage, is CaptureItem.DocumentBackImage -> {
                navController.navigateTo(destinationId = R.id.cameraFragment,
                    args = Bundle().apply {
                        putParcelableArrayList(
                            Constants.ARG_CAPTURE_DOCUMENTS, captureItems
                        )
                        putInt(
                            Constants.ARG_CAPTURE_POSITION, capturePosition + 1
                        )
                    })
            }

            else -> {
                navController.navigateTo(
                    destinationId = R.id.lottieAnimationFragment,
                    args = Bundle().apply {
                        putParcelable(
                            Constants.ARG_LOTTIE_ANIMATION_TYPE,
                            LottieAnimationType.VERIFICATION_LOADING
                        )
                    })
            }
        }
    }

    private fun setupToolbar() {

        binding.toolbar.btnBack.setColorFilter(resources.getColor(android.R.color.white))
        binding.toolbar.tv2.setTextColor(resources.getColor(android.R.color.white))

        val captureItem = if (isCaptureSelfie) {
            selfieCaptureItem
        } else {
            captureItems.getOrNull(capturePosition)
        }

        val tv1 = binding.toolbar.tv1
        binding.toolbar.tv2.setText(R.string.you_need_to_take_a_photo)

        when (captureItem) {
            is CaptureItem.DocumentFrontImage -> {
                tv1.text = getString(R.string.verify_your_s, captureItem.name)
            }

            is CaptureItem.DocumentBackImage -> {
                tv1.text = getString(R.string.verify_your_s, captureItem.name)
            }

            is CaptureItem.Selfie -> {
                tv1.text = getString(R.string.verify_your_s, "")
            }

            else -> Unit
        }

    }

    override fun setupClickListeners() {
        binding.btnCapture.setOnClickListener(this)
        binding.btnSwitchCamera.setOnClickListener(this)
        binding.toolbar.btnBack.setOnClickListener(this)
        binding.btnLooksGood.setOnClickListener(this)
        binding.btnRetake.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    override fun onDestroy() {

        // Shut down our background executor
        cameraExecutor?.shutdown()

        super.onDestroy()
    }


    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Rebind the camera with the updated display metrics
        bindCameraUseCases()

        // Enable or disable switching between cameras
        updateCameraSwitchButton()
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private suspend fun setUpCamera() {
        cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()

        // Select lensFacing depending on the available cameras
        lensFacing = when {
            hasBackCamera() -> CameraSelector.LENS_FACING_BACK
            hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
            else -> throw IllegalStateException("Back and front camera are unavailable")
        }

        if (isCaptureSelfie && hasFrontCamera()) {
            lensFacing = CameraSelector.LENS_FACING_FRONT
        }

        // Enable or disable switching between cameras
        updateCameraSwitchButton()

        // Build and bind the camera use cases
        bindCameraUseCases()
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(requireActivity()).bounds

        Timber.d("Screen metrics: " + metrics.width() + " x " + metrics.height())

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Timber.d("Preview aspect ratio: $screenAspectRatio")

        val rotation = binding.viewFinder.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        if (camera != null) {
            // Must remove observers from the previous camera instance
            removeCameraStateObservers(camera!!.cameraInfo)
        }

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            Timber.e(exc, "Use case binding failed")
        }
    }

    private fun removeCameraStateObservers(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.removeObservers(this@CameraFragment)
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(this@CameraFragment) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                        Timber.i("CameraState: Pending Open")
                        showToast("Close other camera apps")
                    }

                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        Timber.i("CameraState: Opening")
                    }

                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                        Timber.i("CameraState: Open")
                    }

                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                        Timber.i("CameraState: Closing")
                    }

                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                        Timber.i("CameraState: Closed")
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        Timber.i("Stream config error")
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the camera
                        Timber.i("Camera in use")
                        showToast("Close other camera apps")
                    }

                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        Timber.i("Max cameras in use")
                        showToast("Close other camera apps")
                    }

                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                        Timber.i("Other recoverable error")
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        Timber.i("Camera disabled")
                        showToast("Enable the device's cameras")
                    }

                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        Timber.i("Fatal error")
                        showToast("reboot the device to restore camera function")
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Timber.i("Do not disturb mode enabled")
                        showToast("disable the \"Do Not Disturb\" mode, then reopen the camera")
                    }
                }
            }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {

        // Remove previous UI if any
//        cameraUiContainerBinding?.root?.let {
//            fragmentCameraBinding.root.removeView(it)
//        }
//
//        cameraUiContainerBinding = CameraUiContainerBinding.inflate(
//            LayoutInflater.from(requireContext()),
//            fragmentCameraBinding.root,
//            true
//        )
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
        try {
            binding.btnSwitchCamera.isEnabled = hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            binding.btnSwitchCamera.isEnabled = false
            Timber.e(exception)
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            binding.btnCapture.id -> {

                if (isNetworkNotConnected()) {
                    showToast("Please connect to internet")
                    return
                }

                binding.btnCapture.isEnabled = false

                // Get a stable reference of the modifiable image capture use case
                imageCapture?.let { imageCapture ->

                    // Setup image capture listener which is triggered after photo has been taken
                    imageCapture.takePicture(
                        cameraExecutor!!, object : ImageCapture.OnImageCapturedCallback() {
                            override fun onError(exc: ImageCaptureException) {
                                Timber.e(exc, "Photo capture failed: " + exc.message)
                                capturedImage.delete()
                                capturedImage = null
                                runOnUiThread {
                                    binding.btnCapture.isEnabled = true
                                }
                            }

                            override fun onCaptureSuccess(image: ImageProxy) {
                                Timber.d("Photo capture succeeded")

                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)

                                // Launch coroutine to save the image
                                lifecycleScope.launch {


                                    // Create time stamped name

                                    val captureItem = if (isCaptureSelfie) {
                                        selfieCaptureItem
                                    } else {
                                        captureItems[capturePosition]
                                    }!!

                                    val prefix = when (captureItem) {
                                        is CaptureItem.DocumentFrontImage -> {
                                            captureItem.name
                                        }

                                        is CaptureItem.DocumentBackImage -> {
                                            captureItem.name
                                        }

                                        is CaptureItem.Selfie -> {
                                            Constants.SELFIE
                                        }
                                    }

                                    val name = prefix + SimpleDateFormat(
                                        FILENAME, Locale.US
                                    ).format(System.currentTimeMillis()) + ".jpg"

                                    capturedImage = FileManager.saveCapturedImage(bytes, name)
                                        .let { savedFile ->
                                            Timber.d("Photo capture succeeded, saved at: ${savedFile.absolutePath}")
                                            savedFile
                                        }

                                    val key = when (captureItem) {
                                        is CaptureItem.DocumentFrontImage -> {
                                            if (captureItem.name.contains(
                                                    "license", ignoreCase = true
                                                )
                                            ) {
                                                Constants.DL_FRONT_IMAGE
                                            } else if (captureItem.name.contains(
                                                    "passport", ignoreCase = true
                                                )
                                            ) {
                                                Constants.PP_FRONT_IMAGE
                                            } else {
                                                Constants.ID_CARD_FRONT_IMAGE
                                            }
                                        }

                                        is CaptureItem.DocumentBackImage -> {
                                            if (captureItem.name.contains(
                                                    "license", ignoreCase = true
                                                )
                                            ) {
                                                Constants.DL_BACK_IMAGE
                                            } else if (captureItem.name.contains(
                                                    "passport", ignoreCase = true
                                                )
                                            ) {
                                                Constants.PP_BACK_IMAGE
                                            } else {
                                                Constants.ID_CARD_BACK_IMAGE
                                            }
                                        }

                                        is CaptureItem.Selfie -> {
                                            Constants.SELFIE
                                        }
                                    }

                                    capturedImage?.absolutePath?.let {
                                        AppConfig.addImagePath(
                                            key, it
                                        )
                                    }


                                    getToken()

                                    runOnUiThread {
                                        binding.btnCapture.isEnabled = true
                                    }
                                }
                                image.close() // Close the image

                            }
                        })

                    // We can only change the foreground Drawable using API level 23+ API
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        // Display flash animation to indicate that photo was captured
                        binding.root.postDelayed({
                            binding.root.foreground = ColorDrawable(Color.WHITE)
                            binding.root.postDelayed(
                                { binding.root.foreground = null }, ANIMATION_FAST_MILLIS
                            )
                        }, ANIMATION_SLOW_MILLIS)
                    }
                }
            }

            binding.btnSwitchCamera.id -> {
                lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                // Re-bind use cases to update selected camera
                bindCameraUseCases()
            }

            binding.toolbar.btnBack.id -> {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            binding.btnLooksGood.id -> {

                if (isNetworkNotConnected()) {
                    showToast("Please connect to internet")
                    return
                }

                navigateToNextAction()
            }

            binding.btnRetake.id -> {
                handleSwitchState(InternalCameraState.CAMERA_ON)
            }

        }
    }

    override fun setupThemes() {
        AppConfig.getCustomColor(FaceKi.ColorElement.TitleTextColor).getColorIntOrNull()?.let {
            binding.toolbar.tv1.setTextColor(it)
            binding.btnLooksGood.setTextColor(it)

            binding.imgCapture.setColorFilter(it)

            val backgroundDrawable = binding.btnCapture.background as GradientDrawable
            backgroundDrawable.setStroke(2, it)
            binding.btnRetake.background = backgroundDrawable

        }
        AppConfig.getCustomColor(FaceKi.ColorElement.SecondaryTextColor).getColorIntOrNull()?.let {
            binding.toolbar.tv2.setTextColor(it)
        }

        val logoImgView = binding.toolbar.imageViewLogo
        logoImgView.loadIcon(iconValue = AppConfig.getCustomIcon(FaceKi.IconElement.Logo),
            onSuccess = { logoImgView.makeVisible() },
            onFailure = { exception ->
                logoImgView.makeGone()
                exception.printStackTrace()
            })


    }

    private fun showLoading() {
        if (!isLoading && !requireActivity().isFinishing) {
            loadingDialog = AnalyzingProgressDialogFragment().also {
                it.show(parentFragmentManager, "AnalyzingProgressDialogFragment")
            }
            isLoading = true
        }
    }

    private fun hideLoading() {
        if (isLoading) {
            loadingDialog?.dismiss()
            loadingDialog = null
            isLoading = false
        }
    }

    override fun onDestroyView() {
        hideLoading()
        super.onDestroyView()
        releaseCameraResources()
    }

    private fun checkCameraPermission() {
        if (!isGranted(Manifest.permission.CAMERA)) {
            handleCameraPermission()
            return
        }
    }

    private fun handleCameraPermission() {
        handlePermission(permission = Manifest.permission.CAMERA, onNotGranted = {
            cameraPermissionLauncher.launch(it)
        }, onRationaleNeeded = {
            showPermissionExplainDialog(title = getString(R.string.camera),
                message = getString(R.string.camera_msg),
                onAllow = {
                    goToAppDetailsSettings(CAMERA_PERMISSION)
                },
                onDismiss = {
                    checkCameraPermission()
                })

        })
    }

    //method to release camera resources
    private fun releaseCameraResources() {
        try {
            camera?.cameraControl?.enableTorch(false)
            cameraProvider?.unbindAll()
            cameraExecutor?.shutdown()
            camera = null
            cameraProvider = null
            preview = null
            imageCapture = null
            // Unregister the listeners
            displayManager.unregisterDisplayListener(displayListener)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun handleSwitchState(newState: InternalCameraState) {
        try {
            currentCameraState = newState
            when (currentCameraState) {
                InternalCameraState.CAMERA_ON -> {
                    binding.ivBg.setImageDrawable(null)
                    capturedImage.delete()
                    capturedImage = null
                    binding.llPhotoButtons.visibility = View.GONE
                    binding.llCameraButtons.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.VISIBLE
                    startCamera()
                }

                InternalCameraState.IMAGE_DISPLAY -> {
                    Glide.with(requireContext()).load(capturedImage).into(binding.ivBg)
                    binding.llCameraButtons.visibility = View.GONE
                    binding.llPhotoButtons.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.GONE
                    releaseCameraResources()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_TYPE = "image/jpeg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val CAMERA_PERMISSION = 28
    }

}