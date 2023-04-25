package com.example.sdk_3divi_kotlin

//TODO: after the service starts working, comment on these lines and put what you downloaded from our SVN next to this project
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*

const val FLUTTER_ENGINE_ID = "flutter_engine"

class MainActivity : AppCompatActivity() {

    //TODO: after the service starts working, comment on these lines and put what you downloaded from our SVN next to this project
    private val CHANNEL = "com.flutterKotlin/result"
    lateinit var methodChannel: MethodChannel
    private lateinit var flutterEngine: FlutterEngine


    var TAG = "TEST_SDK"
    var score = 0.0
    var capturedIdDocument: ByteArray? = null
    var alivenessCheckImage: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dataDir: String = applicationInfo.dataDir
        val libDir: String = applicationInfo.nativeLibraryDir

        extractAssets()

        Log.d(TAG, "native lib dir: " + applicationInfo.nativeLibraryDir + "/libfacerec.so")



        flutterEngine = FlutterEngine(this)
        flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        FlutterEngineCache.getInstance().put(FLUTTER_ENGINE_ID, flutterEngine)
        val accessKey = "<yourAccessKey>"
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        methodChannel.invokeMethod("initiateSdk", mapOf("accessKey" to accessKey, "libDir" to libDir, "dataDir" to dataDir))//pass the "accessKey" argument
        methodChannel.setMethodCallHandler { call, result ->
            //Handle result from sdk
            when (call.method) {
                "onCaptureIdDocumentResult" -> { //Handle result from capture method
                    var documentFields = call.argument<Map<String, String>>("documentFields")
                    capturedIdDocument = call.argument<ByteArray>("capturedIdDocument")
                    println("------documentFields = $documentFields")
                    println("------capturedIdDocument = $capturedIdDocument")
                }
                "onAlivenessCheckResult" -> { //Handle result from aliveness method
                    var alive = call.argument<Boolean>("alive")
                    alivenessCheckImage = call.argument<ByteArray>("alivenessCheckImage")
                    println("------alive = $alive")
                    println("------alivenessCheckImage = $alivenessCheckImage")
                }
                "onFacialMatchingResult" -> { //Handle result from facialMatching method
                    score = call.argument<Double>("score")!!
                    println("------score = $score")
                }
                else -> {
                    result.notImplemented() //Handle other methods or unknown methods
                }
            }
        }

        //TODO: after the service starts working, comment on these lines and put what you downloaded from our SVN next to this project

        val text: TextView = findViewById(R.id.score)
        text.text = score.toString()

        //Call the "capture" method in Flutter and pass the "documentType" argument
        val buttonCapture: Button = findViewById(R.id.fab1)
        buttonCapture.setOnClickListener {
            val documentType = 1
            launchFlutterActivity("captureIdDocument", mapOf("documentType" to documentType))
        }

        //Call the "alivenessCheck" method in Flutter and pass the "Params"
        val buttonAlivenessAndFaceMatch: Button = findViewById(R.id.fab2)
        buttonAlivenessAndFaceMatch.setOnClickListener {
//            var capturedIdDocumentImage = ByteArray
            launchFlutterActivity(
                "alivenessAndFaceMatch",
                mapOf(
                    "doFaceMatch" to true,
                    "capturedIdDocumentImage" to capturedIdDocument
                )
            )
        }
        //Call the "alivenessCheck" method in Flutter
        val alivenessCheck: Button = findViewById(R.id.fab3)
        alivenessCheck.setOnClickListener {
            launchFlutterActivity("alivenessCheck", mapOf("" to ""))
        }

        //Call the "facialMatching" method in Flutter and pass the "facialMatchingParams" argument
        val facialMatching: Button = findViewById(R.id.fab4)
        facialMatching.setOnClickListener {
            launchFlutterActivity(
                "facialMatching",
                mapOf(
                    "alivenessCheckImage" to alivenessCheckImage,
                    "capturedIdDocumentImage" to capturedIdDocument,
                )
            )
        }
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

    private fun launchFlutterActivity(methodName: String, methodArg: Any?) {
        val intent = FlutterActivity
            .withCachedEngine(FLUTTER_ENGINE_ID)
            .build(this)
        methodChannel.invokeMethod(methodName, methodArg)
        startActivity(intent)
    }
}