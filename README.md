# Lint File

[![License](https://img.shields.io/github/license/lumkit/LintFile)](LICENSE)
[![Version](https://img.shields.io/github/v/release/lumkit/LintFile?include_prereleases)](https://github.com/lumkit/LintFile/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lumkit/lint-file)](https://central.sonatype.com/artifact/io.github.lumkit/lint-file/)

一个适用于Android平台的文件操作库 —— kt库。

# 简介

这是一个多功能文件库，它简化了开发人员访问安卓设备磁盘文件的步骤。
它支持基本的Java文件Api，集成了高级文件操作Api，如“SuFile”和“ShizukuFile”， 并支持访问权限的自动申请。
开发者不再需要关心适配不同Android版本的新变化，一切都交给LintFile！

# 特性

- [x] 适配Android 7.0~Android 15
- [x] 支持Root文件操作和Root打开文件IO流
- [x] 支持Shizuku文件操作和Shizuku打开文件操作流
- [x] 高性能的Root文件操作和Shizuku文件操作
- [x] 简单易用的File Api（与java.io.File类似）
- [x] 自动化权限申请

# 将Lint File导入你的项目

1. 给你的项目配置maven仓库
    ```kotlin
    repositories {
        google()
        mavenCentral()
    }
    ```

2. 导入lint-file依赖
   ```kotlin
   dependencies {
       implementation("io.github.lumkit:lint-file:1.0.5")
   }
   ```

3. 初始化文件操作库.
   ```kotlin
   class MainActivity : ComponentActivity() {
   
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
   
           // 初始化Android上下文
           LintFileConfiguration.instance.init(this)
   
           // 进行其它操作
   
       }
   
       override fun onDestroy() {
           // 释放文件操作库
           LintFileConfiguration.instance.destroy()
           super.onDestroy()
       }
   }
   ```

4. 开始使用
   * 自动化权限
      ```kotlin
      // 1. 先获取LintFile实例
      // file扩展函数会根据文件路径自动创建合适的LintFile实例
      // 你也可以手动创建LintFile的不同实现：DefaultFile、StorageAccessFrameworkFile、SuFile和ShizukuFile
      val lintFile = file("/xxx/xxx/xxx")
   
      // 2. 通过use扩展函数进行自动化权限申请
      lintFile.use(
          // 实现权限注册，这里只是简单实现，实际上你可以在此弹出模态框来进一步优化交互体验
          onRequestPermission = { type: PermissionType ->
              when (type) {
                  // 外部存储权限
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
                  // 所有文件访问权限
                  PermissionType.MANAGE_STORAGE -> {
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                          val intent =
                              Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                          intent.data = Uri.parse("package:" + activity.packageName)
                          activity.startActivity(intent)
                      }
                  }
                  // SAF框架文件访问权限
                  PermissionType.STORAGE_ACCESS_FRAMEWORK -> {
                      activity.requestAccessPermission(0x000002, openFile.path)
                  }
                  // Root权限
                  PermissionType.SU -> {
                      // 这里没啥好实现的
                  }
                  // Shizuku权限
                  PermissionType.SHIZUKU -> try {
                      ShizukuUtil.requestPermission()
                  } catch (e: Exception) {
                      e.printStackTrace()
                      Toast.makeText(
                          activity,
                          R.string.text_shizuku_service_is_not_active,
                          Toast.LENGTH_SHORT
                      ).show()
                  }
              }
          },
          // 此回调作用域中表示已获取到需要的权限，可通过this: LintFile来调用文件操作API
          granted = {
              val fileName = this.name
              println(fileName)
          }
      )
      ```
      不要忘了！
      ```kotlin
      class MainActivity : ComponentActivity() {
   
          // 在此保存/Android/data的文件访问权限！
          override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
              super.onActivityResult(requestCode, resultCode, data)
              takePersistableUriPermission(12138, requestCode, resultCode, data)
          }
      }
      ```
   * 文件API操作（与java.io.File类型，这里就不过多赘述）
      ```kotlin
      // 获取Lint File实例
      val file = file("/x/xx/xxx")
   
      // 获取文件名
      val fileName = file.name
      // 获取文件路径
      val path = file.path
      //获取文件列表
      val fileList = file.list()
      ...
      ```
   
   * 打开IO流
      * 打开输入流
         ```kotlin
         //获取LintFile实例
         val file = file("/x/xx/xxx")
         // 打开输入流
         val inputStream = file.openInputStream()
         // 进行读取操作，需要手动关闭流
         ...
         ```
      * 打开输出流
         ```kotlin
         //获取LintFile实例
         val file = file("/x/xx/xxx")
         // 打开输出流
         val outputStream = file.openOutputStream()
         // 进行写入操作，需要手动关闭流
         ...
         ```

# End