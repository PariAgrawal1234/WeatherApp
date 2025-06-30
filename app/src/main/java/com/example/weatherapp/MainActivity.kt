package com.example.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.SearchView
import java.util.concurrent.locks.Condition

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchWeatherData("mathura")
        SearchCity()
    }

    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object: android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityname:String){
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(Api_Interface::class.java)
        val response = retrofit.getWeatherData(cityname, "cb7684adf03a2bba392495bce25fd557", "metric")
        response.enqueue(object : Callback<WeatherApp>{
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if(response.isSuccessful && responseBody!= null) {
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity.toString()
                    val windspeed = responseBody.wind.speed.toString()
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val sealevel = responseBody.main.sea_level.toString()
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                    val maxtemp = responseBody.main.temp_max
                    val mintemp = responseBody.main.temp_min

                    binding.City.text = cityname
                    binding.Windspeed.text = "$windspeed m/s"
                    binding.Condition.text = condition
                    binding.MaxTemp.text = "$maxtemp°C"
                    binding.attributeCondtn.text = condition
                    binding.MinTemp.text = "$mintemp°C"
                    binding.sunrise.text = time(sunrise)
                    binding.sunset.text = time(sunset)
                    binding.sealevel.text = "$sealevel hPa"
                    binding.Humidity.text = "$humidity %"
                    binding.Temperature.text = temperature
                    binding.Day.text = dayName(System.currentTimeMillis())
                    binding.Date.text = date()
                    //Log.d("TAG", "onResponse : $temperature")
                    ChangeImage(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("TAG", "Network call failed: ${t.message}")
            }

        })
}

    private fun ChangeImage(Conditions : String) {
        when (Conditions){
            "Clouds", "Haze", "Partly Clouds", "Overcast", "mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
             "Rain", "Light Rain", "Heavy Rain", "Moderate Rain", "Showers", "Drizzle", "Showers" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Clear", "Sunny", "Clear Sky" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Snow", "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun time(timestamp :Long): String {
        val sdf = SimpleDateFormat("HH:MM", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }

    fun dayName(timestamp :Long):String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}
