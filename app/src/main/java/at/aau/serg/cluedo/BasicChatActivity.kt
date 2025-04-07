package at.aau.serg.cluedo

import MyStomp
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import com.example.myapplication.databinding.ActivityBaiscChatBinding

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityBaiscChatBinding

class BaiscChatActivity : ComponentActivity(), Callbacks{
    lateinit var mystomp:MyStomp
    lateinit var outputField: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        mystomp= (application as CluedoApp).mystomp
        val cluedoApp = application as CluedoApp
        cluedoApp.mystomp.init(this)

        super.onCreate(savedInstanceState)
        binding = ActivityBaiscChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        enableEdgeToEdge()
        setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);

        outputField = binding.responseView

        binding.messagebtn.setOnClickListener{mystomp.sendMessage(binding.inputText.text.toString())}
        binding.jsonbtn.setOnClickListener{mystomp.sendJson()}

        val composeView = binding.composeView
        composeView.setContent {
            //add Composable Contents here
        }
    }

    override fun onResponse(res: String) {
        if (::outputField.isInitialized){
            runOnUiThread {
                outputField.append(res+"\n")
            }
        }
    }


}