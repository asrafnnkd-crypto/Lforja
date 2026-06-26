package com.example.data.parser

import com.example.data.models.Channel
import com.example.data.models.Match
import java.io.BufferedReader
import java.io.StringReader

object PlaylistParser {

    /**
     * Parses an M3U playlist file content into a list of Channels.
     */
    fun parseM3U(m3uContent: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(m3uContent))
        var line: String? = reader.readLine()
        
        var currentId = ""
        var currentName = ""
        var currentLogoUrl = ""
        var currentCategory = "ترفيه"
        
        while (line != null) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("#EXTINF:")) {
                // Parse EXTINF metadata
                // Example: #EXTINF:-1 tvg-id="bein1" tvg-logo="http://..." group-title="beIN Sports",beIN Sports MAX 1
                currentName = trimmedLine.substringAfterLast(",").trim()
                
                val tvgIdMatch = Regex("""tvg-id="([^"]+)"""").find(trimmedLine)
                currentId = tvgIdMatch?.groupValues?.get(1) ?: currentName.lowercase().replace(" ", "_")
                
                val tvgLogoMatch = Regex("""tvg-logo="([^"]+)"""").find(trimmedLine)
                currentLogoUrl = tvgLogoMatch?.groupValues?.get(1) ?: ""
                
                val groupTitleMatch = Regex("""group-title="([^"]+)"""").find(trimmedLine)
                currentCategory = groupTitleMatch?.groupValues?.get(1) ?: "قنوات عامة"
            } else if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                // This is the stream URL
                if (currentName.isNotEmpty()) {
                    channels.add(
                        Channel(
                            id = currentId.ifEmpty { trimmedLine.hashCode().toString() },
                            name = currentName,
                            logoUrl = currentLogoUrl,
                            streamUrl = trimmedLine,
                            category = currentCategory,
                            isLive = true,
                            hasTrophy = currentName.contains("MAX", ignoreCase = true) || currentName.contains("Arryadia", ignoreCase = true)
                        )
                    )
                    // Reset
                    currentId = ""
                    currentName = ""
                    currentLogoUrl = ""
                    currentCategory = "ترفيه"
                }
            }
            line = reader.readLine()
        }
        return channels
    }

    /**
     * Simple XMLTV EPG parser extracting current program info and schedules.
     */
    fun parseXMLTV(xmltvContent: String): List<Map<String, String>> {
        val programs = mutableListOf<Map<String, String>>()
        // Simple regex parsing of XMLTV <programme> tags to avoid heavy DOM parsers
        // Example: <programme start="20260625180000 +0000" stop="20260625193000 +0000" channel="bein1"><title lang="ar">مباراة تركيا ضد أمريكا</title><desc lang="ar">مباشر كأس العالم</desc></programme>
        val programmeRegex = Regex("""<programme[^>]*start="([^"]+)"[^>]*stop="([^"]+)"[^>]*channel="([^"]+)"[^>]*>(.*?)</programme>""", RegexOption.DOT_MATCHES_ALL)
        val titleRegex = Regex("""<title[^>]*>(.*?)</title>""")
        val descRegex = Regex("""<desc[^>]*>(.*?)</desc>""")

        programmeRegex.findAll(xmltvContent).forEach { matchResult ->
            val start = matchResult.groupValues[1]
            val stop = matchResult.groupValues[2]
            val channel = matchResult.groupValues[3]
            val innerXml = matchResult.groupValues[4]

            val title = titleRegex.find(innerXml)?.groupValues?.get(1) ?: "بث مباشر"
            val desc = descRegex.find(innerXml)?.groupValues?.get(1) ?: ""

            programs.add(
                mapOf(
                    "channel" to channel,
                    "start" to start,
                    "stop" to stop,
                    "title" to title,
                    "desc" to desc
                )
            )
        }
        return programs
    }
}
