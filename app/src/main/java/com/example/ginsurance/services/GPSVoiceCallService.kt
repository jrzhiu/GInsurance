package com.example.ginsurance.services

import android.provider.Telephony.Mms.Addr
import com.example.ginsurance.model.GPSVoiceCallData
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GPSVoiceCallService{

    @POST("api/GPSVoiceCall")
    fun AddGPSVoiceCall(@Body post: GPSVoiceCallData):Call<GPSVoiceCallData>

}