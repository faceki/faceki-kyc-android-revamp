package com.faceki.android.data.repository

import com.faceki.android.data.remote.FaceKiApi
import com.faceki.android.domain.preferences.Preferences
import com.faceki.android.domain.repository.TokenRepository
import com.faceki.android.util.KYCErrorCodes
import com.faceki.android.util.Resource
import com.faceki.android.util.hasTokenExpired
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

internal class TokenRepositoryImpl(
    private val faceKiApi: FaceKiApi, private val preferences: Preferences
) : TokenRepository {
    override suspend fun getBearerToken(clientId: String, clientSecret: String): Resource<String> {
        return try {


            val token = preferences.getToken()
            val hasExpired = preferences.getTokenTimestamp().hasTokenExpired(
                token = token,
                expiresInSec = preferences.getTokenExpireTime()
            )

            if (!hasExpired) {
                return Resource.Success(token)
            }

            val response = faceKiApi.generateToken(
                clientId = clientId, clientSecret = clientSecret
            )
            val body = response.body()!!

            if (response.isSuccessful) {

                when (body.responseCode) {
                    KYCErrorCodes.SUCCESS -> {
                        body.data?.let { data ->
                            val accessToken = data.accessToken ?: ""
                            preferences.saveToken(accessToken)
                            preferences.saveTokenType(data.tokenType ?: "")
                            preferences.saveTokenExpireTime(data.expiresIn)
                            preferences.saveTokenTimestamp(System.currentTimeMillis())

                            Resource.Success(accessToken)
                        }
                        Resource.Success(body.data?.accessToken)
                    }

                    else -> {
                        Resource.Error("responseCode : ${body.responseCode} , statusCode : ${body.statusCode} , errorMessage = ${body.message} ,clientSecret = ${body.clientSecret} ,")
                    }
                }


            } else {
                Resource.Error("responseCode : ${body.responseCode} , statusCode : ${body.statusCode} , errorMessage = ${body.message} , clientSecret = ${body.clientSecret} ,")
            }

        } catch (e: NullPointerException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Bearer Token"
            )
        } catch (e: IOException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Bearer Token"
            )
        } catch (e: HttpException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Bearer Token"
            )
        }
    }

    override fun getBearerToken(): String? = preferences.getToken()

    override fun getTokenType(): String? = preferences.getTokenType()

    override fun getTokenExpireTime(): Int = preferences.getTokenExpireTime()

    override fun getTokenTimestamp(): Long = preferences.getTokenTimestamp()
}