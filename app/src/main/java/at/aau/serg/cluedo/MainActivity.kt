package at.aau.serg.cluedo

import MyStomp
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFullscreenBinding
import kotlin.jvm.java

@SuppressLint("StaticFieldLeak")
private lateinit var binding: FragmentFullscreenBinding

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp:MyStomp                        //Client instance

    override fun onCreate(savedInstanceState: Bundle?) {
        val cluedoApp = application as CluedoApp        //store client instance on app-level
        cluedoApp.mystomp=MyStomp(this)
        myStomp = cluedoApp.mystomp

        super.onCreate(savedInstanceState)
        binding = FragmentFullscreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        enableEdgeToEdge()
        setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);

        var username = binding.inputText.text       //e.g get username from textfield

        binding.loginbtn.setOnClickListener {       //Connect to server and Switch activity.
            myStomp.connect()
            myStomp.login(username.toString())  //Example call
            runOnUiThread {
                var intent = Intent(this,BasicChatActivity::class.java)
                startActivity(intent)
            }
        }

        val composeView = binding.composeView
        composeView.setContent {
            //add Composable Contents here
        }
    }

    override fun onResponse(res: String) {
        runOnUiThread {
            Log.e("tag","magic response from Callback")
            //Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
        }
    }

}

//used to preview composable contents
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivity()
}