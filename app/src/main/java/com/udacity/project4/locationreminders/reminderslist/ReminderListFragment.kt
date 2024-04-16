package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_reminders, container, false
        )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))
        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        // Load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        // Use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder())
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {}
        // Setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle selection based on the menu item ID
        when (item.itemId) {
            // When the logout option is selected
            R.id.logout -> {
                // Sign out the user using the AuthUI instance
                AuthUI.getInstance().signOut(requireContext())
                    // Add a success listener to handle successful sign-out
                    .addOnSuccessListener {
                        // Once the user is signed out, start the AuthenticationActivity
                        startAuthenticationActivity()
                    }
            }
        }
        // Call the superclass implementation to handle other menu items
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    private fun startAuthenticationActivity() {
        // Create an Intent to launch the AuthenticationActivity
        val intent = Intent(activity, AuthenticationActivity::class.java)

        // Add a flag to clear the current task, removing all activities on the task stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // Add a flag to start a new task for the AuthenticationActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Start the AuthenticationActivity using the constructed Intent
        startActivity(intent)
    }

}