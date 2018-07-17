package rocks.crownstone.beaconizer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.PermissionChecker
import android.Manifest
import android.app.Notification
import android.bluetooth.le.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout

fun String.toFullUUID(): String {
    return when (this.length) {
        4 -> "0000$this-0000-1000-8000-00805F9B34FB"
        8 -> "$this-0000-1000-8000-00805F9B34FB"
        else -> this
    }
}

class MainActivity : AppCompatActivity() {

    lateinit var bleManager: BluetoothManager
    lateinit var bleAdapter: BluetoothAdapter
    lateinit var bleScanner: BluetoothLeScanner
    lateinit var bleAdvertiser: BluetoothLeAdvertiser

    private var advFrequency: Int? = AdvertiseSettings.ADVERTISE_MODE_BALANCED
    private var advTxPower: Int? = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH

    var crownstoneDevices: ArrayList<CrownstoneDevice> = ArrayList()

    private val crownstoneScanner = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            //Log.d("MainActivity", "onScanResult(): ${result?.device?.address} - ${result?.device?.name}")
            var name = result?.device?.name
            var address = result?.device?.address
            if (name == null) {
                name = "unknown"
            }
            if (address == null) {
                address = "unknown"
            }

            val crownstoneDevicesSameAddress = crownstoneDevices.filter { it.address == address }

            if (crownstoneDevicesSameAddress.isEmpty()) {
                crownstoneDevices.add(CrownstoneDevice(name, address))
                Log.d("MainActivity", "onScanResult(): Add device ${address} (total ${crownstoneDevices.size})")
                val recyclerview = findViewById<RecyclerView>(R.id.device_list)
                recyclerview.adapter.notifyDataSetChanged()
            } else {
//                Log.d("MainActivity", "onScanResult(): Update device ${address}")
                // eventually update the device
            }
        }
    }

    val REQUEST_BLUETOOTH = 0
    val REQUEST_FINE_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate()")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Get bleManager")
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
        bleScanner = bleAdapter.bluetoothLeScanner
        bleAdvertiser = bleAdapter.bluetoothLeAdvertiser

        Log.d("MainActivity", "Get recyclerView")
        val recyclerview = findViewById<RecyclerView>(R.id.device_list)
        recyclerview.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val adapter = CrownstoneDeviceAdapter(crownstoneDevices)
        recyclerview.adapter = adapter

        //crownstoneDevices.add(CrownstoneDevice("BLE device name","some address"))
    }

    override fun onStart() {
        Log.d("MainActivity", "onStart()")
        super.onStart()
        when (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> bleScanner.startScan(crownstoneScanner)
            else -> requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
        }

        // Start service
        Log.d("MainActivity", "startService")
        val intent = Intent(this, CrownstoneService::class.java)

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        }
        else {
            startService(intent)
        }


//        val advSettings = AdvertiseSettings.Builder().setAdvertiseMode(this.advFrequency!!).
//                setConnectable(false).setTxPowerLevel(this.advTxPower!!).build()
//
//        val uuid = "3333".toFullUUID()
//        //val uuid =  UUID.randomUUID().toString()
//        //val uuid = "0000" + "3333" + "-0000-1000-8000-00805F9B34FB"
//        //Log.d("MainActivity", "onStart(): use uuid ${uuid}")
//        val advData = AdvertiseData.Builder().setIncludeDeviceName(true).
//                addServiceUuid(ParcelUuid.fromString(uuid)).build()
//
//        val advCallback = object : AdvertiseCallback() {
//            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
//                super.onStartSuccess(settingsInEffect)
////                subscriber.onNext(true)
//            }
//
//            override fun onStartFailure(errorCode: Int) {
//                super.onStartFailure(errorCode)
//                println("Error: " + errorCode)
//  //              subscriber.onNext(false)
//            }
//        }
//
//        bleAdvertiser.startAdvertising(advSettings, advData, advCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_FINE_LOCATION -> when (grantResults) {
                intArrayOf(PackageManager.PERMISSION_GRANTED) -> {
                    Log.d("MainActivity", "onRequestPermissionsResult(PERMISSION_GRANTED)")
                    bleScanner.startScan(crownstoneScanner)
                }
                else -> {
                    Log.d("MainActivity", "onRequestPermissionsResult(not PERMISSION_GRANTED)")
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStop() {
        Log.d("MainActivity", "Discovered ${crownstoneDevices.size} devices till now")
        Log.d("MainActivity", "onStop()")
        super.onStop()
        bleScanner.stopScan(crownstoneScanner)
    }
}
