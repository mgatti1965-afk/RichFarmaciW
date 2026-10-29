package com.aistudio.richfarmaciw.util

import com.sun.net.httpserver.HttpServer
import java.awt.Desktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URI
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object GoogleContactsService {

    private const val CLIENT_ID = "1098272982858-desktop-app.apps.googleusercontent.com"
    private const val PORT = 8888
    private const val REDIRECT_URI = "http://localhost:$PORT/oauth2callback"

    private var cachedAccessToken: String? = null
    private var cachedContacts: List<ContactInfo>? = null

    suspend fun fetchContacts(): List<ContactInfo> = withContext(Dispatchers.IO) {
        cachedContacts?.let { return@withContext it }

        val token = cachedAccessToken ?: authenticateAndGetToken()
        if (token.isNullOrBlank()) {
            return@withContext emptyList()
        }

        cachedAccessToken = token
        val contacts = downloadContactsWithToken(token)
        cachedContacts = contacts
        return@withContext contacts
    }

    private fun authenticateAndGetToken(): String? {
        var tokenResult: String? = null
        val lock = Object()

        var server: HttpServer? = null
        try {
            server = HttpServer.create(InetSocketAddress(PORT), 0)
            server.createContext("/oauth2callback") { exchange ->
                val query = exchange.requestURI.query

                var token: String? = null
                if (query != null && query.contains("access_token=")) {
                    token = query.substringAfter("access_token=").substringBefore("&")
                }

                val htmlResponse = """
                    <!DOCTYPE html>
                    <html>
                    <head><title>Autorizzazione Completata</title></head>
                    <body style="font-family: sans-serif; text-align: center; padding-top: 50px; background: #f8fafc;">
                        <h2 style="color: #059669;">Autorizzazione Google completata!</h2>
                        <p>Puoi chiudere questa finestra e tornare a RichFarmaciW.</p>
                        <script>
                            if (window.location.hash && window.location.hash.includes('access_token=')) {
                                var token = window.location.hash.split('access_token=')[1].split('&')[0];
                                window.location.href = 'http://localhost:$PORT/oauth2callback?access_token=' + token;
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                exchange.sendResponseHeaders(200, htmlResponse.toByteArray().size.toLong())
                val os = exchange.responseBody
                os.write(htmlResponse.toByteArray())
                os.close()

                if (!token.isNullOrBlank()) {
                    synchronized(lock) {
                        tokenResult = token
                        lock.notifyAll()
                    }
                }
            }

            server.start()

            val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "response_type=token&" +
                    "client_id=$CLIENT_ID&" +
                    "redirect_uri=$REDIRECT_URI&" +
                    "scope=https://www.googleapis.com/auth/contacts.readonly"

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(authUrl))
            }

            synchronized(lock) {
                lock.wait(60000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { server?.stop(0) } catch (_: Exception) {}
        }

        return tokenResult
    }

    private fun downloadContactsWithToken(token: String): List<ContactInfo> {
        val results = mutableListOf<ContactInfo>()
        try {
            val url = URL("https://people.googleapis.com/v1/people/me/connections?personFields=names,phoneNumbers,emailAddresses&pageSize=1000")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonText = reader.readText()
                reader.close()

                val jsonElement = Json.parseToJsonElement(jsonText)
                val connections = jsonElement.jsonObject["connections"]?.jsonArray ?: return emptyList()

                for (person in connections) {
                    val personObj = person.jsonObject

                    val names = personObj["names"]?.jsonArray
                    var displayName = ""
                    if (names != null && names.isNotEmpty()) {
                        displayName = names[0].jsonObject["displayName"]?.jsonPrimitive?.content ?: ""
                    }

                    var phoneOrEmail = ""
                    val phones = personObj["phoneNumbers"]?.jsonArray
                    if (phones != null && phones.isNotEmpty()) {
                        phoneOrEmail = phones[0].jsonObject["value"]?.jsonPrimitive?.content ?: ""
                    } else {
                        val emails = personObj["emailAddresses"]?.jsonArray
                        if (emails != null && emails.isNotEmpty()) {
                            phoneOrEmail = emails[0].jsonObject["value"]?.jsonPrimitive?.content ?: ""
                        }
                    }

                    if (displayName.isNotBlank() || phoneOrEmail.isNotBlank()) {
                        results.add(ContactInfo(displayName, phoneOrEmail))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }
}
