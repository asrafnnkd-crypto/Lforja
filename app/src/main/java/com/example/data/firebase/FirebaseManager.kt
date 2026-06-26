package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.database.LfrajaRepository
import com.example.data.models.Channel
import com.example.data.models.Match
import com.example.data.models.UserAccount
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isFirebaseAvailable = false
    private var repository: LfrajaRepository? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _connectionStatus = MutableStateFlow("مستعد (قاعدة البيانات المحلية Room نشطة)")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _firebaseUser = MutableStateFlow<UserAccount?>(null)
    val firebaseUser: StateFlow<UserAccount?> = _firebaseUser.asStateFlow()

    fun init(context: Context, repo: LfrajaRepository) {
        repository = repo
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings

            isFirebaseAvailable = true
            _connectionStatus.value = "متصل بقاعدة Firebase المباشرة (الوضع الآمن)"
            Log.d(TAG, "Firebase initialized successfully.")

            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInAnonymously()
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: "anonymous_user"
                        Log.d(TAG, "Auth: Signed in anonymously: $uid")
                        setupUserProfileSync(uid)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Auth sign-in failed: ${e.message}")
                        setupUserProfileSync("anonymous_user")
                    }
            } else {
                setupUserProfileSync(auth.currentUser?.uid ?: "anonymous_user")
            }

            startRealtimeSync()
        } catch (e: Exception) {
            isFirebaseAvailable = false
            _connectionStatus.value = "قاعدة Room المحلية (وضع عدم الاتصال الافتراضي)"
            Log.e(TAG, "Firebase fallback to local Room: ${e.message}")
        }
    }

    fun isAvailable(): Boolean = isFirebaseAvailable

    private fun startRealtimeSync() {
        if (!isFirebaseAvailable || repository == null) return
        val db = FirebaseFirestore.getInstance()

        db.collection("matches")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Matches listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    scope.launch {
                        for (doc in snapshots.documentChanges) {
                            val data = doc.document.data
                            val id = (data["id"] as? Long)?.toInt() ?: doc.document.id.toIntOrNull() ?: 0
                            val match = Match(
                                id = id,
                                tournament = data["tournament"] as? String ?: "",
                                team1Name = data["team1Name"] as? String ?: "",
                                team1Flag = data["team1Flag"] as? String ?: "🏳️",
                                team2Name = data["team2Name"] as? String ?: "",
                                team2Flag = data["team2Flag"] as? String ?: "🏳️",
                                score1 = data["score1"] as? String ?: "0",
                                score2 = data["score2"] as? String ?: "0",
                                status = data["status"] as? String ?: "انتهت",
                                channelName = data["channelName"] as? String ?: "",
                                streamUrl = data["streamUrl"] as? String ?: "",
                                isLive = data["isLive"] as? Boolean ?: false,
                                commentator = data["commentator"] as? String ?: "غير معروف",
                                dateString = data["dateString"] as? String ?: "اليوم"
                            )
                            when (doc.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    repository?.insertMatch(match)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    repository?.deleteMatchById(match.id)
                                }
                            }
                        }
                    }
                }
            }

        db.collection("channels")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Channels listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    scope.launch {
                        for (doc in snapshots.documentChanges) {
                            val data = doc.document.data
                            val channel = Channel(
                                id = doc.document.id,
                                name = data["name"] as? String ?: "",
                                logoUrl = data["logoUrl"] as? String ?: "",
                                streamUrl = data["streamUrl"] as? String ?: "",
                                category = data["category"] as? String ?: "عامة",
                                isLive = data["isLive"] as? Boolean ?: false,
                                currentProgram = data["currentProgram"] as? String ?: "بث مباشر",
                                hasTrophy = data["hasTrophy"] as? Boolean ?: false
                            )
                            when (doc.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    repository?.insertChannel(channel)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    repository?.deleteChannelById(channel.id)
                                }
                            }
                        }
                    }
                }
            }
    }

    fun publishMatch(match: Match) {
        if (!isFirebaseAvailable) return
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "id" to match.id,
            "tournament" to match.tournament,
            "team1Name" to match.team1Name,
            "team1Flag" to match.team1Flag,
            "team2Name" to match.team2Name,
            "team2Flag" to match.team2Flag,
            "score1" to match.score1,
            "score2" to match.score2,
            "status" to match.status,
            "channelName" to match.channelName,
            "streamUrl" to match.streamUrl,
            "isLive" to match.isLive,
            "commentator" to match.commentator,
            "dateString" to match.dateString
        )
        db.collection("matches").document(match.id.toString()).set(data)
    }

    fun removeMatch(matchId: Int) {
        if (!isFirebaseAvailable) return
        val db = FirebaseFirestore.getInstance()
        db.collection("matches").document(matchId.toString()).delete()
    }

    fun publishChannel(channel: Channel) {
        if (!isFirebaseAvailable) return
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "name" to channel.name,
            "logoUrl" to channel.logoUrl,
            "streamUrl" to channel.streamUrl,
            "category" to channel.category,
            "isLive" to channel.isLive,
            "currentProgram" to channel.currentProgram,
            "hasTrophy" to channel.hasTrophy
        )
        db.collection("channels").document(channel.id).set(data)
    }

    fun removeChannel(channelId: String) {
        if (!isFirebaseAvailable) return
        val db = FirebaseFirestore.getInstance()
        db.collection("channels").document(channelId).delete()
    }

    private fun setupUserProfileSync(userId: String) {
        if (!isFirebaseAvailable) return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) {
                    scope.launch {
                        val localProfile = repository?.userAccount?.first()
                        if (localProfile != null) {
                            saveUserProfile(localProfile)
                        } else {
                            val defaultProfile = UserAccount(
                                id = userId,
                                username = "مشترك Lfraja المتميز",
                                email = "user@lfraja.ma",
                                membershipType = "باقة *6 المغرب",
                                token = "LF-" + userId.take(6).uppercase()
                            )
                            repository?.insertUserAccount(defaultProfile)
                            saveUserProfile(defaultProfile)
                        }
                    }
                    return@addSnapshotListener
                }

                val data = snapshot.data
                if (data != null) {
                    val profile = UserAccount(
                        id = userId,
                        username = data["username"] as? String ?: "المشاهد العربي",
                        email = data["email"] as? String ?: "user@lfraja.ma",
                        membershipType = data["membershipType"] as? String ?: "العضوية المجانية",
                        avatarUrl = data["avatarUrl"] as? String ?: "",
                        token = data["token"] as? String ?: ""
                    )
                    _firebaseUser.value = profile
                    scope.launch {
                        repository?.insertUserAccount(profile)
                    }
                }
            }
    }

    fun saveUserProfile(profile: UserAccount) {
        if (!isFirebaseAvailable) return
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: profile.id

        val data = hashMapOf(
            "username" to profile.username,
            "email" to profile.email,
            "membershipType" to profile.membershipType,
            "avatarUrl" to profile.avatarUrl,
            "token" to profile.token
        )

        db.collection("users").document(userId).set(data)
    }
}
