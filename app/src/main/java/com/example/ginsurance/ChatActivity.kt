package com.example.ginsurance

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.genesys.cloud.integration.messenger.MessengerAccount
import com.genesys.cloud.ui.structure.controller.*

class ChatActivity : AppCompatActivity() {

    private var deploymentID=""
    private var domain=""
    private var customerName=""
    private var customerID=""
    private var operationRequested=""
    private var transactionsIDs=""
    private var creditCardID=""
    private var tokenOAuth=""
    private var gpsLatitude=0.0
    private var gpsLongitude=0.0


    private fun loadChatConfiguration()
    {
        //TODO: Load Attributes from API or other Resources
        deploymentID="f11e8246-c7f6-4e9b-a2b6-911aec50f954"
        domain="mypurecloud.com"

    }
    private fun loadCustomAttributes()
    {
        customerName=intent.getStringExtra("customerName").toString()
        customerID=intent.getStringExtra("customerID").toString()
        operationRequested= intent.getStringExtra("operationRequested").toString()
        gpsLatitude = intent.getDoubleExtra("latitude",0.0)
        gpsLongitude = intent.getDoubleExtra("longitude",0.0)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        loadChatConfiguration()
        loadCustomAttributes()
        findViewById<TextView>(R.id.txtCustomerName)!!.text = customerName
        val messengerAccount = MessengerAccount(deploymentID,domain).apply { logging=false

            customAttributes= mapOf("CustomerName" to customerName,
                "CustomerID" to customerID,
                "CustomerName" to customerName,
                "OperationRequested" to operationRequested,
                "GPSLatitude" to gpsLatitude.toString(),
                "GPSLongitude" to gpsLongitude.toString())
        }
        val chatController = ChatController.Builder(this).build(messengerAccount, object : ChatLoadedListener {
            override fun onComplete(result: ChatLoadResponse) {
                result.error?.let {
                    Log.d("ERROR","Chat loading failed")
                } ?: let {

                    supportFragmentManager.beginTransaction().replace(R.id.fragmentChat,result.fragment!!, "ChatActivity").commit()

                }
            }
        })

    }








}
