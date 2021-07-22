package com.example.textrecognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textrecognition.databinding.FragmentHistoryBinding


class HistoryFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>? = null
    private var translatedTextList = ArrayList<String>()
    private var sourceTextList = ArrayList<String>()
    private lateinit var binding: FragmentHistoryBinding

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
        translatedTextList = bundle?.getSerializable("translatedArray") as ArrayList<String>
        sourceTextList = bundle.getSerializable("sourceArray") as ArrayList<String>
        adapter = HistoryAdapter(translatedTextList, sourceTextList)
        binding.historyRecycleView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}