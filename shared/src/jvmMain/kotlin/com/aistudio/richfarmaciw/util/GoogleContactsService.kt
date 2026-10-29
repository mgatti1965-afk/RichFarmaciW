package com.aistudio.richfarmaciw.util

import java.awt.Desktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URI
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object GoogleContactsService {

    var userClientId: String = ""
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
        val clientId = userClientId.trim().ifBlank {
            "1098272982858-desktop-app.apps.googleusercontent.com"
        }

        var tokenResult: String? = null
        var serverSocket: ServerSocket? = null

        try {
            serverSocket = ServerSocket(PORT, 1, InetAddress.getByName("localhost"))
            serverSocket.soTimeout = 60000

            val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "response_type=token&" +
                    "client_id=$clientId&" +
                    "redirect_uri=$REDIRECT_URI&" +
                    "scope=https://www.googleapis.com/auth/contacts.readonly"

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(authUrl))
            }

            val clientSocket = serverSocket.accept()
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val out = PrintWriter(clientSocket.getOutputStream(), true)

            val requestLine = reader.readLine() ?: ""
            
            var token: String? = null
            if (requestLine.contains("access_token=")) {
                token = requestLine.substringAfter("access_token=").substringBefore("&").substringBefore(" ")
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
                            var t = window.location.hash.split('access_token=')[1].split('&')[0];
                            window.location.href = 'http://localhost:$PORT/oauth2callback?access_token=' + t;
                        }
                    </script>
                </body>
                </html>
            """.trimIndent()

            out.println("HTTP/1.1 200 OK")
            out.println("Content-Type: text/html; charset=UTF-8")
            out.println("Content-Length: " + htmlResponse.toByteArray(Charsets.UTF_8).size)
            out.println("Connection: close")
            out.println()
            out.println(htmlResponse)
            out.flush()

            clientSocket.close()

            if (token.isNullOrBlank()) {
                val clientSocket2 = serverSocket.accept()
                val reader2 = BufferedReader(InputStreamReader(clientSocket2.getInputStream()))
                val out2 = PrintWriter(clientSocket2.getOutputStream(), true)

                val requestLine2 = reader2.readLine() ?: ""
                if (requestLine2.contains("access_token=")) {
                    token = requestLine2.substringAfter("access_token=").substringBefore("&").substringBefore(" ")
                }

                out2.println("HTTP/1.1 200 OK")
                out2.println("Content-Type: text/html; charset=UTF-8")
                out2.println("Content-Length: " + htmlResponse.toByteArray(Charsets.UTF_8).size)
                out2.println("Connection: close")
                out2.println()
                out2.println(htmlResponse)
                out2.flush()

                clientSocket2.close()
            }

            tokenResult = token
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { serverSocket?.close() } catch (_: Exception) {}
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
