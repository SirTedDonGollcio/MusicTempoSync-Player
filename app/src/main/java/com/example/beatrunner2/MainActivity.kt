package com.example.beatrunner2

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import be.tarsos.dsp.onsets.OnsetHandler
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.pow


class MainActivity : AppCompatActivity(), SensorEventListener {

    val ACTIVITY_PERMISION_RQ = 505
    val MEDIA_PERMISION_RQ = 506

    var stepValueInt = 0
    private var running = false
    private var sensorManager: SensorManager? = null
    lateinit var runnable: Runnable
    private var handler = Handler()
    private val onsets // in seconds
            : MutableList<Double>? = null
    private val beats //in seconds
            : MutableList<Double>? = null
    private lateinit var mediaplayer: MediaPlayer
    private lateinit var selectedFile : Uri

    private var isArrowsShown = true
    private var isSlowAllowed = true

    private var isTempoButtonClicked_stepTempo = false
    private var is_1_ClickFull_stepTempo = false
    private var is_2_ClickFull_stepTempo = false
    private var is_3_ClickFull_stepTempo = false
    private var is_4_ClickFull_stepTempo = false
    private var is_5_ClickFull_stepTempo = false
    private var click1_stepTempo: Float = 0f
    private var click2_stepTempo: Float = 0f
    private var click3_stepTempo: Float = 0f
    private var click4_stepTempo: Float = 0f
    private var click5_stepTempo: Float = 0f
    private var timerTemp_stepTempo: Float = 10000f
    private var bpm_stepTempo: Float = 0f
    private  val timerStepTempo = object: CountDownTimer(10000, 1) {
        override fun onTick(millisUntilFinished: Long) {
            timerTemp_stepTempo = millisUntilFinished.toFloat()
        }

        override fun onFinish() {
            isTempoButtonClicked_stepTempo=false
            is_1_ClickFull_stepTempo = false
            is_2_ClickFull_stepTempo = false
            is_3_ClickFull_stepTempo = false
            is_4_ClickFull_stepTempo = false
            is_5_ClickFull_stepTempo = false
            click1_stepTempo = 0f
            click2_stepTempo = 0f
            click3_stepTempo = 0f
            click4_stepTempo = 0f
            click5_stepTempo = 0f
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkForPermission(android.Manifest.permission.ACTIVITY_RECOGNITION, "activity", ACTIVITY_PERMISION_RQ)
        checkForPermission(android.Manifest.permission.ACCESS_MEDIA_LOCATION, "media", MEDIA_PERMISION_RQ)


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mediaplayer = MediaPlayer.create(this, R.raw.music)

        seek_bar.progress = 0
        seek_bar.max = mediaplayer.duration
        //println(milliSecondsToTimer(mediaplayer.getDuration().toLong()))
        player_duration.setText(milliSecondsToTimer(mediaplayer.getDuration().toLong()))

        var isTempoButtonClicked = false
        var is_1_ClickFull = false
        var is_2_ClickFull = false
        var is_3_ClickFull = false
        var is_4_ClickFull = false
        var is_5_ClickFull = false
        var click1: Float = 0f
        var click2: Float = 0f
        var click3: Float = 0f
        var click4: Float = 0f
        var click5: Float = 0f
        var timerTemp: Float = 10000f
        var bpm: Float = 0f
        val timer = object: CountDownTimer(10000, 1) {
            override fun onTick(millisUntilFinished: Long) {
                timerTemp = millisUntilFinished.toFloat()
            }

            override fun onFinish() {
                isTempoButtonClicked=false
                is_1_ClickFull = false
                is_2_ClickFull = false
                is_3_ClickFull = false
                is_4_ClickFull = false
                is_5_ClickFull = false
                click1 = 0f
                click2 = 0f
                click3 = 0f
                click4 = 0f
                click5 = 0f
            }
        }

        var isTempoButtonClicked_song = false
        var is_1_ClickFull_song = false
        var is_2_ClickFull_song = false
        var is_3_ClickFull_song = false
        var is_4_ClickFull_song = false
        var is_5_ClickFull_song = false
        var click1_song: Float = 0f
        var click2_song: Float = 0f
        var click3_song: Float = 0f
        var click4_song: Float = 0f
        var click5_song: Float = 0f
        var timerTemp_song: Float = 10000f
        var bpm_song: Float = 0f
        val timer_song = object: CountDownTimer(10000, 1) {
            override fun onTick(millisUntilFinished: Long) {
                timerTemp_song = millisUntilFinished.toFloat()
            }

            override fun onFinish() {
                isTempoButtonClicked_song=false
                is_1_ClickFull_song = false
                is_2_ClickFull_song = false
                is_3_ClickFull_song = false
                is_4_ClickFull_song = false
                is_5_ClickFull_song = false
                click1_song = 0f
                click2_song = 0f
                click3_song = 0f
                click4_song = 0f
                click5_song = 0f
            }
        }



        bt_tapRunTempo.setOnClickListener()
        {
            if(!isTempoButtonClicked)
            {
                isTempoButtonClicked = true
                timer.start()
            } else {
                if(!is_1_ClickFull)
                {
                    is_1_ClickFull=true
                    click1 = timerTemp
                } else if(!is_2_ClickFull) {
                    is_2_ClickFull=true
                    click2 = timerTemp
                } else if(!is_3_ClickFull) {
                    is_3_ClickFull=true
                    click3 = timerTemp
                } else if(!is_4_ClickFull) {
                    is_4_ClickFull=true
                    click4 = timerTemp
                    bpm = 60000 / (((click1-click2)+(click2-click3)+(click3-click4))/3)
                    tv_runTempo.setText((bpm.toInt()).toString())
                } else if(!is_5_ClickFull) {
                    is_5_ClickFull=true
                    click5 = timerTemp
                    bpm = 60000 / (((click1-click2)+(click2-click3)+(click3-click4)+(click4-click5))/4)
                    tv_runTempo.setText((bpm.toInt()).toString())
                } else {
                    click1 = click2
                    click2 = click3
                    click3 = click4
                    click4 = click5
                    click5 = timerTemp
                    bpm = 60000 / (((click1-click2)+(click2-click3)+(click3-click4)+(click4-click5))/4)
                    tv_runTempo.setText((bpm.toInt()).toString())
                }
            }
        }

        bt_tapSongTempo.setOnClickListener()
        {
            if(!isTempoButtonClicked_song)
            {
                isTempoButtonClicked_song = true
                timer_song.start()
            } else {
                if(!is_1_ClickFull_song)
                {
                    is_1_ClickFull_song=true
                    click1_song = timerTemp_song
                } else if(!is_2_ClickFull_song) {
                    is_2_ClickFull_song=true
                    click2_song = timerTemp_song
                } else if(!is_3_ClickFull_song) {
                    is_3_ClickFull_song=true
                    click3_song = timerTemp_song
                } else if(!is_4_ClickFull_song) {
                    is_4_ClickFull_song=true
                    click4_song = timerTemp_song
                    bpm_song = 60000 / (((click1_song-click2_song)+(click2_song-click3_song)+(click3_song-click4_song))/3)
                    tv_songTempo.setText((bpm_song.toInt()).toString())
                } else if(!is_5_ClickFull_song) {
                    is_5_ClickFull_song=true
                    click5_song = timerTemp_song
                    bpm_song = 60000 / (((click1_song-click2_song)+(click2_song-click3_song)+(click3_song-click4_song)+(click4_song-click5_song))/4)
                    tv_songTempo.setText((bpm_song.toInt()).toString())
                } else {
                    click1_song = click2_song
                    click2_song = click3_song
                    click3_song = click4_song
                    click4_song = click5_song
                    click5_song = timerTemp_song
                    bpm_song = 60000 / (((click1_song-click2_song)+(click2_song-click3_song)+(click3_song-click4_song)+(click4_song-click5_song))/4)
                    tv_songTempo.setText((bpm_song.toInt()).toString())
                }
            }
        }

        var smallestSpeedFactor = 500f

        bt_refactorSpeed.setOnClickListener()
        {
            var factorTemp = 500f
            val metrumPow = 2f
            smallestSpeedFactor = 500f
            if(!(bpm==0f) && !(bpm_song==0f))
            {
                for (i in 0..4) {
                    if(isSlowAllowed) {
                        factorTemp = ((bpm / bpm_song) / (metrumPow.pow(i)))
                        //println(factorTemp.toString())
                        if ((1 - factorTemp).absoluteValue < (1 - smallestSpeedFactor).absoluteValue) {
                            smallestSpeedFactor = factorTemp
                            //println("Znalezione i to: " + smallestSpeedFactor.toString())
                        }
                    }
                    factorTemp = ((bpm/bpm_song)*(metrumPow.pow(i)))
                    //println("Bpm: " + bpm.toString() + " i song to " + bpm_song.toString() + " bpm/bpmsong " + (bpm/bpm_song).toString() + " pow " + (metrumPow.pow(i)).toString() )
                    //println(factorTemp.toString())
                    if((1-factorTemp).absoluteValue < (1-smallestSpeedFactor).absoluteValue)
                    {
                        smallestSpeedFactor = factorTemp
                        //println("Znalezione i to: " + smallestSpeedFactor.toString())
                    }
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if(!mediaplayer.isPlaying)
                    {
                        mediaplayer?.setPlaybackParams(mediaplayer?.getPlaybackParams().setSpeed(smallestSpeedFactor.toFloat()))
                        mediaplayer.pause()
                    } else {
                        mediaplayer?.setPlaybackParams(mediaplayer?.getPlaybackParams().setSpeed(smallestSpeedFactor.toFloat()))
                    }
                    Toast.makeText(this, "Song was refactor with value " + smallestSpeedFactor.toString(), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "BPM for song or run not set!", Toast.LENGTH_SHORT).show()
            }
        }


        bt_play.setOnClickListener()
        {
            if(!mediaplayer.isPlaying)
            {
                mediaplayer.start()

                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    mediaplayer?.setPlaybackParams(mediaplayer?.getPlaybackParams().setSpeed(1.25f))
                }*/
                bt_play.visibility = View.GONE
                bt_pause.visibility = View.VISIBLE
            }
        }

        bt_pause.setOnClickListener()
        {
            if(mediaplayer.isPlaying)
            {
                mediaplayer.pause()
                bt_play.visibility = View.VISIBLE
                bt_pause.visibility = View.GONE
            }
        }

        bt_explorer.setOnClickListener {

            val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)

        }

        bt_runTempoMinus.setOnClickListener()
        {
            if(bpm>0)
            {
                bpm+=-1
                tv_runTempo.setText(bpm.toInt().toString())
            }
        }

        bt_runTempoPlus.setOnClickListener()
        {
            if(bpm<1000)
            {
                bpm+=1
                tv_runTempo.setText(bpm.toInt().toString())
            }
        }

        bt_songTempoMinus.setOnClickListener()
        {
            if(bpm_song>0)
            {
                bpm_song+=-1
                tv_songTempo.setText(bpm_song.toInt().toString())
            }
        }

        bt_songTempoPlus.setOnClickListener()
        {
            if(bpm_song<1000)
            {
                bpm_song+=1
                tv_songTempo.setText(bpm_song.toInt().toString())
            }
        }

        bt_hideArrows.setOnClickListener()
        {
            if(isArrowsShown)
            {
                bt_songTempoMinus.visibility = View.INVISIBLE
                bt_songTempoPlus.visibility = View.INVISIBLE
                bt_runTempoMinus.visibility = View.INVISIBLE
                bt_runTempoPlus.visibility = View.INVISIBLE
                bt_hideArrows.setText("Show Manual Tempo")
                isArrowsShown=false

            } else {
                bt_songTempoMinus.visibility = View.VISIBLE
                bt_songTempoPlus.visibility = View.VISIBLE
                bt_runTempoMinus.visibility = View.VISIBLE
                bt_runTempoPlus.visibility = View.VISIBLE
                bt_hideArrows.setText("Hide Manual Tempo")
                isArrowsShown=true
            }

        }

        bt_allowSlow.setOnClickListener()
        {
            if(isSlowAllowed)
            {
                isSlowAllowed = false
                bt_allowSlow.setText("All Speeds")
            } else {
                isSlowAllowed = true
                bt_allowSlow.setText("Only Speed Up")
            }
        }

        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, pos: Int, changed: Boolean) {
                if (changed) {
                    mediaplayer.seekTo(pos)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        runnable = Runnable {
            seek_bar.progress = mediaplayer.currentPosition
            player_position.setText(milliSecondsToTimer(mediaplayer.currentPosition.toLong()))
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)

        mediaplayer.setOnCompletionListener {
            bt_play.visibility = View.VISIBLE
            bt_pause.visibility = View.GONE
            seek_bar.progress = 0
        }
        /*
        //val ehh = Event(mediaplayer)
        val lol = EventList()
        val ehhh = Induction()
        val audiommm = AudioFile(R.raw.music2)
        val aaa = AudioInputStream(audiommm.getMonoStream(42000))
        val dispatcher = AudioDispatcher(aaa,2048,0)*/

        //val externalStorage = Environment.getExternalStorageDirectory()
        /*val mp3 = File("C:/Users/Ted/Desktop/02 - Dirtmouth.mp3")

        val aaa = AndroidAudioInputStream()
        val adp2 = AudioDispatcherFactory.fromPipe()
        (mp3,44100,5000,2500)
        val adp = AudioDispatcherFactory.fromPipe(R.raw.music2, 44100, 5000,2500)*/
        //var beats: ArrayList<Double>
        //val beats: MutableList<Double>
        //val onsets: List<Double>

        // TU WYCIETE
        bt_forw.setOnClickListener()
        {

        }
        bt_rew.setOnClickListener()
        {
            println("possibly start")

            val findFFMPEG = AndroidFFMPEGLocator(this)

            //val audioFileMusic = R.raw.music2
            //val audioFileMusic = File("C:/Users/Ted/Desktop/beatRunnerTestMusic/Kwiat Jabłoni - Mogło być nic.mp3")
            //val audioFileMusic = getResources().getIdentifier("music2.mp3","raw", getPackageName());
            //val audioFileMusic = File(Uri.parse("android.resource://com.my.package/raw/music3.wav").toString())
            val audioFileMusic = File(selectedFile.toString())
            val adp: AudioDispatcher
            adp = AudioDispatcherFactory.fromPipe(audioFileMusic.absolutePath, 44100, 5000, 2500)
            //adp = AudioDispatcherFactory.fromPipe(audioFileMusic.absolutePath, 44100, 5000, 2500)
            //adp = AudioDispatcherFactory.fromFile(audioFileMusic.absolutePath, 44100, 5000, 2500)

            val sampleRate = adp.format.sampleRate
            val lag = 256/44100 /2.0
            val detector2 = ComplexOnsetDetector(256)
            val broeh = BeatRootOnsetEventHandler()
            adp.addAudioProcessor(detector2)

            adp.addAudioProcessor(object : AudioProcessor {
                override fun processingFinished() {
                    broeh.trackBeats(object : OnsetHandler {
                        //						@Override
                        override fun handleOnset(time: Double, salience: Double) {
                            if (beats != null) {
                                beats.add(time - lag)
                                println("aa")
                            } else {
                                println("aaAa")
                            }

                        }
                    })
                }

                override fun process(audioEvent: AudioEvent?): Boolean {
                    println("aaAaAAaaA")
                    return true
                }
            })
            detector2.setHandler(object : OnsetHandler {
                //				@Override
                override fun handleOnset(time: Double, salience: Double) {
                    onsets!!.add(time - lag)
                    broeh.handleOnset(time - lag, salience)
                    println("aaAaAAaaAH")
                }
            })
            Thread(adp).start()
            println("A tutaj niby skonczone hehe")
            println(beats)
            Toast.makeText(this, beats.toString(), Toast.LENGTH_SHORT).show()

        }
        //TU SKONCZYLEM!!!!!!! TUTAJ PATRZ TUTAJ TUTAJ NIE DZIALA BIBLIOTEKI LADOWANIE

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            selectedFile = data?.data!! //The uri with the location of the file
            val extensionOfFile = selectedFile?.let { getMimeType(this, it) }

            //Toast.makeText(this, selectedFile?.getPath(), Toast.LENGTH_SHORT).show()
            //Toast.makeText(this, extensionOfFile, Toast.LENGTH_SHORT).show()
            //Toast.makeText(this, selectedFile?.let { getMimeType(this, it) }, Toast.LENGTH_SHORT).show()
             if(extensionOfFile=="mp3" || extensionOfFile=="wav" || extensionOfFile=="flac" || extensionOfFile=="wmv"|| extensionOfFile=="mp4") {
                 mediaplayer.pause()
                 bt_play.visibility = View.VISIBLE
                 bt_pause.visibility = View.GONE
                 mediaplayer = MediaPlayer.create(this, selectedFile)

                 seek_bar.progress = 0
                 seek_bar.max = mediaplayer.duration
                 //println(milliSecondsToTimer(mediaplayer.getDuration().toLong()))
                 player_duration.setText(milliSecondsToTimer(mediaplayer.getDuration().toLong()))
             } else {
                 Toast.makeText(this, "File with wrong extension!", Toast.LENGTH_SHORT).show()
             }
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        val extension: String?

        //Check uri format to avoid null
        extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
        return extension
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        //val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MOTION_DETECT)

        if (stepsSensor == null) {

            val stepsSensor2 = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (stepsSensor2 != null) {
                Toast.makeText(this, "Lets Try accel !", Toast.LENGTH_SHORT).show()
                //sensorManager?.registerListener(this, stepsSensor2, SensorManager.SENSOR_DELAY_UI)
            } else {
                Toast.makeText(this, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
            }
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
            Toast.makeText(this, "Finaly done with sensor!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (running) {
            if(!isTempoButtonClicked_stepTempo)
            {
                isTempoButtonClicked_stepTempo = true
                timerStepTempo.start()
            } else {
                if(!is_1_ClickFull_stepTempo)
                {
                    is_1_ClickFull_stepTempo=true
                    click1_stepTempo = timerTemp_stepTempo
                } else if(!is_2_ClickFull_stepTempo) {
                    is_2_ClickFull_stepTempo=true
                    click2_stepTempo = timerTemp_stepTempo
                } else if(!is_3_ClickFull_stepTempo) {
                    is_3_ClickFull_stepTempo=true
                    click3_stepTempo = timerTemp_stepTempo
                } else if(!is_4_ClickFull_stepTempo) {
                    is_4_ClickFull_stepTempo=true
                    click4_stepTempo = timerTemp_stepTempo
                    bpm_stepTempo = 60000 / (((click1_stepTempo-click2_stepTempo)+(click2_stepTempo-click3_stepTempo)+(click3_stepTempo-click4_stepTempo))/3)
                    tv_bpm.setText((bpm_stepTempo.toInt()).toString())
                } else if(!is_5_ClickFull_stepTempo) {
                    is_5_ClickFull_stepTempo=true
                    click5_stepTempo = timerTemp_stepTempo
                    bpm_stepTempo = 60000 / (((click1_stepTempo-click2_stepTempo)+(click2_stepTempo-click3_stepTempo)+(click3_stepTempo-click4_stepTempo)+(click4_stepTempo-click5_stepTempo))/4)
                    tv_bpm.setText((bpm_stepTempo.toInt()).toString())
                } else {
                    click1_stepTempo = click2_stepTempo
                    click2_stepTempo = click3_stepTempo
                    click3_stepTempo = click4_stepTempo
                    click4_stepTempo = click5_stepTempo
                    click5_stepTempo = timerTemp_stepTempo
                    bpm_stepTempo = 60000 / (((click1_stepTempo-click2_stepTempo)+(click2_stepTempo-click3_stepTempo)+(click3_stepTempo-click4_stepTempo)+(click4_stepTempo-click5_stepTempo))/4)
                    tv_bpm.setText((bpm_stepTempo.toInt()).toString())
                }
            }
            stepValueInt += 1
            tv_stepsValue.setText(stepValueInt.toString())
            println(stepValueInt)

        }
    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        fun innerCheck(name: String){
            if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        when(requestCode) {
            ACTIVITY_PERMISION_RQ -> innerCheck("activity")
            MEDIA_PERMISION_RQ -> innerCheck("media")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission required")
            setPositiveButton("Ok") { dialog, which ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun milliSecondsToTimer(milliseconds: Long): String? {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }








}