package com.example.finalprojectrodrigoaugusto.fragments

import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.finalprojectrodrigoaugusto.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherFragment : Fragment() {

    companion object{
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    data class WeatherData(val temperature: Double, val description: String, val conditionCode: Int)
    data class DistrictDate(val city: String, val district: String)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation(view)
        }else{
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }

        return view;
    }

    private fun getWeatherIcon(conditionCode: Int): String{
        return when(conditionCode){
            in 200..232 -> "wi_thunderstorm"
            in 300..321 -> "wi_showers"
            in 500..531 -> "wi_rain"
            in 600..622 -> "wi_snow"
            in 701..781 -> "wi_fog"
            800 -> "wi_day_sunny"
            801 -> "wi_day_cloudy"
            802 -> "wi_cloudy"
            803, 804 -> "wi_day_cloudy_high"
            1183 -> "wi_day_light_wind"
            else -> "wi_day_sunny"
        }
    }

    private fun getWeatherColor(conditionCode: Int): String{
        return when(conditionCode){
            in 200..232 -> "#637E90"
            in 300..321 -> "#29B3FF"
            in 500..531 -> "#14C2DD"
            in 600..622 -> "#E5F2F0"
            in 701..781 -> "#FFFEA8"
            800 -> "#FBC740"
            801 -> "#BCECE0"
            802 -> "#BCECE0"
            803, 804 -> "#36EEE0"
            1183 -> "#14C2DD"
            else -> "#FBC740"
        }
    }

    private suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherData{
        val apiKey = "c5f719b3a46245ff906190214233103"
        val url = "https://api.weatherapi.com/v1/current.json?lang=pt&key=$apiKey&q=$latitude,$longitude"
        val jsonText = withContext(Dispatchers.IO) { URL(url).readText() }
        val jsonObject = JSONObject(jsonText)
        val current = jsonObject.getJSONObject("current")
        val temperature = current.getDouble("temp_c")
        val description = current.getJSONObject("condition").getString("text")
        val conditionCode = current.getJSONObject("condition").getInt("code")

        return WeatherData(temperature, description, conditionCode)
    }

    private suspend fun getCityDistrict(latitude: Double, longitude: Double): DistrictDate{
        val url = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=pt"
        val jsonText = withContext(Dispatchers.IO) { URL(url).readText() }
        val jsonObject = JSONObject(jsonText)
        val city = jsonObject.getString("city")
        val district = jsonObject.getString("locality")

        return DistrictDate(city, district)
    }

    private fun updateUi(weatherData: WeatherData, cityDistrict: DistrictDate, view: View){
        val temperatureTextView = view.findViewById<TextView>(R.id.weather_fragment_txt_temperature)
        val descriptionTextView = view.findViewById<TextView>(R.id.weather_fragment_txt_description)
        val neighborhoodTextView = view.findViewById<TextView>(R.id.weather_fragment_txt_neighborhood)

        temperatureTextView.text = "${weatherData.temperature} °C"
        descriptionTextView.text = weatherData.description
        neighborhoodTextView.text = "${cityDistrict.district}, ${cityDistrict.city}"

        val imageView = view.findViewById<ImageView>(R.id.weather_fragment_img_view)
        val drawableId = resources.getIdentifier(getWeatherIcon(weatherData.conditionCode), "drawable", requireContext().packageName)
        imageView.setImageResource(drawableId)

        val hexColor = getWeatherColor(weatherData.conditionCode)
        val color = Color.parseColor(hexColor)
        imageView.setColorFilter(color)
    }

    private fun getLocation(view: View){
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, object:
            LocationListener{
            override fun onLocationChanged(location: Location) {
                lifecycleScope.launch{
                    val weatherData = getWeatherData(location.latitude, location.longitude)
                    val cityDistrict = getCityDistrict(location.latitude, location.longitude)

                    updateUi(weatherData, cityDistrict, view)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        })
    }

}