package com.bandapa.feature.venues.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PlacePrediction(
    val description: String,
    @SerialName("place_id") val placeId: String = "",
)

@Serializable
private data class PlacesResponse(
    val predictions: List<PlacePrediction> = emptyList(),
    val status: String = "",
)

@Serializable
data class GeocodedLocation(val lat: Double, val lng: Double)

@Serializable
private data class GeocodingResponse(
    val results: List<GeocodingResult> = emptyList(),
    val status: String = "",
)

@Serializable
private data class GeocodingResult(
    val geometry: GeocodingGeometry,
    @SerialName("formatted_address") val formattedAddress: String = "",
)

@Serializable
private data class GeocodingGeometry(val location: GeocodedLocation)

private val placesJson = Json { ignoreUnknownKeys = true }

class GooglePlacesClient(private val apiKey: String) {

    private val client = HttpClient {
        install(ContentNegotiation) { json(placesJson) }
    }

    suspend fun autocomplete(input: String): List<PlacePrediction> {
        if (input.length < 3) return emptyList()
        return runCatching {
            val response: PlacesResponse = client.get(
                "https://maps.googleapis.com/maps/api/place/autocomplete/json"
            ) {
                parameter("input", input)
                parameter("key", apiKey)
            }.body()
            if (response.status == "OK") response.predictions else emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun geocode(address: String): GeocodedLocation? =
        runCatching {
            val response: GeocodingResponse = client.get(
                "https://maps.googleapis.com/maps/api/geocode/json"
            ) {
                parameter("address", address)
                parameter("key", apiKey)
            }.body()
            response.results.firstOrNull()?.geometry?.location
        }.getOrNull()
}

fun staticMapUrl(lat: Double, lng: Double, apiKey: String, width: Int = 600, height: Int = 200): String =
    "https://maps.googleapis.com/maps/api/staticmap" +
    "?center=$lat,$lng&zoom=15&size=${width}x${height}" +
    "&markers=color:green%7C$lat,$lng" +
    "&style=element:geometry%7Ccolor:0x1b2215" +
    "&style=element:labels.text.fill%7Ccolor:0xe1e3d9" +
    "&key=$apiKey"
