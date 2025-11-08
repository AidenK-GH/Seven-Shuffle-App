package io.github.aidenk.sevenshuffle

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        //MapManager.startLoad(this)
        lifecycleScope.launch {
            try {
                // Suspends until startLoad completes
                MapManager.startLoad(this@Splash)
                goToMain()
            } catch (e: Exception) {
                // TODO show a simple error/retry if you want
                MapManager.isLoaded = false
                val alertDialog = AlertDialog.Builder(this@Splash).create()
                alertDialog.setTitle("Alert")
                alertDialog.setMessage("Alert message to be shown")
                alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL, "OK",
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                        }
                    })
                alertDialog.show()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
