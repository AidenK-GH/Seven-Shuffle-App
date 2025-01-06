package com.example.anagramarama_app

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout

class MyKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {
    // keyboard keys (buttons)
    private var mButton1: Button? = null
    private var mButton2: Button? = null
    private var mButton3: Button? = null
    private var mButton4: Button? = null
    private var mButton5: Button? = null
    private var mButton6: Button? = null
    private var mButton7: Button? = null
    private var mButtonDelete: Button? = null
    private var mButtonEnter: Button? = null

    // This will map the button resource id to the String value that we want to
    // input when that button is clicked.
    var keyValues = SparseArray<String>()

    // Our communication link to the EditText
    var inputConnection: InputConnection? = null

    // constructors
    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        // initialize buttons
        LayoutInflater.from(context).inflate(R.layout.custom_keyboard, this, true)
        mButton1 = findViewById<View>(R.id.button_1) as Button
        mButton2 = findViewById<View>(R.id.button_2) as Button
        mButton3 = findViewById<View>(R.id.button_3) as Button
        mButton4 = findViewById<View>(R.id.button_4) as Button
        mButton5 = findViewById<View>(R.id.button_5) as Button
        mButton6 = findViewById<View>(R.id.button_6) as Button
        mButton7 = findViewById<View>(R.id.button_7) as Button
        mButtonDelete = findViewById<View>(R.id.button_delete) as Button
        //mButtonEnter = findViewById<View>(R.id.button_enter) as Button

        // set button click listeners
        mButton1!!.setOnClickListener(this)
        mButton2!!.setOnClickListener(this)
        mButton3!!.setOnClickListener(this)
        mButton4!!.setOnClickListener(this)
        mButton5!!.setOnClickListener(this)
        mButton6!!.setOnClickListener(this)
        mButton7!!.setOnClickListener(this)
        mButtonDelete!!.setOnClickListener(this)
        //mButtonEnter!!.setOnClickListener(this)

        // map buttons IDs to input strings
        keyValues.put(R.id.button_1, "1")
        keyValues.put(R.id.button_2, "2")
        keyValues.put(R.id.button_3, "3")
        keyValues.put(R.id.button_4, "4")
        keyValues.put(R.id.button_5, "5")
        keyValues.put(R.id.button_6, "6")
        keyValues.put(R.id.button_7, "7")
        //keyValues.put(R.id.button_enter, "\n")
    }

    override fun onClick(v: View) {

        // do nothing if the InputConnection has not been set yet
        if (inputConnection == null) return

        // Delete text or input key value
        // All communication goes through the InputConnection
        if (v.id == R.id.button_delete) {
            val selectedText = inputConnection!!.getSelectedText(0)
            if (TextUtils.isEmpty(selectedText)) {
                // no selection, so delete previous character
                inputConnection!!.deleteSurroundingText(1, 0)
            } else {
                // delete the selection
                inputConnection!!.commitText("", 1)
            }
        } else {
            val value = keyValues[v.id]
            inputConnection!!.commitText(value, 1)
        }
    }

    // The activity (or some parent or controller) must give us
    // a reference to the current EditText's InputConnection
    fun configureInputConnection(ic: InputConnection?) {
        inputConnection = ic
    }
}
