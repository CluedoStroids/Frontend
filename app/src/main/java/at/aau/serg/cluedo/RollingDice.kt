package at.aau.serg.cluedo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import kotlin.random.Random

class RollingDice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rolling_dice)

        val rollDiceButton = findViewById<Button>(R.id.button_roll_dice)
        val diceResultText = findViewById<TextView>(R.id.text_dice_result)

        rollDiceButton.setOnClickListener {
            val randomNumber = Random.nextInt(2, 13) // 2 bis 12 inklusive
            diceResultText.text = randomNumber.toString()
        }
    }
}