package com.example.todolist.presentation.fragments

import android.app.Dialog
import android.app.ProgressDialog.show
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.presentation.MainActivity
import com.example.todolist.presentation.MainActivityViewModel
import com.example.todolist.R
import com.example.todolist.presentation.adapter.CategoryAdapter
import com.example.todolist.presentation.adapter.TasksAdapter
import com.example.todolist.databinding.FragmentBaseBinding
import com.example.todolist.data.model.TaskCategoryInfo
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class BaseFragment : ParentFragment() {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var binding : FragmentBaseBinding
    @Inject
    @Named("base_fragment")
    lateinit var tasksAdapter : TasksAdapter
    @Inject lateinit var categoryAdapter : CategoryAdapter

    @Inject
    lateinit var pref:SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_base, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        tasksAdapter.setOnItemClickListener {
            editTaskInformation(it)
        }
        tasksAdapter.setOnTaskStatusChangedListener {
            updateTaskStatus(viewModel, it)
        }
        categoryAdapter.setOnItemClickListener {
            goToTaskCategoryFragment(it)
        }

        initCategoryRecycler()
        initTaskRecycler()

        binding.fab.setOnClickListener {
            val action = BaseFragmentDirections.actionBaseFragmentToNewTaskFragment(null)
            it.findNavController().navigate(action)
        }

        binding.statusChipGroup.setOnCheckedStateChangeListener { chipGroup, i ->
            val id = i[0]
            val chip = chipGroup.findViewById(id) as Chip
            when (chip.tag) {
                "0" ->  viewModel.getUncompletedTask().observe(viewLifecycleOwner) {
                    loadData(it)
                }
                "1" -> viewModel.getCompletedTask().observe(viewLifecycleOwner) {
                    loadData(it)
                }
                else ->  viewModel.getAllTasks().observe(viewLifecycleOwner){
                    loadData(it)
                }
            }
        }

        binding.searchEditText.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d("Search text ", s.toString())
                viewModel.searchTask("%"+s.toString()+"%").observe(viewLifecycleOwner){
                    loadData(it)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.ivSort.setOnClickListener {
            displaySortOptionDialog()
        }

        viewModel.getAllTasks().observe(viewLifecycleOwner){
            if(it.isEmpty())
            {
                pref.edit().putBoolean("networkData",false).apply()
            }
            loadData(it)
        }

        viewModel.getNoOfTaskForEachCategory().observe(viewLifecycleOwner) {
            if (it.isEmpty()) binding.categoryAnimationView.visibility = View.VISIBLE
            else binding.categoryAnimationView.visibility = View.GONE
            categoryAdapter.differ.submitList(it)
        }

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                val taskInfo = tasksAdapter.differ.currentList[position]?.taskInfo
                val categoryInfo = tasksAdapter.differ.currentList[position]?.categoryInfo?.get(0)
                if (taskInfo != null && categoryInfo!= null) {
                    deleteTask(viewModel, taskInfo, categoryInfo)
                    Snackbar.make(binding.root,"Deleted Successfully",Snackbar.LENGTH_LONG)
                        .apply {
                            setAction("Undo") {
                                viewModel.insertTaskAndCategory(taskInfo, categoryInfo)
                            }
                            show()
                        }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView)

    }

    private fun displaySortOptionDialog() {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.sort_dialog)
        val btnSort         = dialog.findViewById<MaterialButton>(R.id.btnSort)
        val radioGroup      = dialog.findViewById<RadioGroup>(R.id.radioGroup)

        btnSort.setOnClickListener {

            when (radioGroup.checkedRadioButtonId) {
                R.id.rbPriority ->  viewModel.getSortedTaskByPriority().observe(viewLifecycleOwner) {
                    Log.e("SXXD", "priority "+it?.size)
                    loadData(it)
                }
                R.id.rbDueDate -> viewModel.getSortedTaskByDate().observe(viewLifecycleOwner) {
                    Log.e("SXXD", "date "+it?.size)
                    loadData(it)
                }
                R.id.rbCompleteness -> viewModel.getSortedTaskByCompleted().observe(viewLifecycleOwner) {
                    Log.e("SXXD", "completed "+it?.size)
                    loadData(it)
                }
            }

            dialog.dismiss()
        }

        Objects.requireNonNull(dialog.window)
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private fun loadData(it: List<TaskCategoryInfo>) {
        if (it.isEmpty()) binding.noResultAnimationView.visibility = View.VISIBLE
        else binding.noResultAnimationView.visibility = View.GONE
        tasksAdapter.differ.submitList(it)
    }

    private fun editTaskInformation(taskCategoryInfo: TaskCategoryInfo) {
        val action = BaseFragmentDirections.actionBaseFragmentToNewTaskFragment(taskCategoryInfo)
        findNavController().navigate(action)
    }

    private fun goToTaskCategoryFragment(category: String) {
        val action = BaseFragmentDirections.actionBaseFragmentToTaskCategoryFragment(category)
        findNavController().navigate(action)
    }

    private fun initTaskRecycler() {
        binding.tasksRecyclerView.adapter = tasksAdapter
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initCategoryRecycler() {
        binding.categoriesRecyclerView.adapter = categoryAdapter
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
}
