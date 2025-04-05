package at.aau.serg.cluedo

import MyStomp
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.example.myapplication.R

class BasicChatActivity : ComponentActivity(), Callbacks{
    lateinit var mystomp:MyStomp
    lateinit var outputField: TextView
    lateinit var inputText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        mystomp= (application as CluedoApp).mystomp
        mystomp

        val cluedoApp = application as CluedoApp
        cluedoApp.mystomp.init(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_baisc_chat)

        inputText = findViewById(R.id.inputText)
        outputField = findViewById(R.id.response_view)

        findViewById<Button>(R.id.hellobtn).setOnClickListener{mystomp.sendMessage(inputText.text.toString())}
        findViewById<Button>(R.id.jsonbtn).setOnClickListener{mystomp.sendJson()}

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            //add Composable Contents here
        }
    }

    override fun onResponse(res: String) {
        if (::outputField.isInitialized){
            runOnUiThread {
                //outputField.text = res
                outputField.append(res+"\n")
            }
        }
    }


}