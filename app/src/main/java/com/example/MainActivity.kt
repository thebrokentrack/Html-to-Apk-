package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ProjectRepository
import com.example.ui.ConverterDashboardScreen
import com.example.ui.ProjectViewModel
import com.example.ui.ProjectViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room Database, Dao, and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ProjectRepository(database.projectDao())
        
        // Build the ViewModel Factory
        val factory = ProjectViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[ProjectViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConverterDashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
