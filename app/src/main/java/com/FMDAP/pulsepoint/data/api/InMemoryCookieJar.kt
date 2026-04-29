package com.FMDAP.pulsepoint.data.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class InMemoryCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { new ->
            this.cookies.removeAll { it.name == new.name && it.domain == new.domain }
            this.cookies.add(new)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        cookies.filter { it.matches(url) }

    fun hasSession(): Boolean = cookies.any { it.name == "pp_session" }

    fun clear() = cookies.clear()
}
