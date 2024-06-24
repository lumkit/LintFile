package io.github.lumkit.lintfile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.lumkit.io.LintFile
import io.github.lumkit.io.LintFileConfiguration
import io.github.lumkit.io.data.IoModel
import io.github.lumkit.io.data.PermissionType
import io.github.lumkit.io.file
import io.github.lumkit.io.requestAccessPermission
import io.github.lumkit.io.shell.ShizukuUtil
import io.github.lumkit.io.takePersistableUriPermission
import io.github.lumkit.io.use
import io.github.lumkit.lintfile.model.MainViewModel
import io.github.lumkit.lintfile.ui.theme.LintFileTheme
import io.github.lumkit.lintfile.util.FileSizeConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        LintFileConfiguration.instance.init(this)

        setContent {
            LintFileTheme {
                Greeting()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.hasManageExternalStorage.value =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        takePersistableUriPermission(0x000002, requestCode, resultCode, data)
    }

    override fun onDestroy() {
        LintFileConfiguration.instance.destroy()
        super.onDestroy()
    }
}

@Composable
private fun Greeting() {
    val activity = LocalContext.current as ComponentActivity
    val viewModel = viewModel<MainViewModel>()

    var hasWritePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val isManageExternal by viewModel.hasManageExternalStorage.collectAsStateWithLifecycle()

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasWritePermission = it[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
    }

    AnimatedContent(
        targetState = hasWritePermission && isManageExternal, label = "AnimatedContent"
    ) {
        if (it) {
            Content()
        } else {
            Permission(
                activity,
                hasWritePermission,
                isManageExternal,
                writePermissionLauncher,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Permission(
    activity: ComponentActivity,
    hasWritePermission: Boolean,
    isManageExternal: Boolean,
    writePermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.title_register_permisstions))
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Button(
                onClick = {
                    writePermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                },
                enabled = !hasWritePermission
            ) {
                Text(text = stringResource(id = R.string.text_register_external_storage_permission))
            }

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            val intent =
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:${activity.packageName}")
                            activity.startActivity(intent)
                        }
                    }
                },
                enabled = !isManageExternal
            ) {
                Text(text = stringResource(id = R.string.text_register_manage_external_permission))
            }
        }
    }

}

@SuppressLint("DefaultLocale", "SimpleDateFormat", "MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content() {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: MainViewModel = viewModel()
    val currentList = remember { mutableStateListOf<LintFile>() }
    val currentFile = viewModel.currentFile.collectAsStateWithLifecycle()
    val loadDialogState = rememberSaveable { mutableStateOf(false) }
    var columnCount by rememberSaveable { mutableIntStateOf(1) }
    var openFile by remember { mutableStateOf(currentFile.value) }

    var fileInfoState by remember { mutableStateOf(false) }

    var permissionDialogState by remember { mutableStateOf(false) }
    var permissionType by remember { mutableStateOf(PermissionType.EXTERNAL_STORAGE) }

    var headRow by remember { mutableStateOf(Collections.synchronizedList(arrayListOf<LintFile>())) }
    val lazyRowState = rememberLazyListState()

    if (loadDialogState.value) {
        Dialog(
            onDismissRequest = {}
        ) {
            Card(
                modifier = Modifier.size(300.dp, 200.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(90.dp))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { currentFile.value }
            .onEach {
                loadDialogState.value = true

                // 加载文件列表
                currentList.clear()
                val files = it.listFiles().sortedBy { file -> file.path }
                currentList.addAll(files)

                loadDialogState.value = false
            }.flowOn(Dispatchers.IO)
            .launchIn(this)

        snapshotFlow { currentFile.value }
            .onEach {
                //加载文件路径
                val builder = StringBuilder()
                val synchronizedList = Collections.synchronizedList(arrayListOf<LintFile>())
                it.path.split(File.separator).forEach { name ->
                    if (name.isNotEmpty()) {
                        builder.append("/")
                        builder.append(name)
                        synchronizedList.add(file(builder.toString()))
                    }
                }
                headRow = synchronizedList
                lazyRowState.animateScrollToItem(headRow.size * 2)
            }.launchIn(this)
    }

    BackHandler {
        val parent = currentFile.value.getParentFile()
        if (Environment.getExternalStorageDirectory().absolutePath == parent.path) {
            viewModel.push(parent)
        } else {
            parent.use(
                onRequestPermission = { type ->
                    permissionType = type
                    permissionDialogState = true
                },
                granted = {
                    if (this.isDirectory()) {
                        viewModel.pop(activity::finish)
                    } else if (this.isFile()) {
                        fileInfoState = true
                    } else {
                        activity.finish()
                    }
                }
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.title_main))
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                columnCount = if (columnCount > 1) 1 else 2
                            }
                        ) {
                            Icon(
                                imageVector = if (columnCount > 1) {
                                    Icons.Default.GridView
                                } else {
                                    Icons.Default.List
                                },
                                contentDescription = null
                            )
                        }

                        Column {
                            var modeState by remember { mutableStateOf(false) }
                            var mode by remember { mutableStateOf(LintFileConfiguration.instance.ioMode) }
                            TextButton(
                                onClick = {
                                    modeState = true
                                }
                            ) {
                                Text(text = mode.name)
                            }
                            DropdownMenu(
                                expanded = modeState,
                                onDismissRequest = { modeState = false }
                            ) {
                                IoModel.entries.forEach {
                                    DropdownMenuItem(
                                        text = {
                                            Text(it.name)
                                        },
                                        onClick = {
                                            val temp = LintFileConfiguration.instance.ioMode
                                            LintFileConfiguration.instance.ioMode = it
                                            currentFile.value.use(
                                                onRequestPermission = { type ->
                                                    permissionType = type
                                                    permissionDialogState = true
                                                    LintFileConfiguration.instance.ioMode = temp
                                                },
                                                granted = {
                                                    viewModel.push(currentFile.value)
                                                    mode = it
                                                    modeState = false
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                state = lazyRowState,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 16.dp)
            ) {
                headRow.forEachIndexed { index, lintFile ->
                    item {
                        FilterChip(
                            selected = index == headRow.size - 1,
                            onClick = {
                                if (Environment.getExternalStorageDirectory().absolutePath == lintFile.path) {
                                    viewModel.push(lintFile)
                                } else {
                                    lintFile.use(
                                        onRequestPermission = { type ->
                                            permissionType = type
                                            permissionDialogState = true
                                        },
                                        granted = {
                                            if (this.isDirectory()) {
                                                viewModel.push(lintFile)
                                            } else if (this.isFile()) {
                                                fileInfoState = true
                                            } else {
                                                Toast.makeText(
                                                    activity,
                                                    R.string.text_file_is_null,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                }
                            },
                            label = {
                                Text(text = lintFile.name)
                            }
                        )
                    }
                    if (index < headRow.size - 1) {
                        item {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                columns = GridCells.Fixed(columnCount),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentList) { file ->
                    FileItem(file) { item ->
                        openFile = item
                        item.use(
                            onRequestPermission = { type ->
                                permissionType = type
                                permissionDialogState = true
                            },
                            granted = {
                                if (this.isDirectory()) {
                                    viewModel.push(item)
                                } else if (this.isFile()) {
                                    fileInfoState = true
                                } else {
                                    Toast.makeText(
                                        activity,
                                        R.string.text_file_is_null,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (fileInfoState) {
        val dateFormatText = stringResource(id = R.string.format_file_last_update)

        val dateFormat = remember { SimpleDateFormat(dateFormatText) }

        AlertDialog(
            onDismissRequest = { fileInfoState = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fileInfoState = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.text_confirom))
                }
            },
            title = {
                Text(text = stringResource(id = R.string.title_dialog_file_info))
            },
            text = {
                Column {
                    Text(
                        text = String.format(
                            stringResource(id = R.string.format_file_name),
                            openFile.name
                        )
                    )
                    Text(
                        text = String.format(
                            stringResource(id = R.string.format_file_size),
                            FileSizeConverter.autoConvert(
                                openFile.length(),
                                FileSizeConverter.Unit.B
                            )
                        )
                    )
                    Text(text = String.format("    bytes: %d", openFile.length()))
                    Text(text = dateFormat.format(Date(openFile.lastModified())))
                }
            }
        )
    }

    if (permissionDialogState) {
        AlertDialog(
            onDismissRequest = { permissionDialogState = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionDialogState = false
                        when (permissionType) {
                            PermissionType.EXTERNAL_STORAGE -> {
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                    ),
                                    0x000001
                                )
                            }

                            PermissionType.MANAGE_STORAGE -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent =
                                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    intent.data = Uri.parse("package:" + activity.packageName)
                                    activity.startActivity(intent)
                                }
                            }

                            PermissionType.STORAGE_ACCESS_FRAMEWORK -> {
                                activity.requestAccessPermission(0x000002, openFile.path)
                            }

                            PermissionType.SU -> {

                            }
                            PermissionType.SHIZUKU -> ShizukuUtil.requestPermission()
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.text_confirom))
                }
            },
            title = {
                Text(text = stringResource(id = R.string.title_request_permission))
            },
            text = {
                Text(
                    text = when (permissionType) {
                        PermissionType.EXTERNAL_STORAGE -> activity.getString(R.string.text_permission_external_storage)
                        PermissionType.MANAGE_STORAGE -> activity.getString(R.string.text_permission_manage_storage)
                        PermissionType.STORAGE_ACCESS_FRAMEWORK -> activity.getString(R.string.text_permission_storage_access_framework)
                        PermissionType.SU -> activity.getString(R.string.text_permission_su)
                        PermissionType.SHIZUKU -> activity.getString(R.string.text_permission_shizuku)
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileItem(lintFile: LintFile, onClick: (LintFile) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            onClick(lintFile)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (lintFile.isDirectory()) {
                    Icons.Default.Folder
                } else {
                    Icons.Default.InsertDriveFile
                },
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = lintFile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (lintFile.isDirectory()) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}