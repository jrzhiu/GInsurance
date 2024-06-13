package com.example.ginsurance.model

import java.time.LocalDateTime


data class GPSVoiceCallData (
    val PhoneNumber:String,
    val GPSLatitude:String,
    val GPSLongitude:String,
    val OperationRequested:String,
    val CustomerId:String,
    val CustomerName:String
)
