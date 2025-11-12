package io.github.aidenk.sevenshuffle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity : AppCompatActivity() {

    private lateinit var newGameBtn: Button
    private lateinit var gameHistoryBtn: Button
    private lateinit var settingsBtn: Button
    private lateinit var helpBtn: Button

    private lateinit var dataManager: DataManager
    private lateinit var dialogManager: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        dataManager = (application as MyApp).dataManager
        //dialogManager = (application as MyApp).dialogManager

        newGameBtn = findViewById(R.id.new_game_btn)
        gameHistoryBtn = findViewById(R.id.game_history_btn)
        settingsBtn = findViewById(R.id.settings_btn)
        helpBtn = findViewById(R.id.help_btn)

        newGameBtn.setOnClickListener {
            moveToGameActivity()
        }

//        gameHistoryBtn.setOnClickListener {
//            dialogManager.showPastGamesDialog()
//        }
//
//        settingsBtn.setOnClickListener {
//            dialogManager.showSettingsDialog()
//        }
//
//        helpBtn.setOnClickListener {
//            dialogManager.showHelpDialog()
//        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }

    private fun moveToGameActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // remove this if you want Back to return to menu
    }
}


