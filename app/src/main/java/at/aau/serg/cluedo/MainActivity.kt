package at.aau.serg.cluedo

import MyStomp
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
import kotlin.jvm.java

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp:MyStomp
    lateinit var  response:TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        val cluedoApp = application as CluedoApp
        cluedoApp.mystomp=MyStomp(this)
        myStomp = cluedoApp.mystomp

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.fragment_fullscreen)

        var username = findViewById<TextView>(R.id.inputText).text

        findViewById<Button>(R.id.loginbtn).setOnClickListener {
            myStomp.connect()
            myStomp.login(username.toString())  //Example call
            runOnUiThread {
                var intent = Intent(this,BasicChatActivity::class.java)
                startActivity(intent)
            }
        }

        val composeView = findViewById<ComposeView>(R.id.compose_view)
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
