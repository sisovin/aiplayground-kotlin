package com.playapp.aiagents.data.service

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.playapp.aiagents.data.model.Agent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseService {
    private val TAG = "FirebaseService"
    private val database = FirebaseDatabase.getInstance()
    private val agentsRef = database.getReference("agents")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAgents(): Flow<List<Agent>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val agents = snapshot.children.mapNotNull { child ->
                    try {
                        child.getValue(Agent::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to map agent from snapshot: ${e.message}", e)
                        null
                    }
                }
                trySend(agents)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        agentsRef.addValueEventListener(listener)
        awaitClose { agentsRef.removeEventListener(listener) }
    }
}
