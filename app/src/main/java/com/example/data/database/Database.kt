package com.example.data.database

import androidx.room.*
import com.example.data.models.Match
import com.example.data.models.Channel
import com.example.data.models.UserAccount
import com.example.data.models.AnalyticsLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY isLive DESC, id ASC")
    fun getAllMatches(): Flow<List<Match>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<Match>)

    @Update
    suspend fun updateMatch(match: Match)

    @Delete
    suspend fun deleteMatch(match: Match)

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteMatchById(id: Int)

    @Query("DELETE FROM matches")
    suspend fun clearAllMatches()
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: String): Channel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Update
    suspend fun updateChannel(channel: Channel)

    @Delete
    suspend fun deleteChannel(channel: Channel)

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannelById(id: String)

    @Query("DELETE FROM channels")
    suspend fun clearAllChannels()
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_account WHERE id = 'main_user' LIMIT 1")
    fun getUserAccount(): Flow<UserAccount?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount)
}

@Dao
interface AnalyticsLogDao {
    @Query("SELECT * FROM analytics_logs ORDER BY timestamp DESC")
    fun getAnalyticsLogs(): Flow<List<AnalyticsLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsLog(log: AnalyticsLog)

    @Query("DELETE FROM analytics_logs")
    suspend fun clearAnalytics()
}

@Database(
    entities = [Match::class, Channel::class, UserAccount::class, AnalyticsLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun channelDao(): ChannelDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun analyticsLogDao(): AnalyticsLogDao
}

class LfrajaRepository(private val db: AppDatabase) {
    val matches: Flow<List<Match>> = db.matchDao().getAllMatches()
    val channels: Flow<List<Channel>> = db.channelDao().getAllChannels()
    val userAccount: Flow<UserAccount?> = db.userAccountDao().getUserAccount()
    val analyticsLogs: Flow<List<AnalyticsLog>> = db.analyticsLogDao().getAnalyticsLogs()

    suspend fun insertMatch(match: Match) = db.matchDao().insertMatch(match)
    suspend fun updateMatch(match: Match) = db.matchDao().updateMatch(match)
    suspend fun deleteMatchById(id: Int) = db.matchDao().deleteMatchById(id)
    suspend fun clearMatches() = db.matchDao().clearAllMatches()

    suspend fun insertChannel(channel: Channel) = db.channelDao().insertChannel(channel)
    suspend fun getChannelById(id: String): Channel? = db.channelDao().getChannelById(id)
    suspend fun updateChannel(channel: Channel) = db.channelDao().updateChannel(channel)
    suspend fun deleteChannelById(id: String) = db.channelDao().deleteChannelById(id)
    suspend fun clearChannels() = db.channelDao().clearAllChannels()

    suspend fun insertUserAccount(user: UserAccount) = db.userAccountDao().insertUserAccount(user)
    suspend fun insertAnalyticsLog(log: AnalyticsLog) = db.analyticsLogDao().insertAnalyticsLog(log)

    suspend fun prePopulateDefaultData() {
        // Pre-populate user
        db.userAccountDao().insertUserAccount(
            UserAccount(
                id = "main_user",
                username = "أشرف المغربي",
                email = "asrafnnkd@gmail.com",
                membershipType = "باقة نجمة 6 المغربية (*6)",
                avatarUrl = ""
            )
        )

        // Pre-populate matches
        val defaultMatches = listOf(
            Match(
                id = 1,
                tournament = "كأس العالم 2026 - الجولة 2",
                team1Name = "أمريكا",
                team1Flag = "🇺🇸",
                team2Name = "تركيا",
                team2Flag = "🇹🇷",
                score1 = "1",
                score2 = "2",
                status = "جارية الآن",
                channelName = "beIN Sports MAX 1",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                isLive = true,
                commentator = "محمد مبروكي",
                dateString = "اليوم"
            ),
            Match(
                id = 2,
                tournament = "كأس العالم 2026 - الجولة 3",
                team1Name = "بارغواي",
                team1Flag = "🇵🇾",
                team2Name = "أستراليا",
                team2Flag = "🇦🇺",
                score1 = "0",
                score2 = "0",
                status = "جارية الآن",
                channelName = "beIN Sports MAX 2",
                streamUrl = "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8",
                isLive = true,
                commentator = "خليل البلوشي",
                dateString = "اليوم"
            ),
            Match(
                id = 3,
                tournament = "كأس العالم 2026 - الجولة 3",
                team1Name = "المغرب",
                team1Flag = "🇲🇦",
                team2Name = "فرنسا",
                team2Flag = "🇫🇷",
                score1 = "0",
                score2 = "0",
                status = "21:00",
                channelName = "Arryadia TNT",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                isLive = false,
                commentator = "هشام فرج",
                dateString = "الليلة"
            )
        )
        db.matchDao().insertMatches(defaultMatches)

        // Pre-populate channels (Matching Image 2 exactly)
        val defaultChannels = listOf(
            Channel(
                id = "bein_max_1",
                name = "beIN Sports MAX 1",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                category = "beIN Sports",
                isLive = true,
                currentProgram = "كأس العالم: أمريكا ضد تركيا",
                hasTrophy = true
            ),
            Channel(
                id = "bein_max_2",
                name = "beIN Sports MAX 2",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8",
                category = "beIN Sports",
                isLive = true,
                currentProgram = "كأس العالم: بارغواي ضد أستراليا",
                hasTrophy = true
            ),
            Channel(
                id = "bein_max_3",
                name = "beIN Sports MAX 3",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                category = "beIN Sports",
                isLive = false,
                currentProgram = "أستوديو تحليلي",
                hasTrophy = true
            ),
            Channel(
                id = "bein_max_4",
                name = "beIN Sports MAX 4",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8",
                category = "beIN Sports",
                isLive = false,
                currentProgram = "ملخص مباريات الأمس",
                hasTrophy = true
            ),
            Channel(
                id = "bein_max_5",
                name = "beIN Sports MAX 5",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                category = "beIN Sports",
                isLive = false,
                currentProgram = "كرة السلة العالمية",
                hasTrophy = true
            ),
            Channel(
                id = "bein_max_6",
                name = "beIN Sports MAX 6",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8",
                category = "beIN Sports",
                isLive = false,
                currentProgram = "تغطية كأس العالم الشاطئية",
                hasTrophy = true
            ),
            Channel(
                id = "bein_news",
                name = "beIN Sports NEWS",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                category = "beIN Sports",
                isLive = true,
                currentProgram = "النشرة الرياضية الإخبارية",
                hasTrophy = false
            ),
            Channel(
                id = "arryadia",
                name = "Arryadia TNT",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Arryadia_logo.svg/512px-Arryadia_logo.svg.png",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                category = "المغربية",
                isLive = true,
                currentProgram = "مباراة المنتخب الوطني المغربي مباشر",
                hasTrophy = true
            ),
            Channel(
                id = "aloula",
                name = "Aloula",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/Al_Aoula.svg/512px-Al_Aoula.svg.png",
                streamUrl = "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8",
                category = "المغربية",
                isLive = true,
                currentProgram = "الأخبار المسائية لثامنة مساءً",
                hasTrophy = false
            ),
            Channel(
                id = "2m",
                name = "2M",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/Logo_2M_Maroc.png/320px-Logo_2M_Maroc.png",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                category = "المغربية",
                isLive = true,
                currentProgram = "مسلسلات وسهرات مغربية",
                hasTrophy = false
            ),
            Channel(
                id = "bein_movies_1",
                name = "beIN Movies 1",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8",
                category = "ترفيه وأفلام",
                isLive = false,
                currentProgram = "Premiere Action Movie",
                hasTrophy = false
            ),
            Channel(
                id = "bein_movies_2",
                name = "beIN Movies 2",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png",
                streamUrl = "https://demo.unified-streaming.com/k8s/live/stable/sintel.smil/.m3u8",
                category = "ترفيه وأفلام",
                isLive = false,
                currentProgram = "Premiere Action Movie 2",
                hasTrophy = false
            )
        )
        db.channelDao().insertChannels(defaultChannels)

        // Pre-populate basic viewership log data for AI analytics
        val sampleLogs = listOf(
            AnalyticsLog(channelId = "bein_max_1", channelName = "beIN Sports MAX 1", viewsCount = 1420, bandwidthUsedMb = 34500.0),
            AnalyticsLog(channelId = "bein_max_2", channelName = "beIN Sports MAX 2", viewsCount = 980, bandwidthUsedMb = 21000.0),
            AnalyticsLog(channelId = "bein_max_3", channelName = "beIN Sports MAX 3", viewsCount = 450, bandwidthUsedMb = 8700.0),
            AnalyticsLog(channelId = "arryadia", channelName = "Arryadia TNT", viewsCount = 1850, bandwidthUsedMb = 41200.0),
            AnalyticsLog(channelId = "2m", channelName = "2M", viewsCount = 670, bandwidthUsedMb = 12400.0)
        )
        for (log in sampleLogs) {
            db.analyticsLogDao().insertAnalyticsLog(log)
        }
    }
}
