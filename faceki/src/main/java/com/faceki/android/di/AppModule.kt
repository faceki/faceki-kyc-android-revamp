package com.faceki.android.di

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.faceki.android.data.preferences.DefaultPreferences
import com.faceki.android.data.provider.TokenProviderImpl
import com.faceki.android.data.remote.FaceKiApi
import com.faceki.android.data.remote.ImageQualityApi
import com.faceki.android.data.remote.interceptor.AuthInterceptor
import com.faceki.android.data.repository.ImageQualityRepositoryImpl
import com.faceki.android.data.repository.KycRuleRepositoryImpl
import com.faceki.android.data.repository.KycVerificationRepositoryImpl
import com.faceki.android.data.repository.TokenRepositoryImpl
import com.faceki.android.domain.preferences.Preferences
import com.faceki.android.domain.provider.TokenProvider
import com.faceki.android.domain.repository.ImageQualityRepository
import com.faceki.android.domain.repository.KycRuleRepository
import com.faceki.android.domain.repository.KycVerificationRepository
import com.faceki.android.domain.repository.TokenRepository
import com.faceki.android.util.addLoggingInterceptorIfInDevelopmentMode
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit


internal object AppModule {
    fun clear() {
        preferences = null
        faceKiApi = null
        imageQualityApi = null
        tokenRepository = null
        ruleRepository = null
        kycVerificationRepository = null
        imageQualityRepository = null
        tokenProvider = null
        application = null
    }

    @Volatile
    private var preferences: Preferences? = null

    @Volatile
    private var faceKiApi: FaceKiApi? = null

    @Volatile
    private var imageQualityApi: ImageQualityApi? = null

    @Volatile
    private var tokenRepository: TokenRepository? = null

    @Volatile
    private var ruleRepository: KycRuleRepository? = null

    @Volatile
    private var kycVerificationRepository: KycVerificationRepository? = null

    @Volatile
    private var imageQualityRepository: ImageQualityRepository? = null

    @Volatile
    private var tokenProvider: TokenProvider? = null


    private var application: Application? = null

    fun initialize(application: Application) {
        this.application = application
        Timber.plant(Timber.DebugTree())
    }

    private fun provideEncryptedSharedPreferences(
        application: Application
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(application, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        return EncryptedSharedPreferences.create(
            application,
            "face_ki_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    }

    private fun providePreferences(application: Application = this@AppModule.application!!): Preferences {
        return preferences ?: synchronized(this) {
            preferences ?: run {
                DefaultPreferences(
                    provideEncryptedSharedPreferences(
                        application = application
                    )
                ).also {
                    preferences = it
                }
            }
        }
    }

    private fun provideTokenProvider(preferences: Preferences = providePreferences()): TokenProvider {
        return tokenProvider ?: synchronized(this) {
            tokenProvider ?: run {
                TokenProviderImpl(
                    preferences = preferences
                ).also {
                    tokenProvider = it
                }
            }
        }
    }

    private fun provideAuthInterceptor(tokenProvider: TokenProvider = provideTokenProvider()): AuthInterceptor {
        return AuthInterceptor(
            tokenProvider = tokenProvider
        )
    }

    private fun provideOkHttpClient(authInterceptor: AuthInterceptor = provideAuthInterceptor()): OkHttpClient {
        return OkHttpClient.Builder().connectTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES)
            .addLoggingInterceptorIfInDevelopmentMode().addInterceptor(authInterceptor).build()
    }

    private fun provideOkHttpClientForImageQualityApi(): OkHttpClient {
        return OkHttpClient.Builder().addLoggingInterceptorIfInDevelopmentMode().build()
    }


    private fun provideFaceKiApi(
        okHttpClient: OkHttpClient = provideOkHttpClient()
    ): FaceKiApi {
        return faceKiApi ?: synchronized(this) {
            faceKiApi ?: run {
                Retrofit.Builder().baseUrl(FaceKiApi.BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create()).client(okHttpClient)
                    .build().create(FaceKiApi::class.java).also {
                        faceKiApi = it
                    }
            }
        }

    }

    fun provideTokenRepository(
        faceKiApi: FaceKiApi = provideFaceKiApi(), preferences: Preferences = providePreferences()
    ): TokenRepository {
        return tokenRepository ?: synchronized(this) {
            tokenRepository ?: run {
                TokenRepositoryImpl(
                    faceKiApi = faceKiApi, preferences = preferences
                ).also {
                    tokenRepository = it
                }
            }
        }
    }


    fun provideKycRuleRepository(
        faceKiApi: FaceKiApi = provideFaceKiApi(), preferences: Preferences = providePreferences()
    ): KycRuleRepository {
        return ruleRepository ?: synchronized(this) {
            ruleRepository ?: run {
                KycRuleRepositoryImpl(
                    faceKiApi = faceKiApi, preferences = preferences
                ).also {
                    ruleRepository = it
                }
            }
        }
    }

    fun provideImageQualityRepository(
        imageQualityApi: ImageQualityApi = provideImageQualityApi()
    ): ImageQualityRepository {
        return imageQualityRepository ?: synchronized(this) {
            imageQualityRepository ?: run {
                ImageQualityRepositoryImpl(
                    imageQualityApi = imageQualityApi
                ).also {
                    imageQualityRepository = it
                }
            }
        }
    }

    fun provideKycVerificationRepository(
        faceKiApi: FaceKiApi = provideFaceKiApi()
    ): KycVerificationRepository {
        return kycVerificationRepository ?: synchronized(this) {
            kycVerificationRepository ?: run {
                KycVerificationRepositoryImpl(
                    faceKiApi = faceKiApi
                ).also {
                    kycVerificationRepository = it
                }
            }
        }
    }


    private fun provideImageQualityApi(
        okHttpClient: OkHttpClient = provideOkHttpClientForImageQualityApi()
    ): ImageQualityApi {
        return imageQualityApi ?: synchronized(this) {
            imageQualityApi ?: run {
                Retrofit.Builder().baseUrl(ImageQualityApi.BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create()).client(okHttpClient)
                    .build().create(ImageQualityApi::class.java).also {
                        imageQualityApi = it
                    }
            }
        }

    }


}