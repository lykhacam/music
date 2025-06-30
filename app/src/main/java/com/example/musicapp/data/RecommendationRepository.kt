package com.example.myapplication.data

import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object RecommendationRepository {

    private val client = OkHttpClient()

    fun fetchRecommendations(
        onResult: (List<Song>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val url = if (uid != null) {
            "https://asia-southeast1-appmusicrealtime.cloudfunctions.net/getRecommendations?uid=$uid"
        } else {
            "https://asia-southeast1-appmusicrealtime.cloudfunctions.net/getRecommendations"
        }

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful || response.body == null) {
                    onResult(emptyList()); return
                }

                val result = mutableListOf<Song>()
                val jsonArray = JSONArray(response.body!!.string())
                for (i in 0 until jsonArray.length()) {
                    val info = jsonArray.getJSONObject(i).getJSONObject("info")
                    val song = Song(
                        id = jsonArray.getJSONObject(i).getString("songId"),
                        title = info.optString("title", ""),
                        artistNames = info.optJSONArray("artistNames")?.let { arr ->
                            List(arr.length()) { idx -> arr.getString(idx) }
                        } ?: emptyList(),
                        image = info.optString("image", ""),
                        url = info.optString("url", ""),
                        duration = info.optInt("duration", 0),
                        moodIds = info.optJSONArray("moodIds")?.let { arr ->
                            List(arr.length()) { idx -> arr.getString(idx) }
                        } ?: emptyList(),
                        categoryIds = info.optJSONArray("categoryIds")?.let { arr ->
                            List(arr.length()) { idx -> arr.getString(idx) }
                        } ?: emptyList(),
                        playlistIds = info.optJSONArray("playlistIds")?.let { arr ->
                            List(arr.length()) { idx -> arr.getString(idx) }
                        } ?: emptyList()
                    )
                    result.add(song)
                }

                onResult(result)
            }
        })
    }
}
