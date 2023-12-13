package com.faceki.android.util

object KYCErrorCodes {
    const val INTERNAL_SYSTEM_ERROR = 1000
    const val SUCCESS = 0
    const val NO_RULES_FOR_COMPANY = 7001
    const val NEED_REQUIRED_IMAGES = 8001
    const val DOCUMENT_VERIFY_FAILED = 8002
    const val PLEASE_TRY_AGAIN = 8003
    const val FACE_CROPPED = 8004
    const val FACE_TOO_CLOSED = 8005
    const val FACE_NOT_FOUND = 8006
    const val FACE_CLOSED_TO_BORDER = 8007
    const val FACE_TOO_SMALL = 8008
    const val POOR_LIGHT = 8009
    const val ID_VERIFY_FAIL = 8010
    const val DL_VERIFY_FAIL = 8011
    const val PASSPORT_VERIFY_FAIL = 8012
    const val DATA_NOT_FOUND = 8013
    const val INVALID_VERIFICATION_LINK = 8014
    const val VERIFICATION_LINK_EXPIRED = 8015
    const val FAIL_TO_GENERATE_LINK = 8016
    const val KYC_VERIFICATION_LIMIT_REACHED = 8017
    const val SELFIE_MULTIPLE_FACES = 8018
    const val FACE_BLUR = 8019
}