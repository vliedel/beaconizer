package rocks.crownstone.beaconizer

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.ParcelUuid
import android.support.v4.app.NotificationCompat
import android.util.Log

class CrownstoneService: Service() {
    var TAG = this.javaClass.canonicalName

    lateinit var bleManager: BluetoothManager
    lateinit var bleAdapter: BluetoothAdapter
    lateinit var bleAdvertiser: BluetoothLeAdvertiser
    lateinit var advCallback: AdvertiseCallback

    private var advFrequency: Int? = AdvertiseSettings.ADVERTISE_MODE_BALANCED
    private var advTxPower: Int? = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        bleAdvertiser = bleAdapter.bluetoothLeAdvertiser
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        bleAdvertiser.stopAdvertising(advCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
//        Log.w(TAG, "Don't use start, bind to service instead")
        runInForeground()
        startAdvertising()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return null
    }

    fun toFullUuid(shortUuid: String): String {
        return when (shortUuid.length) {
            4 -> "0000$shortUuid-0000-1000-8000-00805F9B34FB"
            8 -> "$shortUuid-0000-1000-8000-00805F9B34FB"
            else -> "00000000-0000-1000-8000-00805F9B34FB"
        }
    }

    fun runInForeground() {
        Log.d(TAG, "runInForeground")
        val notificationId = 13
        val notificationChannelId = "13"
        val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background)
        val notification = NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("beaconizer")
                .setContentText("running")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        startForeground(notificationId, notification)
    }

    fun startAdvertising() {
        Log.d(TAG, "startAdvertising")
        val advSettings = AdvertiseSettings.Builder().setAdvertiseMode(this.advFrequency!!).
                setConnectable(false).setTxPowerLevel(this.advTxPower!!).build()

        val uuid = toFullUuid("3333")
        val advData = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(ParcelUuid.fromString(uuid))
                .build()

        advCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
                Log.d(TAG, "onStartSuccess")
//                subscriber.onNext(true)
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                Log.d(TAG, "onStartFailure: " + errorCode)
//              subscriber.onNext(false)
            }
        }

        bleAdvertiser.startAdvertising(advSettings, advData, advCallback)
    }
}