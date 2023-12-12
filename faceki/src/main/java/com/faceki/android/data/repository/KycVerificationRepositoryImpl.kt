package com.faceki.android.data.repository

import com.faceki.android.data.remote.FaceKiApi
import com.faceki.android.domain.model.VerificationResponse
import com.faceki.android.domain.repository.KycVerificationRepository
import com.faceki.android.util.Constants
import com.faceki.android.util.KYCErrorCodes
import com.faceki.android.util.Resource
import com.faceki.android.util.asMultipart
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.IOException

internal class KycVerificationRepositoryImpl(
    private val faceKiApi: FaceKiApi
) : KycVerificationRepository {
    override suspend fun verifySingleKyc(
        selfieImage: File, docFrontImage: File, docBackImage: File
    ): Resource<VerificationResponse> {
        return try {

            val response = faceKiApi.verifySingleKyc(
                selfie_image = selfieImage.asMultipart(partName = "selfie_image"),
                doc_front_image = docFrontImage.asMultipart(partName = "doc_front_image"),
                doc_back_image = docBackImage.asMultipart(partName = "doc_back_image")
            )

            if (response.isSuccessful) {

                val jsonString = response.body()!!.string()

                val jsonObject: JsonObject = JsonParser.parseString(jsonString).asJsonObject

                when (jsonObject.get("responseCode").asInt) {
                    KYCErrorCodes.SUCCESS -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.SUCCESS,
                                jsonBody = jsonObject.toString()
                            )
                        )
                    }

                    KYCErrorCodes.PLEASE_TRY_AGAIN -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.PLEASE_TRY_AGAIN,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "Verification failed. Please try the verification process again."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_CROPPED -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_CROPPED,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image appears to be cropped or not fully visible."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_TOO_CLOSED -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_TOO_CLOSED,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image is too close to the camera, affecting the quality of the verification."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_NOT_FOUND -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_NOT_FOUND,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The system could not detect a face in the provided image."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_CLOSED_TO_BORDER -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_CLOSED_TO_BORDER,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image is too close to the border, affecting the quality of the verification."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_TOO_SMALL -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_TOO_SMALL,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image is too small for accurate verification."
                            )
                        )
                    }

                    KYCErrorCodes.POOR_LIGHT -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.POOR_LIGHT,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The image quality is affected due to poor lighting conditions. Please ensure you are in a well-lit environment and try the verification process again."
                            )
                        )
                    }

                    else -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = Constants.INVALID_RESPONSE_CODE,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "Couldn't verify single Kyc"
                            )
                        )
                    }
                }


            } else {
                Resource.Success(
                    VerificationResponse(
                        responseCode = Constants.INVALID_RESPONSE_CODE,
                        errorMessage = "Couldn't verify single Kyc"
                    )
                )
            }


        } catch (e: NullPointerException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify single Kyc"
                )
            )
        } catch (e: JsonSyntaxException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify single Kyc"
                )
            )
        } catch (e: JsonParseException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify single Kyc"
                )
            )
        } catch (e: IOException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify single Kyc"
                )
            )
        } catch (e: HttpException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify single Kyc"
                )
            )
        }
    }

    override suspend fun verifyMultipleKyc(
        selfieImage: File,
        idFrontImage: File,
        idBackImage: File,
        dlFrontImage: File?,
        dlBackImage: File?,
        ppFrontImage: File?,
        ppBackImage: File?
    ): Resource<VerificationResponse> {
        return try {

            val response = faceKiApi.verifyMultipleKyc(
                selfie_image = selfieImage.asMultipart(partName = "selfie_image"),
                id_front_image = idFrontImage.asMultipart(partName = "id_front_image"),
                id_back_image = idBackImage.asMultipart(partName = "id_back_image"),
                dl_front_image = dlFrontImage?.asMultipart(partName = "dl_front_image"),
                dl_back_image = dlBackImage?.asMultipart(partName = "dl_back_image"),
                pp_front_image = ppFrontImage?.asMultipart(partName = "pp_front_image"),
                pp_back_image = ppBackImage?.asMultipart(partName = "pp_back_image"),
            )

            if (response.isSuccessful) {

                val jsonString = response.body()!!.string()

                val jsonObject: JsonObject = JsonParser.parseString(jsonString).asJsonObject

                when (jsonObject.get("responseCode").asInt) {
                    KYCErrorCodes.SUCCESS -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.SUCCESS,
                                jsonBody = jsonObject.toString()
                            )
                        )
                    }

                    KYCErrorCodes.PLEASE_TRY_AGAIN -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.PLEASE_TRY_AGAIN,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "Verification failed. Please try the verification process again."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_CROPPED -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_CROPPED,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image appears to be cropped or not fully visible."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_TOO_CLOSED -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_TOO_CLOSED,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image is too close to the camera, affecting the quality of the verification."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_NOT_FOUND -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_NOT_FOUND,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The system could not detect a face in the provided image."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_CLOSED_TO_BORDER -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_CLOSED_TO_BORDER,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image is too close to the border, affecting the quality of the verification."
                            )
                        )
                    }

                    KYCErrorCodes.FACE_TOO_SMALL -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.FACE_TOO_SMALL,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The face in the image is too small for accurate verification."
                            )
                        )
                    }

                    KYCErrorCodes.POOR_LIGHT -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = KYCErrorCodes.POOR_LIGHT,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "The image quality is affected due to poor lighting conditions. Please ensure you are in a well-lit environment and try the verification process again."
                            )
                        )
                    }

                    else -> {
                        Resource.Success(
                            VerificationResponse(
                                responseCode = Constants.INVALID_RESPONSE_CODE,
                                jsonBody = jsonObject.toString(),
                                errorMessage = "Couldn't verify multiple Kyc"
                            )
                        )
                    }
                }


            } else {
                Resource.Success(
                    VerificationResponse(
                        responseCode = Constants.INVALID_RESPONSE_CODE,
                        errorMessage = "Couldn't verify multiple Kyc"
                    )
                )
            }


        } catch (e: NullPointerException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify multiple Kyc"
                )
            )
        } catch (e: JsonSyntaxException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify multiple Kyc"
                )
            )
        } catch (e: JsonParseException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify multiple Kyc"
                )
            )
        } catch (e: IOException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify multiple Kyc"
                )
            )
        } catch (e: HttpException) {
            Timber.e(e)
            Resource.Success(
                VerificationResponse(
                    responseCode = Constants.INVALID_RESPONSE_CODE,
                    errorMessage = "Couldn't verify multiple Kyc"
                )
            )
        }
    }
}