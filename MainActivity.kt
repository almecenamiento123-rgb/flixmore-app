package com.flixmore.app

import android.content.Context
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnAdBlock: Button
    private var adClickCount = 0

    // Redes publicitarias a interceptar cuando el bloqueo está activo
    private val adDomains = listOf(
        "doubleclick.net",
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "adsterra.com",
        "popads.net",
        "exoclick.com",
        "juicyads.com",
        "propellerads.com",
        "adform.net"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        btnAdBlock = findViewById(R.id.btnAdBlock)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: ""

                if (isAdBlockActive() && isAdUrl(url)) {
                    // Bloquea el anuncio retornando una respuesta vacía
                    return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        // Carga de la página de FlixMore
        webView.loadUrl("https://flixmore.blogspot.com")

        actualizarEstadoBoton()

        btnAdBlock.setOnClickListener {
            if (isAdBlockActive()) {
                Toast.makeText(this, "El bloqueador ya está activo por 24 horas", Toast.LENGTH_SHORT).show()
            } else {
                adClickCount++
                if (adClickCount >= 4) {
                    activarBloqueador24Horas()
                    adClickCount = 0
                } else {
                    Toast.makeText(this, "Anuncio $adClickCount de 4 completado", Toast.LENGTH_SHORT).show()
                }
                actualizarEstadoBoton()
            }
        }
    }

    private fun isAdUrl(url: String): Boolean {
        for (domain in adDomains) {
            if (url.contains(domain)) {
                return true
            }
        }
        return false
    }

    private fun isAdBlockActive(): Boolean {
        val prefs = getSharedPreferences("FlixMorePrefs", Context.MODE_PRIVATE)
        val expiracion = prefs.getLong("bloqueo_expiracion", 0)
        return System.currentTimeMillis() < expiracion
    }

    private fun activarBloqueador24Horas() {
        val prefs = getSharedPreferences("FlixMorePrefs", Context.MODE_PRIVATE)
        val tiempoExpiracion = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
        prefs.edit().putLong("bloqueo_expiracion", tiempoExpiracion).apply()
        Toast.makeText(this, "¡Bloqueador activado por 24 horas! Recargando...", Toast.LENGTH_LONG).show()
        webView.reload()
    }

    private fun actualizarEstadoBoton() {
        if (isAdBlockActive()) {
            val prefs = getSharedPreferences("FlixMorePrefs", Context.MODE_PRIVATE)
            val expiracion = prefs.getLong("bloqueo_expiracion", 0)
            val horasRestantes = ((expiracion - System.currentTimeMillis()) / (1000 * 60 * 60)).toInt()
            btnAdBlock.text = "🛡️ Bloqueador Activo ($horasRestantes h restantes)"
            btnAdBlock.setBackgroundColor(android.graphics.Color.GREEN)
        } else {
            btnAdBlock.text = "🎬 Ver Anuncio para Bloquear ($adClickCount/4)"
            btnAdBlock.setBackgroundColor(android.graphics.Color.RED)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
