package com.example.sdk_3divi_kotlin

import android.os.Bundle
import android.widget.Button
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vdt.face_recognition.sdk.FacerecService
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*
//TODO: after the service starts working, comment on these lines and put what you downloaded from our SVN next to this project
//import io.flutter.embedding.android.FlutterActivity
//import io.flutter.embedding.engine.FlutterEngine
//import io.flutter.embedding.engine.FlutterEngineCache
//import io.flutter.embedding.engine.dart.DartExecutor
//import io.flutter.plugin.common.MethodChannel

const val FLUTTER_ENGINE_ID = "flutter_engine"

class MainActivity : AppCompatActivity() {

    //TODO: after the service starts working, comment on these lines and put what you downloaded from our SVN next to this project
//    private val CHANNEL = "com.flutterKotlin/result"
//    lateinit var methodChannel: MethodChannel
//    private lateinit var flutterEngine: FlutterEngine


    var TAG = "TEST_SDK"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dataDir: String = applicationInfo.dataDir
        val libDir: String = applicationInfo.nativeLibraryDir

        extractAssets()
        val service = FacerecService.createService(
            applicationInfo.nativeLibraryDir + "/libfacerec.so",
            applicationInfo.dataDir + "/fsdk/conf/facerec",
            applicationInfo.dataDir + "/fsdk/license"
        )
        Log.d(TAG, "version = ${service.version}")

        //TODO: after the service starts working, comment on these lines and put what you downloaded from our SVN next to this project
//        val button: Button = findViewById(R.id.fab1)
//        button.setOnClickListener {
//            val documentType = 1
//            launchFlutterActivity("captureIdDocument", mapOf("documentType" to documentType))
//        }
//        flutterEngine = FlutterEngine(this)
//        flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
//        FlutterEngineCache.getInstance().put(FLUTTER_ENGINE_ID, flutterEngine)
//        val accessKey = "<yourAccessKey>"
//        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
//        methodChannel.invokeMethod("initiateSdk", mapOf("accessKey" to accessKey, "libDir" to libDir, "dataDir" to dataDir))//pass the "accessKey" argument


    }

    private fun extractAssets() {
        try {
            Log.d(TAG, "extractAssets")

            val appContext = applicationContext ?: return
            val asman: AssetManager = appContext.assets

            // read first line from assets-hash.txt

            // read first line from assets-hash.txt
            val new_hash = BufferedReader(InputStreamReader(asman.open("assets-hash.txt"))).readLine()

            // and compare it with what we have already

            // and compare it with what we have already
            val shpr: SharedPreferences = appContext.getSharedPreferences("fe9733f0bfb7", 0)

            val prev_hash = shpr.getString("assets-hash", null)

            // unpack everything again, if something changes

            // unpack everything again, if something changes
            if (prev_hash == null || prev_hash != new_hash) {
                val buffer = ByteArray(10240)
                val persistent_dir: String = appContext.getApplicationInfo().dataDir
                val queue: Queue<String> = ArrayDeque<String>()
                queue.add("conf")
                queue.add("share")
                queue.add("license")
                while (!queue.isEmpty()) {
                    val path = queue.element()
                    queue.remove()
                    val list = asman.list(path)
                    if (list!!.size == 0) {
                        val file_stream = asman.open(path!!)
                        val full_path = "$persistent_dir/fsdk/$path"
                        File(full_path).parentFile.mkdirs()
                        val out_file = FileOutputStream(full_path)
                        while (true) {
                            val read = file_stream.read(buffer)
                            if (read <= 0) break
                            out_file.write(buffer, 0, read)
                        }
                        file_stream.close()
                        out_file.close()
                    } else {
                        for (p in list) queue.add("$path/$p")
                    }
                }
                val editor = shpr.edit()
                editor.putString("assets-hash", new_hash)
                while (!editor.commit());
            }
        }
        catch (e: Exception) {
            e.message?.let { Log.e("UnpackAssets", it) };
            e.printStackTrace();
        }

    }

//    private fun launchFlutterActivity(methodName: String, methodArg: Any?) {
//        val intent = FlutterActivity
//            .withCachedEngine(FLUTTER_ENGINE_ID)
//            .build(this)
//        methodChannel.invokeMethod(methodName, methodArg)
//        startActivity(intent)
//    }
}