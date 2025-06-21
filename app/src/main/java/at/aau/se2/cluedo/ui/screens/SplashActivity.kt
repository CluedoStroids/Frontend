package at.aau.se2.cluedo.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import at.aau.se2.cluedo.ui.MainActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DISPLAY_LENGTH = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            binding.versionTextView.text = getString(R.string.version_text, versionName)
        } catch (e: Exception) {
            binding.versionTextView.text = getString(R.string.version_text, "2.5")
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH)
    }
}
