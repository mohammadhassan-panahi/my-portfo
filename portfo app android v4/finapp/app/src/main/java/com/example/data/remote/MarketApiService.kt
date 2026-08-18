package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

/**
 * Data Transfer Objects for the BrsApi.ir "Gold_Currency" free market webservice.
 * Docs: https://brsapi.ir/free-api-gold-currency-webservice/
 *
 * Real sample response (captured from the live endpoint):
 * {
 *   "gold": [ { "date":"1405/05/15", "time":"16:59", "time_unix":1786022992,
 *               "symbol":"IR_GOLD_18K", "name_en":"18K Gold", "name":"طلای 18 عیار",
 *               "price":18578200, "change_value":16600, "change_percent":0.09, "unit":"تومان" }, ... ],
 *   "currency": [ ... same shape as gold ... ],
 *   "cryptocurrency": [ { ..., "price":"64440" (STRING, not a number!), "market_cap":..., "description":"..." }, ... ]
 * }
 *
 * Note the "gold" and "currency" arrays report `price` as a JSON number, while
 * "cryptocurrency" reports `price` as a JSON *string* — the DTOs below reflect that
 * (verified against a real captured response, not assumed).
 */
@JsonClass(generateAdapter = true)
data class BrsApiRateDto(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: Double,
    @Json(name = "change_value") val changeValue: Double? = null,
    @Json(name = "change_percent") val changePercent: Double = 0.0,
    @Json(name = "unit") val unit: String,
    @Json(name = "date") val date: String? = null,
    @Json(name = "time") val time: String? = null,
    @Json(name = "time_unix") val timeUnix: Long? = null
)

@JsonClass(generateAdapter = true)
data class BrsApiCryptoDto(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: String, // BrsApi returns this as a string, e.g. "64440"
    @Json(name = "change_percent") val changePercent: Double = 0.0,
    @Json(name = "market_cap") val marketCap: Double? = null,
    @Json(name = "unit") val unit: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "time") val time: String? = null,
    @Json(name = "time_unix") val timeUnix: Long? = null
)

@JsonClass(generateAdapter = true)
data class BrsApiGoldCurrencyResponse(
    @Json(name = "gold") val gold: List<BrsApiRateDto> = emptyList(),
    @Json(name = "currency") val currency: List<BrsApiRateDto> = emptyList(),
    @Json(name = "cryptocurrency") val cryptocurrency: List<BrsApiCryptoDto> = emptyList(),
    // Present only on error responses, e.g. {"code_http":401,"successful":false,"status":"unauthorized","message_error":"..."}
    @Json(name = "successful") val successful: Boolean? = null,
    @Json(name = "message_error") val messageError: String? = null
)

/**
 * Retrofit API Service Interface for the gold/currency/crypto rates.
 * IMPORTANT: this hits OUR Cloudflare Worker proxy (see /brsapi-proxy), not BrsApi.ir
 * directly — the real BrsApi key lives only as a Worker secret, never inside this APK.
 * See /brsapi-proxy/README.md for how to deploy the worker and set BuildConfig.PROXY_BASE_URL.
 */
interface MarketApiService {

    @GET("gold-currency")
    suspend fun getGoldCurrency(): Response<BrsApiGoldCurrencyResponse>

    companion object {
        fun create(proxyBaseUrl: String): MarketApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            // Trailing slash required by Retrofit's baseUrl contract.
            val normalizedBaseUrl = if (proxyBaseUrl.endsWith("/")) proxyBaseUrl else "$proxyBaseUrl/"

            return Retrofit.Builder()
                .baseUrl(normalizedBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(MarketApiService::class.java)
        }
    }
}
