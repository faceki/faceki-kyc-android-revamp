package com.faceki.android.data.repository

import com.faceki.android.data.remote.ImageQualityApi
import com.faceki.android.domain.repository.ImageQualityRepository
import com.faceki.android.util.Resource
import com.faceki.android.util.asMultipart
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.IOException

internal class ImageQualityRepositoryImpl(
    private val imageQualityApi: ImageQualityApi
) : ImageQualityRepository {
    override suspend fun checkImageQuality(
        image: File
    ): Resource<String> {
        return try {

            val response = imageQualityApi.checkQuality(
                image.asMultipart(partName = "image")
            )
            val body = response.body()!!

            if (response.isSuccessful) {

                if (body.objectsDetected.isNotEmpty() && body.liveNess?.message.equals(
                        "passed", ignoreCase = true
                    )
                ) {
                    Resource.Success("passed")
                } else {
                    Resource.Error("Captured image unable to pass quality check!")
                }
            } else {
                Resource.Error("Captured image unable to pass quality check!")
            }

        } catch (e: NullPointerException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Quality test of image"
            )
        } catch (e: IOException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Quality test of image"
            )
        } catch (e: HttpException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Quality test of image"
            )
        }
    }
}