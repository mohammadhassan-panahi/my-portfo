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
 * DTOs for the Tehran Stock Exchange (TSETMC) data, fetched through OUR Cloudflare Worker
 * proxy (see /brsapi-proxy) rather than BrsApi.ir directly — the real BrsApi key lives only
 * as a Worker secret, never inside this APK.
 *
 * Field names — VERIFIED 2026-08-09 against a real captured AllSymbols.php response:
 *   l18=symbol, l30=full name, pl=last price, pc=closing price, pcp=closing price change %,
 *   pd1=best bid (buy queue), po1=best ask (sell queue). Prices are already in RIAL (confirmed
 * via pl/eps ≈ pe on a real فولاد row) — do NOT multiply by RIAL_PER_TOMAN for this service.
 * The Index endpoint's exact field names are still unverified — same caution applies there.
 */
@JsonClass(generateAdapter = true)
data class TsetmcIndexDto(
    @Json(name = "index") val index: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: Double? = null,
    @Json(name = "change_percent") val changePercent: Double? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "time") val time: String? = null
)

@JsonClass(generateAdapter = true)
data class TsetmcSymbolDto(
    @Json(name = "l18") val symbol: String? = null,        // نماد کوتاه، مثلاً "فولاد"
    @Json(name = "l30") val fullName: String? = null,       // نام کامل، مثلاً "فولاد مبارکه اصفهان"
    @Json(name = "pl") val lastPrice: Double? = null,        // آخرین قیمت معامله (به ریال)
    @Json(name = "pc") val closingPrice: Double? = null,     // قیمت پایانی (به ریال)
    @Json(name = "pcp") val changePercent: Double? = null,   // درصد تغییر قیمت پایانی
    @Json(name = "pd1") val buyPrice: Double? = null,         // بهترین قیمت خرید صف اول (به ریال)
    @Json(name = "po1") val sellPrice: Double? = null,        // بهترین قیمت فروش صف اول (به ریال)
    @Json(name = "tno") val tradeCount: Long? = null,
    @Json(name = "successful") val successful: Boolean? = null,
    @Json(name = "message_error") val messageError: String? = null
) {
    /** True only if the API actually returned a usable price — NOT just "no error field present". */
    val hasValidPrice: Boolean get() = successful != false && closingPrice != null && closingPrice!! > 0.0
}

interface TsetmcApiService {
    @GET("tsetmc/all-symbols")
    suspend fun getAllSymbols(): Response<List<TsetmcSymbolDto>>

    @GET("tsetmc/index")
    suspend fun getIndices(): Response<List<TsetmcIndexDto>>

    companion object {
        fun create(proxyBaseUrl: String): TsetmcApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            val normalizedBaseUrl = if (proxyBaseUrl.endsWith("/")) proxyBaseUrl else "$proxyBaseUrl/"
            return Retrofit.Builder()
                .baseUrl(normalizedBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(TsetmcApiService::class.java)
        }
    }
}

/** Thin wrapper kept for call-site compatibility with the rest of the app (PortfolioRepository). */
class TsetmcApiClient(private val service: TsetmcApiService) {
    suspend fun getIndices(): Response<List<TsetmcIndexDto>> = service.getIndices()
    suspend fun getAllSymbols(): Response<List<TsetmcSymbolDto>> = service.getAllSymbols()
}
