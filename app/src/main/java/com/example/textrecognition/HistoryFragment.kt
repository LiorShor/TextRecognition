package com.example.textrecognition

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textrecognition.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class HistoryFragment : Fragment() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<HistoryAdapter.ViewHolder>? = null
    private var translatedTextList = ArrayList<String>()
    private var sourceTextList = ArrayList<String>()
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var communicator: ICommunicator
    private var clicked = false
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.to_bottom_anim
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_history, container, false)
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        communicator = activity as ICommunicator
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = arguments
        layoutManager = LinearLayoutManager(context)
        binding.historyRecycleView.layoutManager = layoutManager
        clearLists()
        if (bundle != null) {
            translatedTextList = bundle.getSerializable("translatedArray") as ArrayList<String>
            sourceTextList = bundle.getSerializable("sourceArray") as ArrayList<String>
        }
        if (bundle?.getBoolean("WithFingerprint") == false) {
            getHistoryFromDB()
        } else {
            adapter = HistoryAdapter(translatedTextList, sourceTextList)
            binding.historyRecycleView.adapter = adapter
        }

        binding.addFloatingActionButton.setOnClickListener {
            onAddButtonClicked()
        }
        binding.takePhotoFloatingActionButton.setOnClickListener {
            communicator.changeFragmentWithoutData()
        }
        binding.clearHistoryFloatingActionButton.setOnClickListener {
            clearLists()
            if (bundle?.getBoolean("WithFingerprint") == false) {
                writeHistoryToDB()
            } else {
                clearSharedPreferenceHistory()
            }
            binding.historyRecycleView.adapter?.notifyDataSetChanged()
        }
        binding.logoutFloatingActionButton.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(activity).edit().remove("WithFingerprint").apply()
            val intent = Intent (requireActivity(), MainActivity()::class.java)
            startActivity(intent)
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            clearLists()
            communicator.changeFragmentWithoutData()
        }
    }

    private fun clearLists() {
        sourceTextList.clear()
        translatedTextList.clear()
    }

    private fun clearSharedPreferenceHistory() {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().remove("source").remove("translated").apply()
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.takePhotoFloatingActionButton.visibility = View.VISIBLE
            binding.clearHistoryFloatingActionButton.visibility = View.VISIBLE
            binding.logoutFloatingActionButton.visibility = View.VISIBLE
        } else {
            binding.takePhotoFloatingActionButton.visibility = View.INVISIBLE
            binding.clearHistoryFloatingActionButton.visibility = View.INVISIBLE
            binding.logoutFloatingActionButton.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.clearHistoryFloatingActionButton.startAnimation(fromBottom)
            binding.takePhotoFloatingActionButton.startAnimation(fromBottom)
            binding.logoutFloatingActionButton.startAnimation(fromBottom)
            binding.addFloatingActionButton.startAnimation(rotateOpen)
        } else {
            binding.clearHistoryFloatingActionButton.startAnimation(toBottom)
            binding.takePhotoFloatingActionButton.startAnimation(toBottom)
            binding.logoutFloatingActionButton.startAnimation(toBottom)
            binding.addFloatingActionButton.startAnimation(rotateClose)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getHistoryFromDB() {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseUser: FirebaseUser = mAuth.currentUser!!
        val uid: String = firebaseUser.uid
        val myRef: DatabaseReference = database.getReference("History").child(uid)
        var temp: ArrayList<String>? = null
        var temp2: ArrayList<String>? = null
        myRef.get().addOnSuccessListener { dataSnapshot: DataSnapshot ->
            for (childDataSnapshot in dataSnapshot.children) {
                if (childDataSnapshot.key.equals("Translated")) {
                    temp = childDataSnapshot.value as ArrayList<String>
                }
                if (childDataSnapshot.key.equals("Source")) {
                    temp2 = childDataSnapshot.value as ArrayList<String>
                }
            }
            temp?.let { translatedTextList.addAll(it) }
            temp2?.let { sourceTextList.addAll(it) }
            adapter = HistoryAdapter(translatedTextList, sourceTextList)
            binding.historyRecycleView.adapter = adapter
            writeHistoryToDB()
        }
    }

    private fun writeHistoryToDB() {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser = mAuth.currentUser!!
        val uid = firebaseUser.uid
        val database = FirebaseDatabase.getInstance()
        val databaseReference = database.getReference("History").child(uid)
        databaseReference.child("Translated").setValue(translatedTextList)
        databaseReference.child("Source").setValue(sourceTextList)
    }

    companion object

}