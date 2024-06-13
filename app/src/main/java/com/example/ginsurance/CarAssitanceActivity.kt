package com.example.ginsurance
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ginsurance.model.GPSVoiceCallData
import com.example.ginsurance.services.GPSVoiceCallService
import com.example.ginsurance.services.ServiceBuilder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val CONTACT_CENTER_NUMBER = "017019484"
class CarAssitanceActivity : AppCompatActivity() {

    private lateinit var butStartChat: Button
    private lateinit var butStartVoiceCall: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_car_assitance)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<TextView>(R.id.txtCustomerName)!!.text = getCustomerName()
        requestPermissions()
        butStartChat = findViewById<Button>(R.id.butStartChat)!!
        butStartChat.setOnClickListener {

            val confirmDialog = AlertDialog.Builder(this)
            confirmDialog.setTitle("Please Confirm")
            confirmDialog.setIcon(R.drawable.gps)
            confirmDialog.setMessage("To make our service more efficient, your GPS location will be shared over a secure connection just for one time. If you agree click Yes or if you want to continue without sharing your data click No")
            confirmDialog.setPositiveButton("Yes") { _, _ ->
                getLocation(false)
            }
            confirmDialog.setNegativeButton("No") { _, _ -> chatWithoutGPS() }

            val createDialog = confirmDialog.create()
            createDialog.show()
        }
        butStartVoiceCall = findViewById<Button>(R.id.butStartVoiceCall)!!
        butStartVoiceCall.setOnClickListener {

            val confirmDialog = AlertDialog.Builder(this)
            confirmDialog.setTitle("Please Confirm")
            confirmDialog.setIcon(R.drawable.gps)
            confirmDialog.setMessage("To make our service more efficient, your GPS location and your phone number will be shared with our systems just for this call. If you agree click Yes or if you want to continue without sharing your location click No")
            confirmDialog.setPositiveButton("Yes") { _, _ ->
                getLocation(true)
            }
            confirmDialog.setNegativeButton("No") { _, _ -> voiceCallWithoutGPS() }
            val createDialog = confirmDialog.create()
            createDialog.show()
        }


    }
    private fun getLocation(isVoiceCall:Boolean) {

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(applicationContext, "The app does not have GPS permissions, please enable them and try again", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
        }
        else
        {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            var taskLocation: Task<Location> = fusedLocationClient.lastLocation
            taskLocation.addOnSuccessListener {
                if (it != null) {
                    Toast.makeText(applicationContext,"GPS successfully get: ${it.latitude} ${it.longitude}",Toast.LENGTH_LONG).show()
                    if(isVoiceCall)
                        startVoiceCall(it.latitude, it.longitude)
                    else
                        startChat(it.latitude, it.longitude)
                }
            }
        }
    }
    private fun startVoiceCall(latitude: Double, longitude: Double) {

        //Get Mobile Phone Number
        val phoneNumber = getNumber()
        //Call API Web Service to save phone, GPS, and customer data
        val gpsVoiceCall = GPSVoiceCallData(phoneNumber, latitude.toString(),longitude.toString(),"Roadside Assistance", getCustomerID(),getCustomerName())
        val gpsVoiceCallService: GPSVoiceCallService = ServiceBuilder.buildService(GPSVoiceCallService::class.java)
        val requestCall: Call<GPSVoiceCallData> = gpsVoiceCallService.AddGPSVoiceCall(gpsVoiceCall)
        //Execute API
        requestCall.enqueue(object : Callback<GPSVoiceCallData> {
            override fun onResponse(call: Call<GPSVoiceCallData>, response: Response<GPSVoiceCallData>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Information saved in the backend successfully", Toast.LENGTH_LONG).show()
                    //Start Voice Call
                    val dialIntent = Intent(Intent.ACTION_DIAL)
                    dialIntent.data = Uri.parse("tel:$CONTACT_CENTER_NUMBER")
                    startActivity(dialIntent)
                }
                else
                    Toast.makeText(applicationContext, "API Call Failed", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<GPSVoiceCallData>, t: Throwable) {
                Toast.makeText(applicationContext, "API Call Failed", Toast.LENGTH_LONG).show()
            }
        }
        )



    }
    private fun getNumber():String {
        if (!(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_NUMBERS) ==PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(applicationContext, "The app does not have get phone number permissions, please enable them and try again", Toast.LENGTH_LONG).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_PHONE_NUMBERS,
                        Manifest.permission.READ_PHONE_STATE
                    ), 100
                )
            }
            return ""
        }
        else
        {
            val phoneNumbers = arrayListOf<String>()
            if (isFromAPI(23)) {
                val subscriptionManager = getSystemService(SubscriptionManager::class.java)
                val subsInfoList = subscriptionManager.activeSubscriptionInfoList
                for (subscriptionInfo in subsInfoList) {
                    val phoneNumber =
                        if (isFromAPI(33))
                            subscriptionManager.getPhoneNumber(subscriptionInfo.subscriptionId)
                        else subscriptionInfo.number
                    if (phoneNumber.isNullOrBlank().not()) phoneNumbers.add(phoneNumber)
                }
                return phoneNumbers[0]
            } else
                return ""
        }
    }
    private fun isFromAPI(apiLevel: Int) = Build.VERSION.SDK_INT >= apiLevel
    private fun startChat(latitude: Double, longitude: Double) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        intent.putExtra("customerName", getCustomerName())
        intent.putExtra("customerID", getCustomerID())
        intent.putExtra("operationRequested", "Car Accident")

        startActivity(intent)
    }


    private fun requestPermissions()
    {
        val listPerm: MutableList<String> = ArrayList()
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            listPerm.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_NUMBERS) ==PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                listPerm.add(android.Manifest.permission.READ_PHONE_NUMBERS)
                listPerm.add(android.Manifest.permission.READ_PHONE_STATE)
                //requestPermissions(arrayOf(Manifest.permission.READ_PHONE_NUMBERS,Manifest.permission.READ_PHONE_STATE), 100 )
            }
        }
        if(listPerm.isNotEmpty())
            ActivityCompat.requestPermissions(this,listPerm.toTypedArray(),101)

    }

























    private fun getCustomerName(): String{
        return "Jordan Rosas"
    }
    private fun getCustomerID(): String{
        return "41739991"
    }
    private fun chatWithoutGPS(){

    }
    private fun voiceCallWithoutGPS(){

    }


}