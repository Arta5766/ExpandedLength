package com.example.expandedlength

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.text.TextWatcher
import android.text.Editable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var thickness: EditText
    private lateinit var radius: EditText
    private lateinit var kFactor: EditText
    private val lengths = mutableListOf<EditText>()
    private lateinit var result: TextView
    private lateinit var details: TextView

    private fun dp(n: Int) =
        (n * resources.displayMetrics.density).toInt()

    private fun number(e: EditText): Double =
        e.text.toString().replace(',', '.')
            .toDoubleOrNull() ?: 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(16), dp(16), dp(16), dp(24))
            setBackgroundColor(Color.rgb(244, 246, 248))
        }

        scroll.addView(root)
        setContentView(scroll)

        val title = TextView(this).apply {
            text = "محاسبه طول گسترده"
            textSize = 24f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(25, 55, 85))
            setPadding(0, dp(12), 0, dp(20))
        }
        root.addView(title)

        addTitle(root, "مشخصات ورق")

        val parameters = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        root.addView(parameters)

        thickness = addField(parameters, "ضخامت ورق", "1")
        radius = addField(parameters, "شعاع خم", "0.5")
        kFactor = addField(parameters, "K Factor", "0.4")

        addTitle(root, "ابعاد قطعه")

        val defaults = listOf(
            "10", "15", "41", "824", "41",
            "15", "10", "0", "0", "0", "0"
        )

        for (i in 0..10) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, dp(4))
            }

            val label = TextView(this).apply {
                text = "L${i + 1}"
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(Color.DKGRAY)
            }

            row.addView(label,
                LinearLayout.LayoutParams(0, dp(48), 1f))

            val edit = EditText(this).apply {
                setText(defaults[i])
                textSize = 16f
                gravity = Gravity.CENTER
                inputType =
                    InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    InputType.TYPE_NUMBER_FLAG_SIGNED
                setSingleLine()
            }

            row.addView(edit,
                LinearLayout.LayoutParams(0, dp(48), 2f))

            val bend = TextView(this).apply {
                text = if (i == 0) "بدون خم" else "خم ${i}"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.GRAY)
            }

            row.addView(bend,
                LinearLayout.LayoutParams(0, dp(48), 1.5f))

            root.addView(row)
            lengths.add(edit)
        }

        val button = Button(this).apply {
            text = "محاسبه"
            textSize = 17f
            isAllCaps = false
        }

        root.addView(button,
            LinearLayout.LayoutParams(-1, dp(52)))

        addTitle(root, "نتیجه محاسبه")

رامین2, [11/6/2026 2:27 PM]
result = TextView(this).apply {
            textSize = 22f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(20, 110, 70))
            setPadding(0, dp(16), 0, dp(16))
        }

        root.addView(result)

        details = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(35, 85, 135))
        }

        root.addView(details)

        button.setOnClickListener {
            calculate()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int,
                before: Int, count: Int
            ) {
                calculate()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        thickness.addTextChangedListener(watcher)
        radius.addTextChangedListener(watcher)
        kFactor.addTextChangedListener(watcher)

        lengths.forEach {
            it.addTextChangedListener(watcher)
        }

        calculate()
    }

    private fun addTitle(
        parent: LinearLayout,
        title: String
    ) {
        val text = TextView(this).apply {
            this.text = title
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(35, 55, 75))
            setPadding(0, dp(14), 0, dp(8))
        }
        parent.addView(text)
    }

    private fun addField(
        parent: LinearLayout,
        label: String,
        value: String
    ): EditText {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), 0, dp(3), dp(8))
        }

        parent.addView(box,
            LinearLayout.LayoutParams(0, dp(85), 1f))

        box.addView(TextView(this).apply {
            text = label
            textSize = 13f
            gravity = Gravity.CENTER
        })

        val edit = EditText(this).apply {
            setText(value)
            textSize = 16f
            gravity = Gravity.CENTER
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine()
        }

        box.addView(edit)
        return edit
    }

    private fun format(value: Double): String {
        return String.format(
            Locale.US, "%.3f", value
        ).trimEnd('0').trimEnd('.')
    }

    private fun calculate() {

        if (!::result.isInitialized) return

        val t = number(thickness)
        val r = number(radius)
        val k = number(kFactor)

        val f = lengths.map { number(it) }

        var cumulative = f[0]
        val values = mutableListOf<Double>()

        for (i in 1..10) {

            cumulative += f[i]

            val bends = i.toDouble()

            val value = if (f[i] == 0.0) {
                0.0
            } else {
                cumulative -
                2 * bends * (t + r) +
                bends * 3.1415 * (r + k * t) / 2
            }

            values.add(value)
        }

        // مطابق اکسل: MAX(G2:G11)
        // مقدار G12 عمداً وارد بیشینه نمی‌شود.

        val maximum = values.take(9).maxOrNull() ?: 0.0

        result.text =
            "طول گسترده ورق: ${format(maximum)}"

        val output = StringBuilder()

        output.append("نتایج طول‌های گسترده\n\n")

        for (i in 0..10) {

            if (i == 0) {
                output.append("L1 : بدون محاسبه\n")
            } else {
                output.append(
                    "L${i + 1} : ${format(values[i - 1])}\n"
                )
            }
        }

        details.text = output.toString()
    }
}
