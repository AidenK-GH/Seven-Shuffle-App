//package io.github.aidenk.sevenshuffle
//
//import android.content.Intent
//import android.net.Uri
//import android.text.method.LinkMovementMethod
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//
//// NOT IN USE
//// but keeping in-case ever want to use it
//// will probable delete this before its added to play-store
//
//class DialogManager(
//    private val activity: AppCompatActivity,
//    private val dataManager: DataManager = (activity.application as MyApp).dataManager,
//    /** InputMode accessors from MainActivity */
//    private val getInputMode: () -> MainActivity.InputMode,
//    private val setInputMode: (MainActivity.InputMode) -> Unit,
//    /** Apply the visual change after toggling input mode */
//    private val applyInputModeUI: () -> Unit,
//    /** What to do when user confirms “New Game” */
//    private val onStartNewGame: () -> Unit,
//    /** Theme titles for the spinner */
//    private val themeTitleList: Array<String>
//) {
//
//    private val inflater: LayoutInflater = activity.layoutInflater
//    private val sharedPrefs by lazy { SharedPreferenceManger(activity) }
//
//    fun showNewGameConfirmationDialog() {
//        val builder = AlertDialog.Builder(activity)
//        val dialogView = inflater.inflate(R.layout.ask_new_game, null)
//        builder.setView(dialogView)
//
//        val dialog = builder.create()
//        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
//        dialogView.findViewById<Button>(R.id.no_button).setOnClickListener { dialog.dismiss() }
//        dialogView.findViewById<Button>(R.id.new_game_btn).setOnClickListener {
//            onStartNewGame()
//            dialog.dismiss()
//        }
//        dialog.show()
//    }
//
//    fun showHelpDialog() {
//        val builder = AlertDialog.Builder(activity)
//        val dialogView = inflater.inflate(R.layout.help_info, null)
//        builder.setView(dialogView)
//
//        val dialog = builder.create()
//        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
//
//        val linkTextView = dialogView.findViewById<TextView?>(R.id.github_textview)
//        linkTextView?.apply {
//            text = "https://github.com/AidenK-GH/Seven-Shuffle-App/tree/master"
//            linksClickable = true
//            movementMethod = LinkMovementMethod.getInstance()
//            setOnClickListener {
//                val i = Intent(Intent.ACTION_VIEW, Uri.parse(text.toString()))
//                activity.startActivity(i)
//            }
//        }
//
//        dialog.show()
//    }
//
//    fun showSettingsDialog() {
//        val builder = AlertDialog.Builder(activity)
//        val dialogView = inflater.inflate(R.layout.setting_window, null)
//        builder.setView(dialogView)
//
//        val dialog = builder.create()
//        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
//
//        val toggleInputBtn = dialogView.findViewById<Button>(R.id.toggle_input_mode_btn)
//        toggleInputBtn.text = when (getInputMode()) {
//            MainActivity.InputMode.KEYBOARD -> "Switch to Buttons"
//            MainActivity.InputMode.BUTTONS -> "Switch to Keyboard"
//        }
//
//        val themeSpinner = dialogView.findViewById<Spinner>(R.id.themeSpinner)
//
//        val adapter = ArrayAdapter(
//            activity,
//            android.R.layout.simple_spinner_dropdown_item,
//            themeTitleList
//        )
//        themeSpinner.adapter = adapter
//        themeSpinner.setSelection(sharedPrefs.theme)
//
//        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?, view: View?, position: Int, id: Long
//            ) {
//                if (position != sharedPrefs.theme) {
//                    sharedPrefs.theme = position
//                    AppCompatDelegate.setDefaultNightMode(sharedPrefs.themeFlag[position])
//                    dialog.dismiss()
//                }
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }
//
//        toggleInputBtn.setOnClickListener {
//            val next = when (getInputMode()) {
//                MainActivity.InputMode.KEYBOARD -> MainActivity.InputMode.BUTTONS
//                MainActivity.InputMode.BUTTONS -> MainActivity.InputMode.KEYBOARD
//            }
//            setInputMode(next)
//            dialog.dismiss()
//            applyInputModeUI()
//        }
//
//        dialog.show()
//    }
//
//    fun showPastGamesDialog() {
//        val builder = AlertDialog.Builder(activity)
//        val dialogView = inflater.inflate(R.layout.past_games_board, null)
//        builder.setView(dialogView)
//        val dialog = builder.create()
//
//        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.board_recycler_view)
//        val gamesList = dataManager.getPastGames()
//
//        if (gamesList.isNotEmpty()) {
//            recyclerView.layoutManager = LinearLayoutManager(activity)
//            recyclerView.adapter = PastGamesAdapter(gamesList)
//        } else {
//            recyclerView.visibility = View.GONE
//            val tv = TextView(activity).apply {
//                text = "No past games yet. Play one and check back!"
//                textSize = 16f
//                setTextColor(ContextCompat.getColor(activity, android.R.color.black))
//                setPadding(24, 24, 24, 24)
//            }
//            (recyclerView.parent as? ViewGroup)?.addView(tv)
//        }
//
//        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
//        dialog.show()
//    }
//}
