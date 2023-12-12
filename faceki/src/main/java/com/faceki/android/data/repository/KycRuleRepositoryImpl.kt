package com.faceki.android.data.repository

import com.faceki.android.data.mapper.toRuleResponseData
import com.faceki.android.data.remote.FaceKiApi
import com.faceki.android.domain.model.RuleResponseData
import com.faceki.android.domain.preferences.Preferences
import com.faceki.android.domain.repository.KycRuleRepository
import com.faceki.android.util.KYCErrorCodes
import com.faceki.android.util.Resource
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

internal class KycRuleRepositoryImpl(
    private val faceKiApi: FaceKiApi, private val preferences: Preferences
) : KycRuleRepository {
    override suspend fun getKycRules(fetchFromRemote: Boolean): Resource<RuleResponseData> {
        return try {

            if (!fetchFromRemote) {
                return Resource.Success(preferences.getRuleResponse())
            }

            val response = faceKiApi.getKycRules()
            val body = response.body()!!

            if (response.isSuccessful) {

                when (body.responseCode) {
                    KYCErrorCodes.SUCCESS -> {
                        val ruleResponse = body.data!!.toRuleResponseData()
                        preferences.saveRuleResponse(ruleResponse)
                        Resource.Success(ruleResponse)
                    }

                    else -> {
                        Resource.Error("Couldn't get Kyc Rules")
                    }
                }


            } else {
                Resource.Error("Couldn't get Kyc Rules")
            }

        } catch (e: NullPointerException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Kyc Rules"
            )
        } catch (e: IOException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Kyc Rules"
            )
        } catch (e: HttpException) {
            Timber.e(e)
            Resource.Error(
                message = "Couldn't get Kyc Rules"
            )
        }
    }
}