package com.example.harumub_front

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_watch_together.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*
import java.io.File
import java.text.SimpleDateFormat

// 1:1 영상통화
class TogetherActivity : AppCompatActivity() {
    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInterface

    // 현재 로그인하고 있는 사용자 아이디, 이름
    lateinit var id : String
    lateinit var roomCode : String
    lateinit var roomToken : String

    // 추천 정보
    lateinit var reco1_titleArray : ArrayList<String>
    lateinit var reco1_posterArray : ArrayList<String>
    lateinit var reco2_1_userId : String
    lateinit var reco2_2_userId : String
    lateinit var reco2_3_userId : String
    lateinit var reco2_4_userId : String
    lateinit var reco2_5_userId : String
    lateinit var reco2_1_title : ArrayList<String>
    lateinit var reco2_2_title : ArrayList<String>
    lateinit var reco2_3_title : ArrayList<String>
    lateinit var reco2_4_title : ArrayList<String>
    lateinit var reco2_5_title : ArrayList<String>
    lateinit var reco2_1_poster : ArrayList<String>
    lateinit var reco2_2_poster : ArrayList<String>
    lateinit var reco2_3_poster : ArrayList<String>
    lateinit var reco2_4_poster : ArrayList<String>
    lateinit var reco2_5_poster : ArrayList<String>
    lateinit var reco3_titleArray : ArrayList<String>
    lateinit var reco3_posterArray : ArrayList<String>
    lateinit var reco4_year : String
    lateinit var reco4_titleArray : ArrayList<String>
    lateinit var reco4_posterArray : ArrayList<String>
    lateinit var reco6_titleArray : ArrayList<String>
    lateinit var reco6_posterArray : ArrayList<String>

    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }
        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }
    }

    private var isChannelActivate = false
    lateinit var emoji1 : ImageView
    lateinit var emoji2 : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_together)

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        id = intent.getStringExtra("user_id").toString()
        roomCode = intent.getStringExtra("roomCode").toString()
        roomToken = intent.getStringExtra("roomToken").toString()

        reco1_titleArray = intent.getSerializableExtra("reco1_titleArray") as ArrayList<String>
        reco1_posterArray = intent.getSerializableExtra("reco1_posterArray") as ArrayList<String>
        reco2_1_userId = intent.getStringExtra("reco2_1_userId").toString()
        reco2_2_userId = intent.getStringExtra("reco2_1_userId").toString()
        reco2_3_userId = intent.getStringExtra("reco2_1_userId").toString()
        reco2_4_userId = intent.getStringExtra("reco2_1_userId").toString()
        reco2_5_userId = intent.getStringExtra("reco2_1_userId").toString()
        reco2_1_title = intent.getSerializableExtra("reco2_1_title") as ArrayList<String>
        reco2_2_title = intent.getSerializableExtra("reco2_2_title") as ArrayList<String>
        reco2_3_title = intent.getSerializableExtra("reco2_3_title") as ArrayList<String>
        reco2_4_title = intent.getSerializableExtra("reco2_4_title") as ArrayList<String>
        reco2_5_title = intent.getSerializableExtra("reco2_5_title") as ArrayList<String>
        reco2_1_poster = intent.getSerializableExtra("reco2_1_poster") as ArrayList<String>
        reco2_2_poster = intent.getSerializableExtra("reco2_2_poster") as ArrayList<String>
        reco2_3_poster = intent.getSerializableExtra("reco2_3_poster") as ArrayList<String>
        reco2_4_poster = intent.getSerializableExtra("reco2_4_poster") as ArrayList<String>
        reco2_5_poster = intent.getSerializableExtra("reco2_5_poster") as ArrayList<String>
        reco3_titleArray = intent.getSerializableExtra("reco3_titleArray") as ArrayList<String>
        reco3_posterArray = intent.getSerializableExtra("reco3_posterArray") as ArrayList<String>
        reco4_year = intent.getStringExtra("reco4_year").toString()
        reco4_titleArray = intent.getSerializableExtra("reco4_titleArray") as ArrayList<String>
        reco4_posterArray = intent.getSerializableExtra("reco4_posterArray") as ArrayList<String>
        reco6_titleArray = intent.getSerializableExtra("reco6_titleArray") as ArrayList<String>
        reco6_posterArray = intent.getSerializableExtra("reco6_posterArray") as ArrayList<String>


        // 메인 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id") && intent.hasExtra("roomCode") && intent.hasExtra("roomToken")) {
            Log.e("TogetherActivity", "입장에서 받아온 id : $id , " +
                    "\n방 코드: $roomCode , " +
                    "\n방 토큰: $roomToken")
        } else {
            Log.e("TogetherActivity", "가져온 데이터 없음")
        }
        // 카메라, 오디오 권한 부여 후 엔진 초기화 및 채널 참가
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
            && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            Log.e("TogetherActivity", "권한 부여 완료")
            initAgoraEngineAndJoinChannel(roomCode, roomToken)
        } else Log.e("TogetherActivity", "권한 부여 실패")
    }
    // 엔진 초기화 및 채널 참가 과정
    private fun initAgoraEngineAndJoinChannel(roomCode: String, roomToken: String) {
        Log.e("TogetherActivity", "참가 과정 시작")

        initializeAgoraEngine()
        setupVideoProfile()
        setupLocalVideo()
        joinChannel(roomCode, roomToken)
    }
    // #1
    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, getString(R.string.AGORA_APP_ID), mRtcEventHandler)

            Log.e("TogetherActivity", "rtc엔진 생성 완료")
        } catch (e: Exception) { Log.e("TogetherActivity", "rtc엔진 생성 실패")
            Log.e(LOG_TAG, Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }
    // #2
    private fun setupVideoProfile() {
        mRtcEngine!!.enableVideo()
        // mRtcEngine!!.setVideoProfile(Constants.VIDEO_PROFILE_360P, false) // Earlier than 2.3.0

        mRtcEngine!!.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT)
        )

        Log.e("TogetherActivity", "rtc엔진 - 카메라 설정 완료")
    }
    // #3
    private fun setupLocalVideo() {
        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))

        Log.e("TogetherActivity", "로컬 사용자 비디오 설정 완료")
    }
    // #4
    private fun joinChannel(roomCode: String, roomToken: String) {
        mRtcEngine!!.joinChannel(roomToken, roomCode, null, 0) // uid 명시X > 자동 생성

        Log.e("TogetherActivity", "채널 참가 완료")
        verifyStoragePermission(this) // 캡처 관련 권한 설정

        isChannelActivate = true
        var time = 0

        // captureScreen("together", roomCode+"_"+time+".jpg", id, roomCode, time.toString())

        while(isChannelActivate) { // 채널이 생성되고 활성화되어 있는 동안 계속 캡처 및 감정 출력
            // 캡처하기                     // s3 버킷 이름 /   파일 이름    "_"+id+     / user_id / channelName / time
            captureScreen("together", roomCode+"_"+time+".jpg", id, roomCode, time.toString())

            val map = HashMap<String, String>()
            map.put("id", id)
            map.put("roomCode", roomCode)
            map.put("time", time.toString())
            val call = retrofitInterface.executeWatchTogetherImageCapture(map)
            call!!.enqueue(object : Callback<WatchTogether?> {
                override fun onResponse(call: Call<WatchTogether?>, response: Response<WatchTogether?>) {
                    if (response.code() == 200) {
                        val result = response.body() // 감정 배열
                        Log.w("TogetherActivity", "감정 배열 받고 출력")

                        shareEmotions(result!!.emotion_count_array, time) // 감정 출력
                    }
                    else if (response.code() == 400) {
                        Toast.makeText(this@TogetherActivity, "실시간 캡처 오류",
                            Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<WatchTogether?>, t: Throwable) {
                    Toast.makeText(this@TogetherActivity, t.message,
                        Toast.LENGTH_LONG).show()
                }
            })
            time += 10 // 10초씩 더해주기
        }
    }
    // 화면 캡처 > 이미지 파일 > s3버킷 전달
    private fun captureScreen(s3Bucket_FolderName: String, fileName: String, user_id: String, channelName: String, time: String) {
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.KOREA).format(Date())

        mRtcEngine?.takeSnapshot(channelName, 0,
            "/storage/emulated/0/Android/data/com.example.harumub_front/files/Pictures/$timeStamp.jpg"
        )
        Log.w("TogetherActivity", "$timeStamp - 사용자 비디오 캡처 완료")
        Thread.sleep(10000) // 10초 쉬어주기

        var storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) // File?
        var outFile = File(storageDir, "$timeStamp.jpg")
        var curPath = storageDir?.absolutePath // 외부 저장소 접근
        var fileUri = FileProvider.getUriForFile(this, "com.example.harumub_front", outFile)

        if (outFile != null) {
            uploadWithTransferUtilty(s3Bucket_FolderName, fileName, outFile) // file
        }
    }
    // 감정 출력
    private fun shareEmotions(emotion_count_array: List<Emotion>, second: Int) {
        // 감정 이모티콘 출력 - 서버에서 받아온 감정 배열
        val emotions = emotion_count_array // 리스트 중 첫번째 배열 - 감정 배열은 시간대별 하나만 전달받음
        val counts = intArrayOf( // 감정별 최종 횟수(정수값) 배열
            emotions[0].HAPPY, emotions[1].SAD, emotions[2].ANGRY, emotions[3].CONFUSED,
            emotions[4].DISGUSTED, emotions[5].SURPRISED, emotions[6].FEAR
        )
        val emojis = intArrayOf( // 감정 이미지 Int 배열
            R.drawable.happy, R.drawable.sad, R.drawable.angry,
            R.drawable.confused, R.drawable.disgusted, R.drawable.surprised,
            R.drawable.fear, R.drawable.calm
        )
        var check = 0
        for (i in 0..6) { // 감정 배열 값 찾기
            if (counts[i] < 1) continue
            else if(counts[i] == 1) {
                for (j in 0..6) { // 이미지 찾기
                    if (i==j) {
                        if(check == 0) {
                            emoji1.setImageResource(emojis[j])
                            check = 1
                        }
                        else if(check == 1) {
                            emoji2.setImageResource(emojis[j])
                            check = 2
                        }
                    }
                }
            }
            else if (counts[i] == 2) {
                for (j in 0..6) { // 이미지 찾기
                    if (i==j) {
                        if(check == 0) {
                            emoji1.setImageResource(emojis[j])
                            emoji2.setImageResource(emojis[j])
                            check = 2
                        }
                    }
                }
            }
        }
        Log.w("TogetherActivity", "$second 초: 감정 출력 완료")
    }

    // 권한 부여 설정
    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(LOG_TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(permission),
                requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // 원래는 X
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)

        when (requestCode) {
            PERMISSION_REQ_ID_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                    finish()
                }
            }
            PERMISSION_REQ_ID_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel(roomCode, roomToken)
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA)
                    finish()
                }
            }
        }
    }

    private fun showLongToast(msg: String) {
        this.runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    override fun onDestroy() {
        super.onDestroy()

        leaveChannel() // 채널 떠나기

        RtcEngine.destroy()
        mRtcEngine = null
    }
    // 화면 끄기 버튼
    fun onLocalVideoMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        mRtcEngine!!.muteLocalVideoStream(iv.isSelected)

        val container = findViewById(R.id.local_video_view_container) as FrameLayout
        val surfaceView = container.getChildAt(0) as SurfaceView
        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE

        Log.w("TogetherActivity", "내 비디오 화면 on/off 버튼 클릭")
    }
    // 음소거 버튼
    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
        Log.w("TogetherActivity", "음소거 버튼 클릭")
    }
    // 카메라 방향 전환 버튼
    fun onSwitchCameraClicked(view: View) {
        mRtcEngine!!.switchCamera()
        Log.w("TogetherActivity", "카메라 방향 전환")
    }
    // 통화 종료 버튼
    fun onEndCallClicked(view: View) {
        finish()

        val map = HashMap<String, String>()
        map.put("id", id)
        map.put("roomCode", roomCode)
        val call = retrofitInterface.executeWatchTogetherEnd(map)
        call!!.enqueue(object : Callback<Void?> {
            override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                if (response.code() == 200) {
                    Log.w("TogetherActivity", "Room & Room Code 삭제")
                    isChannelActivate = false

                    // 입장 페이지로 다시 돌아가기
                    val intent = Intent(applicationContext, EnterActivity::class.java)
                    intent.putExtra("user_id", id)
                    intent.putExtra("reco1_titleArray", reco1_titleArray)
                    intent.putExtra("reco1_posterArray", reco1_posterArray)
                    intent.putExtra("reco2_1_userId", reco2_1_userId)
                    intent.putExtra("reco2_2_userId", reco2_2_userId)
                    intent.putExtra("reco2_3_userId", reco2_3_userId)
                    intent.putExtra("reco2_4_userId", reco2_4_userId)
                    intent.putExtra("reco2_5_userId", reco2_5_userId)
                    intent.putExtra("reco2_1_title", reco2_1_title)
                    intent.putExtra("reco2_2_title", reco2_2_title)
                    intent.putExtra("reco2_3_title", reco2_3_title)
                    intent.putExtra("reco2_4_title", reco2_4_title)
                    intent.putExtra("reco2_5_title", reco2_5_title)
                    intent.putExtra("reco2_1_poster", reco2_1_poster)
                    intent.putExtra("reco2_2_poster", reco2_2_poster)
                    intent.putExtra("reco2_3_poster", reco2_3_poster)
                    intent.putExtra("reco2_4_poster", reco2_4_poster)
                    intent.putExtra("reco2_5_poster", reco2_5_poster)
                    intent.putExtra("reco3_titleArray", reco3_titleArray)
                    intent.putExtra("reco3_posterArray", reco3_posterArray)
                    intent.putExtra("reco4_year", reco4_year)
                    intent.putExtra("reco4_titleArray", reco4_titleArray)
                    intent.putExtra("reco4_posterArray", reco4_posterArray)
                    intent.putExtra("reco6_titleArray", reco6_titleArray)
                    intent.putExtra("reco6_posterArray", reco6_posterArray)
                    startActivity(intent)
                }
                else if (response.code() == 400) {
                    Toast.makeText(this@TogetherActivity, "실시간 캡처 오류",
                        Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Void?>, t: Throwable) {
                Toast.makeText(this@TogetherActivity, t.message,
                    Toast.LENGTH_LONG).show()
            }
        })
    }

    // 1:1 >> 하나의 상대방(원격 사용자) 비디오만 출력
    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)

        if (container.childCount >= 1) {
            return
        }
        // video renderer view 생성
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)
        // 원격 사용자(상대방)의 비디오 뷰 초기화
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))

        surfaceView.tag = uid // for mark purpose
        val tipMsg = findViewById<TextView>(R.id.quick_tips_when_use_agora_sdk) // optional UI
        tipMsg.visibility = View.GONE

        Log.w("TogetherActivity", "상대의 비디오 출력")
    }
    // 채널을 떠날 때
    private fun leaveChannel() {
        mRtcEngine!!.leaveChannel()
        Log.w("TogetherActivity", "채널을 떠남")
        isChannelActivate = false
    }
    // 상대방이 채널을 떠났을 때
    private fun onRemoteUserLeft() {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        container.removeAllViews()

        val tipMsg = findViewById<TextView>(R.id.quick_tips_when_use_agora_sdk) // optional UI
        tipMsg.visibility = View.VISIBLE

        Log.w("TogetherActivity", "상대방이 채널을 떠남")
        isChannelActivate = false
    }
    // 상대방의 비디오가 꺼졌을 때
    private fun onRemoteUserVideoMuted(uid: Int, muted: Boolean) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)

        val surfaceView = container.getChildAt(0) as SurfaceView

        val tag = surfaceView.tag
        if (tag != null && tag as Int == uid) {
            surfaceView.visibility = if (muted) View.GONE else View.VISIBLE
        }
        Log.w("TogetherActivity", "상대방 비디오 꺼짐")
    }
    // static -> companion object : 클래스 내부에 정의된 singleton value
    companion object {
        private val LOG_TAG = TogetherActivity::class.java.simpleName
        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

        private final val REQUEST_EXTERNAL_STORAGE = 1
        private var PERMISSION_STORAGE = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        fun verifyStoragePermission(activity: Activity) {
            val permission = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission!=PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    PERMISSION_STORAGE, REQUEST_EXTERNAL_STORAGE)
            }
        }
    }
    // S3 Bucket Upload
    fun uploadWithTransferUtilty(s3Bucket_FolderName: String?, fileName: String?, file: File?) {
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials(getString(R.string.AWS_ACCESS_KEY), getString(R.string.AWS_SECRET_KEY)) // IAM User의 (accessKey, secretKey)
        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))
        val transferUtility =
            TransferUtility.builder().s3Client(s3Client).context(this.applicationContext).build()
        TransferNetworkLossHandler.getInstance(this.applicationContext)
        val uploadObserver =
            transferUtility.upload("allonsybucket1/$s3Bucket_FolderName", fileName, file) // (bucket name, file 이름, file 객체)
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(s3_id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    // Handle a completed upload
                    Log.d("S3 Bucket ", "Upload Completed!")

                    // S3 Bucket에 file 업로드 후 Emulator에서 삭제
                    if (file != null) {
                        file.delete()
                        Log.d("Emulator : ", "파일 삭제")
                    }
                    else {
                        Log.d("Emulator : ", "삭제할 파일이 없습니다.")
                    }
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (current.toDouble() / total * 100.0).toInt()
                Log.d("TogetherActivity", "UPLOAD - - ID: \$id, percent done = \$done")
            }

            override fun onError(id: Int, ex: java.lang.Exception) {
                Log.d("TogetherActivity", "UPLOAD ERROR - - ID: \$id - - EX:$ex")
            }
        })
    }
}