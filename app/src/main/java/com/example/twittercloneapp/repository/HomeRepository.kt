package com.example.twittercloneapp.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.twittercloneapp.commons.Constants
import com.example.twittercloneapp.commons.TwitterCloneAppData
import com.example.twittercloneapp.model.Tweet
import com.example.twittercloneapp.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HomeRepository@Inject constructor(
    private val authentication: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationContext private val appContext: Context
) {
    var user: UserData? = null
    var tweets: MutableLiveData<List<Tweet>> = MutableLiveData()

    fun getTweets()  {
        db.collection(Constants.TWEETS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val tweetList = ArrayList<Tweet>()
                    for (document in result) {
                        Log.d("Homerepository", "${document.id} => ${document.data}")
                        var tweet = document.toObject(Tweet::class.java)
                        tweet.id = document.id
                        tweetList.add(tweet)
                    }
                    tweets.postValue(tweetList)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                            appContext.applicationContext,
                            "Erreur lors de la modification" + exception.message,
                            Toast.LENGTH_SHORT
                    ).show();
                    tweets.postValue(null)
                }

    }

    fun addTweet(tweetString: String) {

        TwitterCloneAppData.getUser()?.let {
            val tweet = HashMap<String, Any>()
            tweet["userData"] = it
            tweet["tweetText"] = tweetString
            tweet["timestamp"] = FieldValue.serverTimestamp()

            db.collection(Constants.TWEETS)
                .add(tweet)
                .addOnSuccessListener {
                    getTweets()
                    Log.d("Homerepository", "DocumentSnapshot added with ID: ${it.id}")
                }
                    .addOnFailureListener {
                        Toast.makeText(
                                appContext.applicationContext,
                                "Erreur lors de l'ajout" + it.message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
        }

    }

    fun updateTweet(tweetString: String, tweetId: String) {
            val tweet = HashMap<String, Any>()
            tweet["tweetText"] = tweetString


            db.collection(Constants.TWEETS)
                    .document(tweetId)
                    .update(tweet)
                    .addOnSuccessListener {
                        getTweets()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                                appContext.applicationContext,
                                "Erreur lors de la modification" + it.message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
    }

    fun deleteTweet(tweetId: String) {
        db.collection(Constants.TWEETS)
            .document(tweetId)
            .delete()
            .addOnSuccessListener {
                getTweets()
            }
            .addOnFailureListener {
                Toast.makeText(
                    appContext.applicationContext,
                        "Erreur lors de la modification" + it.message,
                    Toast.LENGTH_SHORT
                ).show();
            }
    }

    fun addSnapShotListener() {
        db.collection(Constants.TWEETS).addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("Homerepository", "listen:error", e)
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                val tweet = dc.document.toObject(Tweet::class.java)
                if (tweet.userData?.id != TwitterCloneAppData.getUser()?.id) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> getTweets()
                        DocumentChange.Type.MODIFIED -> getTweets()
                        DocumentChange.Type.REMOVED -> getTweets()
                    }
                }
                }
            }
        }

}