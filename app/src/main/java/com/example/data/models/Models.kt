package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournament: String,
    val team1Name: String,
    val team1Flag: String, // Emoji flag or drawable name
    val team2Name: String,
    val team2Flag: String, // Emoji flag or drawable name
    val score1: String = "0",
    val score2: String = "0",
    val status: String, // e.g. "جارية الآن", "18:00", "انتهت"
    val channelName: String,
    val streamUrl: String,
    val isLive: Boolean = false,
    val commentator: String = "غير معروف",
    val dateString: String = "اليوم"
)

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey val id: String, // e.g. "bein_max_1"
    val name: String,
    val logoUrl: String = "", // Can fall back to a generated gradient or icon
    val streamUrl: String,
    val category: String, // e.g. "beIN Sports", "Moroccan Channels", "Movies"
    val isLive: Boolean = false,
    val currentProgram: String = "بث مباشر للمباراة",
    val hasTrophy: Boolean = false
)

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey val id: String = "main_user",
    val username: String = "المشاهد العربي",
    val email: String = "user@lfraja.ma",
    val membershipType: String = "العضوية المجانية", // e.g. "باقة *6 المغرب", "المميزة"
    val avatarUrl: String = "",
    val token: String = "LF-9875-AZ92"
)

@Entity(tableName = "analytics_logs")
data class AnalyticsLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val channelId: String,
    val channelName: String,
    val viewsCount: Int = 0,
    val bandwidthUsedMb: Double = 0.0
)
