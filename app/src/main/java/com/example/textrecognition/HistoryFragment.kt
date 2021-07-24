package com.example.textrecognition

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textrecognition.databinding.FragmentHistoryBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


class HistoryFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>? = null
    private var translatedTextList = ArrayList<String>()
    private var sourceTextList = ArrayList<String>()
    private lateinit var binding: FragmentHistoryBinding
    private val TAG = "HistoryFragment"
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = arguments
        layoutManager = LinearLayoutManager(context)
        binding.historyRecycleView.layoutManager = layoutManager
        translatedTextList.clear()
        sourceTextList.clear()
        if(bundle != null) {
            translatedTextList = bundle?.getSerializable("translatedArray") as ArrayList<String>
            sourceTextList = bundle.getSerializable("sourceArray") as ArrayList<String>
        }
        if(bundle?.getBoolean("WithFingerprint") == false) {
            getHistoryFromDB()
        }
        else{
            adapter = HistoryAdapter(translatedTextList, sourceTextList)
            binding.historyRecycleView.adapter = adapter
        }

        binding.addFloatingActionButton.setOnClickListener {
            onAddButtonClicked()
        }
        binding.takePhotofloatingActionButton.setOnClickListener {
            getFragmentManager()?.beginTransaction()
                ?.replace(
                    R.id.container,
                    ImageAnalysisFragment.newInstance()
                )
                ?.commitNow()
        }
        binding.clearHistoryFloatingActionButton.setOnClickListener{
            sourceTextList.clear()
            translatedTextList.clear()
            if(bundle?.getBoolean("WithFingerprint") == false){
                writeHistoryToDB()
            }
            else {
                clearSharedPreferenceHistory()
            }
            binding.historyRecycleView.adapter?.notifyDataSetChanged()
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            sourceTextList.clear()
            translatedTextList.clear()
            getFragmentManager()?.beginTransaction()
                ?.replace(
                    R.id.container,
                    ImageAnalysisFragment.newInstance()
                )
                ?.commitNow()
        }
    }

    private fun clearSharedPreferenceHistory(){
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        preferences.edit().remove("source").remove("translated").apply()

    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.takePhotofloatingActionButton.visibility = View.VISIBLE
            binding.clearHistoryFloatingActionButton.visibility = View.VISIBLE
        } else {
            binding.takePhotofloatingActionButton.visibility = View.INVISIBLE
            binding.clearHistoryFloatingActionButton.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.clearHistoryFloatingActionButton.startAnimation(fromBottom)
            binding.takePhotofloatingActionButton.startAnimation(fromBottom)
            binding.addFloatingActionButton.startAnimation(rotateOpen)
        } else {
            binding.clearHistoryFloatingActionButton.startAnimation(toBottom)
            binding.takePhotofloatingActionButton.startAnimation(toBottom)
            binding.addFloatingActionButton.startAnimation(rotateClose)
        }
    }

    private fun getHistoryFromDB() {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseUser: FirebaseUser = mAuth.currentUser!!
        val uid: String = firebaseUser.uid
        val myRef: DatabaseReference = database.getReference("History").child(uid)
        var temp : ArrayList<String>? = null
        var temp2 : ArrayList<String>? = null
        myRef.get().addOnSuccessListener { dataSnapshot: DataSnapshot ->
            for (childDataSnapshot in dataSnapshot.children) {
                if(childDataSnapshot.key.equals("Translated")){
                    temp = childDataSnapshot.value as ArrayList<String>
                }
                if(childDataSnapshot.key.equals("Source")){
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

    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}