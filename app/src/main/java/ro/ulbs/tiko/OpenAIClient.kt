package ro.ulbs.tiko

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object OpenAIClient {

    private const val BASE_URL = "https://api.openai.com/"

    fun create(): OpenAIService {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor())
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .also {
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    it.addInterceptor(logging)
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIService::class.java)
    }
}

/**
 * An interceptor that retries a failed request up to 3 times on network errors.
 */
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount < maxRetries && (response == null || !response.isSuccessful)) {
            try {
                response = chain.proceed(request)

                // Don't retry on client or server errors (4xx, 5xx)
                if (response.isSuccessful) {
                    return response
                }

            } catch (e: IOException) {
                exception = e
            }

            tryCount++
        }

        // If the request never succeeded, throw the last-seen exception.
        if (response == null && exception != null) {
            throw exception
        }

        return response!!
    }
}
