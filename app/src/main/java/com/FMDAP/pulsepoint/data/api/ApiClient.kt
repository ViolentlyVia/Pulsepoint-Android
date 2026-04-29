package com.FMDAP.pulsepoint.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ApiClient(val baseUrl: String, private val apiKey: String) {

    val cookieJar = InMemoryCookieJar()

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("X-Api-Key", apiKey)
                .build()
            chain.proceed(req)
        }
        .build()

    val api: PulsePointApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PulsePointApi::class.java)

    suspend fun login(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // GET the login page first to pick up any CSRF token
            val loginUrl = "${baseUrl.trimEnd('/')}/managelogin"
            val getReq = Request.Builder().url(loginUrl).get().build()
            val getResp = okHttpClient.newCall(getReq).await()
            val html = getResp.body?.string() ?: ""
            getResp.close()

            val csrfToken = Regex("""__RequestVerificationToken[^>]+value="([^"]+)"""")
                .find(html)?.groupValues?.getOrNull(1)

            val formBody = FormBody.Builder()
                .add("password", password)
                .apply { if (csrfToken != null) add("__RequestVerificationToken", csrfToken) }
                .build()

            val postReq = Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build()

            val postResp = okHttpClient.newCall(postReq).await()
            val code = postResp.code
            postResp.close()

            if (!cookieJar.hasSession() && code !in 200..399) {
                throw IOException("Login failed (HTTP $code)")
            }
        }
    }

    fun logout() = cookieJar.clear()
}

private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) = cont.resumeWithException(e)
        override fun onResponse(call: Call, response: Response) = cont.resume(response)
    })
    cont.invokeOnCancellation { cancel() }
}
