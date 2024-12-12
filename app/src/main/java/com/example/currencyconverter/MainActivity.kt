package com.example.currencyconverter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var baseCurrency = "EUR"
    private var convertedCurrency = "USD"
    private var currencyRates = mutableMapOf<String, Double>()

    private lateinit var amountInput: EditText
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountInput = findViewById(R.id.amountInput)
        resultText = findViewById(R.id.resultText)
        val convertButton: Button = findViewById(R.id.convertButton)
        val swapButton: Button = findViewById(R.id.swapButton)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.itemIconTintList = null


        fetchExchangeRates() // Ambil data dari API



        // Handle tombol convert
        convertButton.setOnClickListener {
            if (amountInput.text.isNotEmpty()) {
                convertCurrency()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle tombol swap
        swapButton.setOnClickListener {
            swapCurrencies()
        }

        // Handle navigasi bottom bar
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_note -> {
                    val intent = Intent(this, NoteActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_calculator -> {
                    val intent = Intent(this, CalculatorActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_chart -> {
                    val intent = Intent(this, ChartActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContent, fragment)
            .commit()
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun convertCurrency() {
        if (baseCurrency == convertedCurrency) {
            resultText.text = "${amountInput.text} $baseCurrency"
            return
        }

        try {
            val input = amountInput.text.toString().toDouble()
            val baseRate = currencyRates[baseCurrency] ?: return
            val targetRate = currencyRates[convertedCurrency] ?: return

            val result = (targetRate / baseRate) * input
            resultText.text = "%.2f $convertedCurrency".format(result)
        } catch (e: Exception) {
            Log.e("Conversion", "Error converting: ${e.message}")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchExchangeRates() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val API = "https://api.currencyfreaks.com/latest?apikey=a5ddf94800f54361a6d3ad1210b4658c"
                val response = URL(API).readText()
                val jsonObject = JSONObject(response)
                val rates = jsonObject.getJSONObject("rates")

                currencyRates.clear()
                rates.keys().forEach { currency ->
                    currencyRates[currency] = rates.getDouble(currency)
                }

                val currencies = rates.keys().asSequence().toList()

                withContext(Dispatchers.Main) {
                    updateCurrencySpinners(currencies)
                }
            } catch (e: Exception) {
                Log.e("API", "Error fetching rates: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Error fetching exchange rates: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateCurrencySpinners(currencies: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val fromCurrencySpinner: Spinner = findViewById(R.id.fromCurrencySpinner)
        val toCurrencySpinner: Spinner = findViewById(R.id.toCurrencySpinner)

        fromCurrencySpinner.adapter = adapter
        toCurrencySpinner.adapter = adapter

        fromCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        toCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                convertedCurrency = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun swapCurrencies() {
        val temp = baseCurrency
        baseCurrency = convertedCurrency
        convertedCurrency = temp

        val fromSpinner: Spinner = findViewById(R.id.fromCurrencySpinner)
        val toSpinner: Spinner = findViewById(R.id.toCurrencySpinner)

        fromSpinner.setSelection((fromSpinner.adapter as ArrayAdapter<String>).getPosition(baseCurrency))
        toSpinner.setSelection((toSpinner.adapter as ArrayAdapter<String>).getPosition(convertedCurrency))
    }
}
