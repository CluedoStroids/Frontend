package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.R

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var mystomp:MyStomp
    lateinit var  response:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        mystomp=MyStomp(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.fragment_fullscreen)


        findViewById<Button>(R.id.connectbtn).setOnClickListener { mystomp.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener{mystomp.sendHello()}
        findViewById<Button>(R.id.jsonbtn).setOnClickListener{mystomp.sendJson()}
        response=findViewById(R.id.response_view)

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            //add Composable Contents here
        }

    }

    override fun onResponse(res: String) {
        response.setText(res)
    }


}

//used to preview composable contents
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivity()
}
