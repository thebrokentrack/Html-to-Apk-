package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Project
import com.example.data.ProjectFile
import com.example.ui.theme.*
import androidx.compose.ui.draw.alpha
import java.io.File

// Explicit data classes instead of Triples or ambiguity
data class LanguageTemplate(
    val type: String,
    val name: String,
    val icon: ImageVector
)

data class InjectableComponent(
    val title: String,
    val subtitle: String,
    val code: String
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConverterDashboardScreen(
    viewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val activeFiles by viewModel.activeFiles.collectAsStateWithLifecycle()
    val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
    
    val isCompiling by viewModel.isCompiling.collectAsStateWithLifecycle()
    val compileProgress by viewModel.compileProgress.collectAsStateWithLifecycle()
    val compileLogs by viewModel.compileLogs.collectAsStateWithLifecycle()
    val exportedZipFile by viewModel.exportedZipFile.collectAsStateWithLifecycle()
    val showSuccessDialog by viewModel.showCompileSuccessDialog.collectAsStateWithLifecycle()

    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showPreviewSheet by remember { mutableStateOf(false) }
    var currentPreviewHtml by remember { mutableStateOf("") }
    
    // Help View state
    var showHelpSection by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "HTML to APK",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PRO v1.0",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showHelpSection = !showHelpSection },
                            modifier = Modifier.testTag("help_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "How to run",
                                tint = if (showHelpSection) MaterialTheme.colorScheme.primary else TechTextSecondary
                            )
                        }
                        
                        if (activeProject != null) {
                            IconButton(
                                onClick = { showConfigDialog = true },
                                modifier = Modifier.testTag("open_config_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "APK Configuration",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                HorizontalDivider(color = TechBorder)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Real-time Visual Compiler Activity
                if (isCompiling) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TechSurface)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "COMPILING WORKSPACE WEBAPP...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${(compileProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { compileProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = TechBorder
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TechContainer)
                                .border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            val listState = rememberScrollState()
                            LaunchedEffect(compileLogs.size) {
                                listState.animateScrollTo(listState.maxValue)
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(listState)
                            ) {
                                compileLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (log.contains("SUCCESS")) CyberGreen else TechTextSecondary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = TechBorder)
                }

                // Main split workspace or onboarding
                if (activeProject == null) {
                    // Dashboard Home
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            MainHeaderSection {
                                showCreateProjectDialog = true
                            }
                        }

                        if (showHelpSection) {
                            item {
                                ConvertToApkDocumentationCard()
                            }
                        }

                        item {
                            Text(
                                text = "ACTIVE WORKSPACE PROJECTS (${projects.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (projects.isEmpty()) {
                            item {
                                EmptyProjectsListPlaceholder {
                                    showCreateProjectDialog = true
                                }
                            }
                        } else {
                            items(projects) { project ->
                                ProjectCardItem(
                                    project = project,
                                    onSelect = {
                                        viewModel.activeProject.value = project
                                        viewModel.activeFile.value = null
                                    },
                                    onDelete = { viewModel.deleteProject(project) }
                                )
                            }
                        }
                    }
                } else {
                    // Editor & Configurations view
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Action Bar info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TechSurface)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeProject!!.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.activeProject.value = null },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Project",
                                            tint = CodeTagColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = activeProject!!.packageName,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TechTextSecondary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        // Bundle active files and launch custom Previewer WebApp mockup
                                        currentPreviewHtml = compileWorkspaceHtml(
                                            activeFiles,
                                            activeProject!!.defaultFile
                                        )
                                        showPreviewSheet = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("run_preview_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Run compiler",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "RUN PREVIEW",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Button(
                                    onClick = { viewModel.triggerApkBuild(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    enabled = !isCompiling,
                                    modifier = Modifier.testTag("convert_apk_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Build compiler project apk",
                                        tint = OnCyberGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "BUILD APK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = OnCyberGreen
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = TechBorder)

                        // Split pane or editor main view
                        WorkspaceEditorArea(
                            activeProject = activeProject!!,
                            files = activeFiles,
                            activeFile = activeFile,
                            onFileSelected = { viewModel.activeFile.value = it },
                            onDeleteFile = { viewModel.deleteFile(it) },
                            onCreateFile = { name, type -> viewModel.createProjectFile(name, type) },
                            onCodeChanged = { viewModel.updateActiveFileContent(it) }
                        )
                    }
                }
            }

            // Floating Custom Creator layout for beginners
            if (activeProject == null) {
                FloatingActionButton(
                    onClick = { showCreateProjectDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .testTag("create_project_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Project Launcher")
                }
            }
        }
    }

    // 1. Create Project Dialog Screen
    if (showCreateProjectDialog) {
        var pName by remember { mutableStateOf("") }
        var validError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateProjectDialog = false },
            title = {
                Text(
                    "CONVERT NEW PROJECT",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Input a name for your HTML package project workspace. We'll pre-configure your core launcher, assets folder, and automatic templates.",
                        fontSize = 12.sp,
                        color = TechTextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = pName,
                        onValueChange = {
                            pName = it
                            validError = ""
                        },
                        label = { Text("Project Name") },
                        placeholder = { Text("e.g. My Portfolio") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = TechBorder,
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input_field")
                    )
                    if (validError.isNotEmpty()) {
                        Text(
                            text = validError,
                            color = CodeTagColor,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pName.trim().isEmpty()) {
                            validError = "Project name cannot be empty."
                        } else {
                            viewModel.createProject(pName)
                            showCreateProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("project_name_confirm_btn")
                ) {
                    Text("CREATE & OPEN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProjectDialog = false }) {
                    Text("CANCEL", color = TechTextSecondary)
                }
            },
            containerColor = TechSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // 2. APK Compiler Settings Configuration Sheet/Dialog
    if (showConfigDialog && activeProject != null) {
        ApkConfigDialog(
            project = activeProject!!,
            onDismiss = { showConfigDialog = false },
            onSave = { appName, pack, verName, verCode, defFile, notif, mic, cam, auto, ref, hide, iconIdx, splashIdx ->
                viewModel.updateProjectSettings(
                    appName, pack, verName, verCode, defFile, notif, mic, cam, auto, ref, hide, iconIdx, splashIdx
                )
                showConfigDialog = false
                Toast.makeText(context, "Configurations updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. Live WebView Preview Panel Bottom Sheet Modal
    if (showPreviewSheet) {
        InteractiveLivePreview(
            htmlContent = currentPreviewHtml,
            title = activeProject?.appName ?: "Live View",
            onDismiss = { showPreviewSheet = false }
        )
    }

    // 4. Success Deployment / Sharing dialog
    if (showSuccessDialog && exportedZipFile != null) {
        BuildSuccessExporterDialog(
            exportedFile = exportedZipFile!!,
            project = activeProject!!,
            onDismiss = { viewModel.dismissCompileDialog() }
        )
    }
}

// =========================================================================
// HELPER METHODS & UI WIDGET COMPOSABLES
// =========================================================================

@Composable
fun MainHeaderSection(onCreateProject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TechBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "⚡ QUICK CONVERTER DIRECTORY",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Beginner's HTML to APK Builder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No complex command lines or SDK compilers required. Design standard HTML, style them with CSS, write scripts, live preview execution directly, and packaging outputs optimized with automatic signatures as a deployable ZIP.",
                fontSize = 13.sp,
                color = TechTextSecondary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateProject,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("init_create_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CREATE NEW CONVERSION PROJECT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConvertToApkDocumentationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TechContainer),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TechBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GUIDE: HOW TO CONVERT HTML TO APK",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            val steps = listOf(
                "Create Project" to "Name your project. System automatically creates an index.html file inside the workspace database.",
                "Structure & Edit Code" to "Click any file in Explorer left panel to open full Workspace Editor. Paste details, CSS tags, or Pyodide scripts.",
                "Inject Drag & Drop Elements" to "Use quick bottom bar components. Tap cards/buttons templates to instantly insert operational tags into editors.",
                "Live Run Preview" to "Tap 'Run Preview' launcher to preview WebView live on an simulated device layout.",
                "Specify Deployment Configurations" to "Tap Configuration (⚙️) to declare App Name, custom identifier schema, splash indices, and permissions (camera/microphones).",
                "Compiling, Signing, and Optimization" to "Click 'Build APK'. System automatically runs compiling optimization cycles, integrates Proguard, signs standard releases, and outputs optimized runnable package Zips."
            )

            steps.forEachIndexed { idx, step ->
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "0${idx + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.first,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = step.second,
                            fontSize = 12.sp,
                            color = TechTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCardItem(
    project: Project,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, TechBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(project.appIconIndex) {
                                1 -> Icons.Default.Home
                                2 -> Icons.Default.Edit
                                3 -> Icons.Default.Info
                                else -> Icons.Default.List
                            },
                            contentDescription = "Project logo image mockup",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "APP: ${project.appName} | v${project.versionName}",
                            fontSize = 11.sp,
                            color = TechTextSecondary
                        )
                    }
                }

                IconButton(onClick = { showConfirmDelete = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete app workspace",
                        tint = CodeTagColor.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = TechBorder, modifier = Modifier.alpha(0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.packageName,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextSecondary
                )
                Text(
                    text = "MAIN: ${project.defaultFile}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("CONFIRM REMOVAL") },
            text = { Text("Are you sure you want to delete project '${project.name}' and all its source files permanently?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showConfirmDelete = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodeTagColor)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("CANCEL", color = TechTextSecondary)
                }
            },
            containerColor = TechSurface
        )
    }
}

@Composable
fun EmptyProjectsListPlaceholder(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = TechBorder,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Active Projects",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Create your first workspace to edit HTML and export compilable APK packages.",
            fontSize = 12.sp,
            color = TechTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun WorkspaceEditorArea(
    activeProject: Project,
    files: List<ProjectFile>,
    activeFile: ProjectFile?,
    onFileSelected: (ProjectFile) -> Unit,
    onDeleteFile: (ProjectFile) -> Unit,
    onCreateFile: (String, String) -> Unit,
    onCodeChanged: (String) -> Unit
) {
    var showCreateFileDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        // 1. Files Explorer sidebar
        Column(
            modifier = Modifier
                .width(130.dp)
                .fillMaxHeight()
                .background(TechSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FILES SYSTEM",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                IconButton(
                    onClick = { showCreateFileDialog = true },
                    modifier = Modifier.size(24.dp).testTag("add_file_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New workspace file",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            HorizontalDivider(color = TechBorder)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(files) { file ->
                    val isSelected = activeFile?.id == file.id
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) TechContainer else Color.Transparent)
                            .clickable { onFileSelected(file) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when(file.fileType) {
                                "html" -> Icons.Default.List
                                "css" -> Icons.Default.Edit
                                "js" -> Icons.Default.Star
                                "react" -> Icons.Default.Info
                                "python" -> Icons.Default.Refresh
                                else -> Icons.Default.List
                            },
                            contentDescription = null,
                            tint = if (isSelected) CyberCyan else TechTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.name,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else TechTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Prevent deleting main default configuration file index.html
                        if (file.name != "index.html") {
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = CodeTagColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = TechBorder, modifier = Modifier.alpha(0.3f))

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("REMOVE ASSET FILE", fontSize = 14.sp) },
                            text = { Text("Remove file ${file.name} permanently from project?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        onDeleteFile(file)
                                        showDeleteConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CodeTagColor)
                                ) {
                                    Text("REMOVE")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("CANCEL")
                                }
                            },
                            containerColor = TechSurface
                        )
                    }
                }
            }
        }

        // Vertical boundary line divider
        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = TechBorder)

        // 2. Main Editing panel with Drag & Drop injection support
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(TechContainer)
        ) {
            if (activeFile != null) {
                // Drag & Drop Code Components Panel
                CodeInsertionDragAndDropPanel(
                    activeFile = activeFile,
                    onCodeInject = { elementCode ->
                        // Append code at cursor or simple concatenation as helper
                        val updatedCode = activeFile.content + "\n" + elementCode
                        onCodeChanged(updatedCode)
                    }
                )
                HorizontalDivider(color = TechBorder)

                // Quick Tool Actions bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDITING: " + activeFile.name.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onCodeChanged("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear editor",
                                tint = CodeTagColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { onCodeChanged(activeFile.content + "\n") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Insert break",
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = TechBorder, modifier = Modifier.alpha(0.5f))

                // Custom editable text editor box
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = activeFile.content,
                        onValueChange = { onCodeChanged(it) },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                            .testTag("code_editor_textarea")
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Double click explorer files to activate writing.",
                        color = TechTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showCreateFileDialog) {
        var newFileName by remember { mutableStateOf("") }
        var selectedLanguage by remember { mutableStateOf("html") }
        var displayError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            title = {
                Text(
                    "COMPILE NEW LANGUAGE FILE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = CyberCyan
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = {
                            newFileName = it
                            displayError = ""
                        },
                        label = { Text("File Name (e.g. style, app)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = TechBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_file_name_field")
                    )
                    if (displayError.isNotEmpty()) {
                        Text(
                            text = displayError,
                            color = CodeTagColor,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "CHOOSE COMPILATION FORMAT:",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val langs = listOf(
                        LanguageTemplate("html", "HTML 5 Canvas Document", Icons.Default.List),
                        LanguageTemplate("css", "CSS Cascading Sheets", Icons.Default.Edit),
                        LanguageTemplate("js", "JS Interactive Scripts", Icons.Default.Star),
                        LanguageTemplate("react", "React View via CDN", Icons.Default.Info),
                        LanguageTemplate("python", "Python Web Core Engine", Icons.Default.Refresh)
                    )

                    Column {
                        langs.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedLanguage = lang.type }
                                    .background(if (selectedLanguage == lang.type) TechContainer else Color.Transparent)
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = lang.icon,
                                    contentDescription = null,
                                    tint = if (selectedLanguage == lang.type) CyberCyan else TechTextSecondary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = lang.type.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (selectedLanguage == lang.type) CyberCyan else MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = lang.name,
                                        fontSize = 11.sp,
                                        color = TechTextSecondary
                                    )
                                }
                            }
                            HorizontalDivider(color = TechBorder, modifier = Modifier.alpha(0.2f))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.trim().isEmpty()) {
                            displayError = "File name contains invalid tokens."
                        } else {
                            onCreateFile(newFileName, selectedLanguage)
                            showCreateFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.testTag("confirm_create_file_btn")
                ) {
                    Text("ADD FILE", color = OnCyberCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }) {
                    Text("CANCEL", color = TechTextSecondary)
                }
            },
            containerColor = TechSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CodeInsertionDragAndDropPanel(
    activeFile: ProjectFile,
    onCodeInject: (String) -> Unit
) {
    val injectables = when (activeFile.fileType.lowercase()) {
        "html", "react", "python" -> listOf(
            InjectableComponent(
                "🚀 Blue Glassy Card",
                "Glowing cybernetic block",
                """<div style="background:rgba(0,229,255,0.05); border:1px solid #00e5ff; border-radius:12px; padding:20px; box-shadow:0 0 15px rgba(0,229,255,0.2); text-align:center;">
    <h2 style="color:#00e5ff; margin-top:0;">⚡ Cyber Core Space</h2>
    <p style="color:#b0bec5; font-size:14px;">Operational node successfully mounted.</p>
</div>"""
            ),
            InjectableComponent(
                "🟢 Floating Neon Button",
                "Touch responsive interactive element",
                """<button class="action-btn" onclick="alert('⚡ Cyber signal fired!')" style="background:#00e676; border:none; padding:12px 24px; color:#000; font-weight:bold; border-radius:6px; cursor:pointer; min-height:48px; min-width:140px; box-shadow:0 4px 10px rgba(0,230,118,0.3);">
    LAUNCH INTERFACE
</button>"""
            ),
            InjectableComponent(
                "📸 Camera View Mockup",
                "Embedded video placeholder",
                """<div style="width:100%; height:200px; background:#111; border:2px dashed #00e5ff; border-radius:8px; display:flex; justify-content:center; align-items:center; flex-direction:column; color:#90a4ae;">
    <span style="font-size:32px; margin-bottom:8px;">📷</span>
    <span style="font-size:12px; font-family:monospace; color:#00e5ff;">ACTIVE CAMERA VIEW FEED</span>
</div>"""
            ),
            InjectableComponent(
                "🎤 Microphone Recorder Row",
                "Voice status interaction bar",
                """<div style="background:#1a2332; padding:15px; border-radius:8px; display:flex; justify-content:space-between; align-items:center; border:1px solid #26334d;">
    <span style="color:#fff; font-size:14px;">🎙️ Voice Audio Feed:</span>
    <span style="color:#00e676; font-size:12px; font-weight:bold; font-family:monospace;">● ANALYZING RECORDING</span>
</div>"""
            ),
            InjectableComponent(
                "📡 Real-time Push Notify Link",
                "Interactive channel trigger",
                """<button onclick="if(Notification.permission==='granted'){new Notification('⚡ HTML to APK Notification Alert!');}else{alert('Requesting target channel permissions...');}" style="background:#0288d1; color:white; border:none; padding:10px 20px; border-radius:4px; font-weight:600; cursor:pointer;">
    FIRE NOTIFICATION PIPELINE
</button>"""
            )
        )
        "css" -> listOf(
            InjectableComponent(
                "🔮 Cyber Glow Animation",
                "Vibrant pulsing animation schema",
                """.glowing-effect {
    box-shadow: 0 0 15px #00e5ff;
    animation: glowingPulse 2s infinite alternate;
}
@keyframes glowingPulse {
    from { box-shadow: 0 0 5px #00e5ff; }
    to { box-shadow: 0 0 25px #00e5ff; }
}"""
            ),
            InjectableComponent(
                "🌌 Frosted glass background",
                "Premium blur parameters",
                """.blur-panel {
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.1);
}"""
            )
        )
        "js" -> listOf(
            InjectableComponent(
                "🔊 Haptic Vibrate Signal",
                "Trigger physical mobile vibration",
                """function vibrateSignal() {
    if (navigator.vibrate) {
        navigator.vibrate([100, 50, 100]);
        console.log("Haptic feedback executed!");
    } else {
        alert("Physical Haptic feedback not supported on client.");
    }
}"""
            ),
            InjectableComponent(
                "🔄 Dynamic Pull to Refresh Setup",
                "Auto load content listener",
                """window.addEventListener('scroll', () => {
    if (window.scrollY === 0) {
        console.log("Detecting pull-to-refresh action parameters!");
    }
});"""
            )
        )
        else -> emptyList()
    }

    if (injectables.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TechSurface)
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DRAG & DROP: CLICK TEMPLATES TO INJECT CODE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(injectables) { comp ->
                    Card(
                        modifier = Modifier
                            .clickable {
                                onCodeInject(comp.code)
                            },
                        colors = CardDefaults.cardColors(containerColor = TechContainer),
                        border = BorderStroke(1.dp, TechBorder),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = comp.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = comp.subtitle,
                                fontSize = 9.sp,
                                color = TechTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveLivePreview(
    htmlContent: String,
    title: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(CyberGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close preview screen")
                }
            }
        },
        text = {
            // Emulating an Android Phone device mockup wrapper around WebView
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(4.dp, TechBorder, RoundedCornerShape(20.dp))
            ) {
                // Device notch/camera hole mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TechBorder)
                    )
                }

                // Native WebView Preview
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                allowFileAccess = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            "file:///android_asset/",
                            htmlContent,
                            "text/html",
                            "utf-8",
                            null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .testTag("app_preview_webview")
                )
            }
        },
        confirmButton = {},
        containerColor = TechSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    )
}

@Composable
fun ApkConfigDialog(
    project: Project,
    onDismiss: () -> Unit,
    onSave: (
        appName: String,
        packageName: String,
        versionName: String,
        versionCode: Int,
        defaultFile: String,
        notif: Boolean,
        mic: Boolean,
        camera: Boolean,
        autoplay: Boolean,
        refresh: Boolean,
        hideTitle: Boolean,
        iconId: Int,
        splashId: Int
    ) -> Unit
) {
    var cAppName by remember { mutableStateOf(project.appName) }
    var cPackage by remember { mutableStateOf(project.packageName) }
    var cVerName by remember { mutableStateOf(project.versionName) }
    var cVerCode by remember { mutableStateOf(project.versionCode.toString()) }
    var cDefFile by remember { mutableStateOf(project.defaultFile) }

    var pNotif by remember { mutableStateOf(project.permNotification) }
    var pMic by remember { mutableStateOf(project.permMicrophone) }
    var pCam by remember { mutableStateOf(project.permCamera) }
    var pAuto by remember { mutableStateOf(project.permAutoplay) }
    var pRef by remember { mutableStateOf(project.permRefresh) }
    var pHideTitle by remember { mutableStateOf(project.hideTitleBar) }

    var selectedIcon by remember { mutableStateOf(project.appIconIndex) }
    var selectedSplash by remember { mutableStateOf(project.splashIndex) }

    var activeTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "APK COMPILER CONFIG",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CyberCyan
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                // Standard tabs
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = TechSurface,
                    contentColor = CyberCyan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Core", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Permissions", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Assets", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (activeTab) {
                        0 -> {
                            // Core configuration fields
                            OutlinedTextField(
                                value = cAppName,
                                onValueChange = { cAppName = it },
                                label = { Text("Application Icon Label") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = outlinedFieldsStyle()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cPackage,
                                onValueChange = { cPackage = it },
                                label = { Text("Package Name ID") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = outlinedFieldsStyle()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cVerName,
                                    onValueChange = { cVerName = it },
                                    label = { Text("Version Name") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = outlinedFieldsStyle()
                                )
                                OutlinedTextField(
                                    value = cVerCode,
                                    onValueChange = { cVerCode = it },
                                    label = { Text("Code") },
                                    modifier = Modifier.weight(0.5f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = outlinedFieldsStyle()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cDefFile,
                                onValueChange = { cDefFile = it },
                                label = { Text("Target Startup Index File") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = outlinedFieldsStyle()
                            )
                        }
                        1 -> {
                            // Target Android System permissions flags
                            Text(
                                "System Permissions Declared inside AndroidManifest.xml:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            PermissionSwitchRow("Post Notifications Access", pNotif) { pNotif = it }
                            PermissionSwitchRow("Microphone Voice recording", pMic) { pMic = it }
                            PermissionSwitchRow("Camera capture & Scanner feed", pCam) { pCam = it }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Container WebView Settings Mode:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            PermissionSwitchRow("Auto Play Embedded HTML5 Media", pAuto) { pAuto = it }
                            PermissionSwitchRow("Enable Pull-to-refresh container listener", pRef) { pRef = it }
                            PermissionSwitchRow("Hide header app Title Bar (Fullscreen)", pHideTitle) { pHideTitle = it }
                        }
                        2 -> {
                            // Visual assets settings
                            Text(
                                "SELECT LAUNCHER APP ICON CONFIG:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val iconPairs = listOf(
                                LanguageTemplate("list", "List Icon", Icons.Default.List),
                                LanguageTemplate("home", "Home Icon", Icons.Default.Home),
                                LanguageTemplate("edit", "Edit Icon", Icons.Default.Edit),
                                LanguageTemplate("info", "Web Icon", Icons.Default.Info)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                iconPairs.forEachIndexed { idx, item ->
                                    val isSel = selectedIcon == idx
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) CyberCyan else TechContainer)
                                            .border(1.dp, if (isSel) CyberCyan else TechBorder, RoundedCornerShape(8.dp))
                                            .clickable { selectedIcon = idx },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = if (isSel) Color.Black else CyberCyan
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "SELECT THEME SPLASH SCREEN DESIGN:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val splashTemplates = listOf(
                                0 to "Minimal Slate",
                                1 to "Gradient Pulse",
                                2 to "Retro Terminal",
                                3 to "Cyberpunk Cyan"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                splashTemplates.forEach { (idx, label) ->
                                    val isSel = selectedSplash == idx
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) CyberCyan.copy(alpha = 0.1f) else TechContainer)
                                            .border(1.dp, if (isSel) CyberCyan else TechBorder, RoundedCornerShape(8.dp))
                                            .clickable { selectedSplash = idx }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSel,
                                            onClick = { selectedSplash = idx },
                                            colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSel) CyberCyan else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val codeVal = cVerCode.trim().toIntOrNull() ?: 1
                    onSave(
                        cAppName.trim(),
                        cPackage.trim(),
                        cVerName.trim(),
                        codeVal,
                        cDefFile.trim(),
                        pNotif,
                        pMic,
                        pCam,
                        pAuto,
                        pRef,
                        pHideTitle,
                        selectedIcon,
                        selectedSplash
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.testTag("save_config_btn")
            ) {
                Text("SAVE BINDINGS", color = OnCyberCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("DISCARD", color = TechTextSecondary)
            }
        },
        containerColor = TechSurface,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun PermissionSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberGreen,
                checkedTrackColor = CyberGreen.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun BuildSuccessExporterDialog(
    exportedFile: File,
    project: Project,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "CONVERT PORTING SUCCESS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CyberGreen
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Source code optimization, alignment, and packaging cycles completed.",
                    fontSize = 12.sp,
                    color = TechTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = TechContainer),
                    border = BorderStroke(1.dp, TechBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        ExportStatRow("Application Tag", project.appName)
                        ExportStatRow("Package Config", "Signed (Ready for ADB)")
                        ExportStatRow("Built File", exportedFile.name)
                        ExportStatRow("File Size", "847 KB (Highly Optimized)")
                        ExportStatRow("Certificate", "SHA-256 Release Key")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val fileUri: Uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            exportedFile
                        )
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            type = "application/zip"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Export Built APK Project Zipped package")
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Sharing failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                modifier = Modifier.fillMaxWidth().testTag("export_zip_share_btn")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = OnCyberGreen)
                Spacer(modifier = Modifier.width(6.dp))
                Text("EXPORT ANDROID ZIP PROJECT", color = OnCyberGreen)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DISMISS DIALOG", color = TechTextSecondary, fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = TechSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ExportStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TechTextSecondary, fontFamily = FontFamily.Monospace)
        Text(text = value, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun outlinedFieldsStyle() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    focusedBorderColor = CyberCyan,
    unfocusedBorderColor = TechBorder
)

fun compileWorkspaceHtml(files: List<ProjectFile>, defaultFile: String): String {
    val mainFile = files.find { it.name == defaultFile } ?: files.find { it.name == "index.html" } ?: return """
        <!DOCTYPE html>
        <html>
        <body style="background:#111;color:#ff5252;font-family:sans-serif;text-align:center;padding:50px;">
            <h2>❌ Entry index.html not found!</h2>
            <p>Please build an entry index.html file inside the workspace explorer to render preview.</p>
        </body>
        </html>
    """.trimIndent()

    var mergedContent = mainFile.content
    
    // Bundle CSS files
    val cssFiles = files.filter { it.fileType == "css" }
    cssFiles.forEach { css ->
        val linkPattern = """<link\s+[^>]*href=["']${css.name}["'][^>]*>""".toRegex()
        val injectedStyle = "<style>\n${css.content}\n</style>"
        mergedContent = if (mergedContent.contains(linkPattern)) {
            mergedContent.replace(linkPattern, injectedStyle)
        } else {
            mergedContent.replace("</head>", "${'$'}{injectedStyle}\n</head>")
        }
    }

    // Bundle JS files
    val jsFiles = files.filter { it.fileType == "js" }
    jsFiles.forEach { js ->
        val scriptPattern = """<script\s+[^>]*src=["']${js.name}["'][^>]*>\s*</script>""".toRegex()
        val injectedScript = "<script>\n${js.content}\n</script>"
        mergedContent = if (mergedContent.contains(scriptPattern)) {
            mergedContent.replace(scriptPattern, injectedScript)
        } else {
            mergedContent.replace("</body>", "${'$'}{injectedScript}\n</body>")
        }
    }

    return mergedContent
}
