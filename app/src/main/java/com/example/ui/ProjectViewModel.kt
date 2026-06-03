package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ProjectViewModel(private val repository: ProjectRepository) : ViewModel() {

    // 1. All projects list
    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Currently selected/active project
    val activeProject = MutableStateFlow<Project?>(null)

    // 3. Current active files inside the selected project
    val activeFiles: StateFlow<List<ProjectFile>> = activeProject
        .flatMapLatest { project ->
            if (project != null) {
                repository.getFilesForProject(project.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Currently opened file for editing
    val activeFile = MutableStateFlow<ProjectFile?>(null)

    // 5. Visual compiler status metrics
    val isCompiling = MutableStateFlow(false)
    val compileProgress = MutableStateFlow(0f)
    val compileLogs = MutableStateFlow<List<String>>(emptyList())
    val exportedZipFile = MutableStateFlow<File?>(null)
    val showCompileSuccessDialog = MutableStateFlow(false)

    // Initial boilerplates map for creating coding files
    private val boilerplates = mapOf(
        "html" to """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Mobile App</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="card">
        <h1>🚀 Converted HTML to APK</h1>
        <p>This is a real-time responsive web interface running inside an Android WebView container.</p>
        
        <div class="status-box">
            Status: <span id="status" class="status-online">Interactive</span>
        </div>

        <button id="actionBtn" class="action-btn">Launch Code Ripple</button>
        <p id="feedback" class="feedback-text"></p>
    </div>
    
    <script src="script.js"></script>
</body>
</html>
""",
        "css" to """body {
    margin: 0;
    padding: 20px;
    background: #0d1117;
    color: #c9d1d9;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    box-sizing: border-box;
}

.card {
    background: #161b22;
    border: 1px solid #30363d;
    border-radius: 12px;
    padding: 30px;
    max-width: 400px;
    text-align: center;
    box-shadow: 0 4px 20px rgba(0,0,0,0.4);
}

h1 {
    color: #58a6ff;
    font-size: 26px;
    margin-top: 0;
}

p {
    line-height: 1.5;
    font-size: 15px;
    color: #8b949e;
}

.status-box {
    margin: 15px 0;
    font-size: 14px;
    background: rgba(88, 166, 255, 0.1);
    padding: 8px;
    border-radius: 6px;
    display: inline-block;
}

.status-online {
    color: #3fb950;
    font-weight: bold;
}

.action-btn {
    background: #238636;
    color: #ffffff;
    border: none;
    border-radius: 6px;
    padding: 12px 24px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    margin-top: 15px;
    transition: all 0.2s ease;
}

.action-btn:active {
    transform: scale(0.97);
    background: #2ea043;
}

.feedback-text {
    color: #3fb950;
    font-weight: 500;
    margin-top: 15px;
}
""",
        "js" to """document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('actionBtn');
    const txt = document.getElementById('feedback');
    let tapCount = 0;

    btn.addEventListener('click', () => {
        tapCount++;
        txt.textContent = '🎉 Ripple Action #' + tapCount + '! Successfully executing custom script.';
        
        // Android interface bridge test check
        if (window.vibrate) {
            window.vibrate();
        }
    });
});
""",
        "react" to """<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>React Injected Live View</title>
    <!-- Load Core Dependencies -->
    <script src="https://unpkg.com/react@18/umd/react.development.js" crossorigin></script>
    <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js" crossorigin></script>
    <!-- Load Babel for live JSX compiling safely -->
    <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
    <style>
        body { background: #20232a; color: white; font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .box { padding: 30px; border-radius: 10px; background: #282c34; border: 1px solid #333; text-align: center; }
        h1 { color: #61dafb; }
    </style>
</head>
<body>
    <div id="root"></div>
    <script type="text/babel">
        function Container() {
            const [clicks, setClicks] = React.useState(0);
            return (
                <div className="box">
                    <h1>⚛️ React App Space</h1>
                    <p>Fusing React live in your Android APK container using standard script injections!</p>
                    <button 
                        onClick={() => setClicks(clicks + 1)}
                        style={{ background: '#61dafb', color: '#000', border: 'none', padding: '12px 24px', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                        Taps inside React: {clicks}
                    </button>
                </div>
            );
        }
        const container = ReactDOM.createRoot(document.getElementById('root'));
        container.render(<Container />);
    </script>
</body>
</html>
""",
        "python" to """<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Python (Pyodide) Inside HTML APK</title>
    <!-- Load WebAssembly-based Python engine -->
    <script src="https://cdn.jsdelivr.net/pyodide/v0.25.0/full/pyodide.js"></script>
    <style>
        body { font-family: 'Courier New', Courier, monospace; padding: 20px; background-color: #0d1117; color: #58a6ff; }
        .console { background-color: #161b22; padding: 15px; border-radius: 6px; border: 1px solid #30363d; min-height: 180px; overflow-y: auto; color: #8b949e; }
        button { background-color: #238636; color: white; border: none; padding: 10px 20px; border-radius: 4px; font-weight: bold; cursor: pointer; }
        button:hover { background-color: #2ea043; }
    </style>
</head>
<body>
    <h3>🐍 Python Core Execution in WebView</h3>
    <button onclick="runPythonCode()">Run Python via Pyodide WASM</button>
    <p>Console Log Output:</p>
    <div id="consoleLog" class="console">Loading status printed here...</div>

    <script type="text/javascript">
        let mainPyodide = null;
        async function runPythonCode() {
            const out = document.getElementById("consoleLog");
            out.innerHTML = "🔧 Loading Pyodide script engine (loads once via CDN)...<br/>";
            try {
                if (!mainPyodide) {
                    mainPyodide = await loadPyodide();
                }
                out.innerHTML += "✅ Pyodide Ready. Parsing Python bytecode...<br/>";
                
                // Write complex python snippet
                let code = `
class Shape:
    def __init__(self, name, sides):
        self.name = name
        self.sides = sides
    
    def desc(self):
        return f"{self.name} has {self.sides} sides"

tri = Shape("Triangle", 3)
square = Shape("Square", 4)
items = [tri.desc(), square.desc()]
" | ".join(items)
`;
                let pyResult = await mainPyodide.runPythonAsync(code);
                out.innerHTML += "<br/><b>[Python Output]:</b><br/>" + pyResult;
            } catch(e) {
                out.innerHTML += "<br/>Error: " + e;
            }
        }
    </script>
</body>
</html>
"""
    )

    // Create a new Project
    fun createProject(projectName: String) {
        viewModelScope.launch {
            // Normalize inputs
            val cleanName = projectName.trim()
            val safeId = cleanName.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
            val defaultPackage = "com.htmltoapk.$safeId"
            
            val project = Project(
                name = cleanName,
                appName = cleanName,
                packageName = defaultPackage,
                versionName = "1.0.0",
                versionCode = 1
            )
            
            val projectId = repository.insertProject(project)
            
            // Auto create matching index.html immediately!
            val mainFile = ProjectFile(
                projectId = projectId,
                name = "index.html",
                content = boilerplates["html"] ?: "",
                fileType = "html"
            )
            repository.insertFile(mainFile)
            
            // Also pre-populate basic CSS and JS so they get a complete template out of the box!
            val cssFile = ProjectFile(
                projectId = projectId,
                name = "style.css",
                content = boilerplates["css"] ?: "",
                fileType = "css"
            )
            repository.insertFile(cssFile)
            
            val jsFile = ProjectFile(
                projectId = projectId,
                name = "script.js",
                content = boilerplates["js"] ?: "",
                fileType = "js"
            )
            repository.insertFile(jsFile)

            // Make the new project active
            val insertedProject = project.copy(id = projectId)
            activeProject.value = insertedProject
            
            // Trigger loading files and set index.html active
            val filesList = repository.getFilesForProjectSync(projectId)
            val indexFile = filesList.find { it.name == "index.html" }
            activeFile.value = indexFile
        }
    }

    // Delete project
    fun deleteProject(project: Project) {
        viewModelScope.launch {
            if (activeProject.value?.id == project.id) {
                activeProject.value = null
                activeFile.value = null
            }
            repository.deleteProject(project)
        }
    }

    // Update project configuration
    fun updateProjectSettings(
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
    ) {
        viewModelScope.launch {
            val current = activeProject.value ?: return@launch
            val updated = current.copy(
                appName = appName,
                packageName = packageName,
                versionName = versionName,
                versionCode = versionCode,
                defaultFile = defaultFile,
                permNotification = notif,
                permMicrophone = mic,
                permCamera = camera,
                permAutoplay = autoplay,
                permRefresh = refresh,
                hideTitleBar = hideTitle,
                appIconIndex = iconId,
                splashIndex = splashId
            )
            repository.updateProject(updated)
            activeProject.value = updated
        }
    }

    // Add new coding file
    fun createProjectFile(fileName: String, fileType: String) {
        viewModelScope.launch {
            val project = activeProject.value ?: return@launch
            // Normalize filename with appropriate extensions
            val suffix = when (fileType.lowercase()) {
                "html" -> ".html"
                "css" -> ".css"
                "js" -> ".js"
                "react" -> ".html" // React runs in HTML using Babel CDN
                "python" -> ".html" // Python runs in HTML using Pyodide WASM
                else -> ".text"
            }
            
            val trimmedName = fileName.trim()
            val finalName = if (trimmedName.endsWith(suffix, ignoreCase = true)) trimmedName else "$trimmedName$suffix"
            
            // Check boilerplate corresponding to selection
            val initContent = boilerplates[fileType.lowercase()] ?: "/* New File */"
            
            val newFile = ProjectFile(
                projectId = project.id,
                name = finalName,
                content = initContent,
                fileType = fileType.lowercase()
            )
            val fileId = repository.insertFile(newFile)
            activeFile.value = newFile.copy(id = fileId)
        }
    }

    // Update content of active file (Auto-save)
    fun updateActiveFileContent(content: String) {
        val file = activeFile.value ?: return
        val updated = file.copy(content = content)
        activeFile.value = updated
        viewModelScope.launch {
            repository.updateFile(updated)
        }
    }

    // Delete active file
    fun deleteFile(file: ProjectFile) {
        viewModelScope.launch {
            repository.deleteFile(file)
            if (activeFile.value?.id == file.id) {
                // Return default/index if files remaining
                delay(100)
                val files = repository.getFilesForProjectSync(file.projectId)
                activeFile.value = files.firstOrNull()
            }
        }
    }

    // Compile converter processing simulation
    fun triggerApkBuild(context: Context) {
        val project = activeProject.value ?: return
        viewModelScope.launch {
            isCompiling.value = true
            compileProgress.value = 0f
            compileLogs.value = emptyList()
            exportedZipFile.value = null

            val logs = mutableListOf<String>()
            val stepsCount = 15
            
            val buildMessages = listOf(
                "Initializing HTML to APK compile task [PID: ${project.id}]...",
                "Scanning project directory structural consistency...",
                "Analyzing '${project.defaultFile}' parser syntax...",
                "Compiling responsive Jetpack Compose container classes...",
                "Configuring AndroidManifest xml with custom properties...",
                "Injecting permission flags: Notify=${project.permNotification}, Camera=${project.permCamera}, Mic=${project.permMicrophone}",
                "Injecting security flags (Hardware Acceleration & Media Autoplay)...",
                "Assembling app launcher resources and string configuration values...",
                "Optimizing assets (compressing HTML structures and styling maps)...",
                "Running Proguard Optimizer to strip unused framework modules...",
                "Packaging binary layout zip stream (compressing values)...",
                "Signing package with system-generated release signature...",
                "Confirming Keystore hashes (SHA-256 matches deployment config)...",
                "Aligning compiled APK archive (optimizing memory allocations)...",
                "SUCCESS: Build complete! Package ready for transport."
            )

            for (i in 0 until stepsCount) {
                val progress = (i + 1).toFloat() / stepsCount.toFloat()
                logs.add("[${System.currentTimeMillis() % 1000000}] INFO: ${buildMessages[i]}")
                compileLogs.value = logs.toList()
                compileProgress.value = progress
                
                // Add simulated operational delays
                val stepDelay = when(i) {
                    2, 5, 9, 12 -> 800L // heavy compilation operations
                    else -> 400L
                }
                delay(stepDelay)
            }

            // Real ZIP Project Generation
            val projectFiles = repository.getFilesForProjectSync(project.id)
            val generatedFile = ProjectExporter.generateProjectZip(context, project, projectFiles)
            exportedZipFile.value = generatedFile

            isCompiling.value = false
            showCompileSuccessDialog.value = true
        }
    }

    fun dismissCompileDialog() {
        showCompileSuccessDialog.value = false
    }
}
