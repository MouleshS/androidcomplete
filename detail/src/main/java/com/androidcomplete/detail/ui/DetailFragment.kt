package com.androidcomplete.detail.ui

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidcomplete.detail.R
import kotlinx.android.synthetic.main.detail_fragment.*
import java.nio.charset.Charset
import java.util.*


/**
 * Created by mouleshs on 05,October,2020
 */


/**
 * all bye services:
Index: 0
serviceId: 00001800-0000-1000-8000-00805f9b34fb

Characteristic
00002a00-0000-1000-8000-00805f9b34fb
00002a01-0000-1000-8000-00805f9b34fb
00002a04-0000-1000-8000-00805f9b34fb

——————————————————————————————————————

Index:1
serviceId: 00001801-0000-1000-8000-00805f9b34fb

Characteristic:
00002a05-0000-1000-8000-00805f9b34fb

—————————————————————————————————————-

Index:2
serviceId: 81020100-76df-11e7-b5a5-be2e44b06b34

Char:
81020101-76df-11e7-b5a5-be2e44b06b34
81020102-76df-11e7-b5a5-be2e44b06b34
81020100-76df-11e7-b5a5-be2e44b06b34

——————————————————————————————————————

Index:3

Service: 81020800-76df-11e7-b5a5-be2e44b06b34

Char:
81020801-76df-11e7-b5a5-be2e44b06b34
81020802-76df-11e7-b5a5-be2e44b06b34
81020803-76df-11e7-b5a5-be2e44b06b34
81020804-76df-11e7-b5a5-be2e44b06b34

————————————————————————————————————

Index: 4

Service: 0000180f-0000-1000-8000-00805f9b34fb

Char:
00002a19-0000-1000-8000-00805f9b34fb

———————————————————————————————

Index: 5
Service: 0000180a-0000-1000-8000-00805f9b34fb

Char:
00002a29-0000-1000-8000-00805f9b34fb
00002a24-0000-1000-8000-00805f9b34fb
00002a25-0000-1000-8000-00805f9b34fb
00002a27-0000-1000-8000-00805f9b34fb
00002a26-0000-1000-8000-00805f9b34fb
00002a28-0000-1000-8000-00805f9b34fb


 *
 */

const val ACCESS_LOCATION_REQUEST = 101
const val REQUEST_ENABLE_BT = 102
const val TAG_BLUETOOTH = "BLUETOOTH"
private const val GATT_MAX_MTU_SIZE = 517
private val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID =
    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
private val VITAL_CHARATERISTIC_UUID = UUID.fromString("81020801-76df-11e7-b5a5-be2e44b06b34")
private val BIOSENSOR_SERVICE_UUID = UUID.fromString("81020800-76df-11e7-b5a5-be2e44b06b34")
private val SessionCodeCharId = UUID.fromString("81020804-76df-11e7-b5a5-be2e44b06b34")
private val StreamConfigCharId = UUID.fromString("81020802-76df-11e7-b5a5-be2e44b06b34")


class DetailFragment : Fragment(), BleAdapter.ItemClickContract {
    private val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    private var mScanning = false
    private val handler = Handler(Looper.getMainLooper())
    private var scanResultSet = mutableSetOf<BluetoothDevice>()
    private var bleAdapter: BleAdapter? = null
    var bluetoothGatt: BluetoothGatt? = null
    var gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG_BLUETOOTH, "Connected to GATT server.")
                    Log.i(
                        TAG_BLUETOOTH, "Attempting to start service discovery: " +
                                bluetoothGatt?.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG_BLUETOOTH, "Disconnected from GATT server.")
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    readCharacteristic(gatt, BIOSENSOR_SERVICE_UUID, SessionCodeCharId)
                }
                else -> Log.d(TAG_BLUETOOTH, "onServicesDiscovered received: $status")
            }
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    parseData(characteristic)
                    toastMsg("onCharacteristicRead")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            toastMsg("onCharacteristicChanged")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }
    }

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                Log.d(TAG_BLUETOOTH, "Result Published Now : ${scanResultSet.size}")
                if (scanResultSet.add(it.device)) {
                    Log.d(TAG_BLUETOOTH, "Added New Device, Size Now: ${scanResultSet.size}")
                    bleAdapter?.publish(scanResultSet.toList())
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.detail_fragment, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let { hasPermissions(it) }
        checkBluetoothIsOn()
        context?.let { checkLocationEnabled(it) }

        scanDevice.setOnClickListener {
            scanForBluetoothDevice(null, null, scanCallback)
        }
        initSession.setOnClickListener {
            bluetoothGatt?.let {
                val timeStamp = System.currentTimeMillis()
                val hexTime = java.lang.Long.toHexString(timeStamp / 1000)

                val array = byteArrayOf(
                    0x01,
                    0x01,
                    0x05,
                    0x00,
                    0x60,
                    0x1D,
                    0x12,
                    0x78
//                    0x0D,
//                    0x15,
//                    0x5E,
//                    0x5E,
//                    0x3C,
//                    0x00
                )

                //val timeArray = hexTime.toByteArray(Charset.defaultCharset())
                //val finalArray = array.plus(timeArray)
                writeCharacteristic(
                    it,
                    BIOSENSOR_SERVICE_UUID,
                    StreamConfigCharId,
                    array
                )

            }
        }
        readVitals.setOnClickListener {
            bluetoothGatt?.let {
                setCharacteristicNotification(
                    it,
                    BIOSENSOR_SERVICE_UUID,
                    VITAL_CHARATERISTIC_UUID,
                    true
                )
            }

        }

        readStatus.setOnClickListener {
            bluetoothGatt?.let {
                readCharacteristic(it, BIOSENSOR_SERVICE_UUID, SessionCodeCharId)

            }
        }

        setUpRecycler()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACCESS_LOCATION_REQUEST -> {
                val x = 5
            }
        }
    }


    private fun hasPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context.applicationContext
                    .checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && context.applicationContext
                    .checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    ACCESS_LOCATION_REQUEST
                )
            } else if (context.applicationContext
                    .checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    ACCESS_LOCATION_REQUEST
                )
            }

            return false
        }
        return true
    }

    private fun checkBluetoothIsOn() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.let {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

    }

    private fun scanForBluetoothDevice(
        filters: List<ScanFilter>?,
        scanSettings: ScanSettings?,
        callback: ScanCallback
    ) {
        if (!mScanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner.stopScan(callback)
            }, SCAN_PERIOD)
            mScanning = true
            bluetoothLeScanner.startScan(callback)
        } else {
            mScanning = false
            bluetoothLeScanner.stopScan(callback)
        }
    }

    fun checkLocationEnabled(context: Context) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(lm)) {
            // Start Location Settings Activity, you should explain to the user why he need to enable location before.
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun setUpRecycler() {
        bleAdapter = BleAdapter(listOf(), this)
        bleRecycler.adapter = bleAdapter
        bleRecycler.layoutManager = LinearLayoutManager(context)
    }

    override fun itemClicked(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private fun readBatteryLevel(gatt: BluetoothGatt) {
        val batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        val batteryLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        val batteryLevelChar = gatt.getService(batteryServiceUuid)
            ?.getCharacteristic(batteryLevelCharUuid)
        gatt.readCharacteristic(batteryLevelChar)

    }

    fun parseData(characteristic: BluetoothGattCharacteristic) {
        when (characteristic.uuid) {
            SessionCodeCharId -> {

                when (characteristic.value.getOrNull(0)?.toInt()) {
                    0 -> toastMsg("Session is Uninitialised")
                    1 -> toastMsg("Session Configured")
                    2 -> toastMsg("Session Active")
                    3 -> toastMsg("Session Config Error")

                }
            }
        }
        toastMsg("Hex Value " + characteristic.value.toHexString())
    }

    fun toastMsg(msg: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()

        }
    }

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

    fun setCharacteristicNotification(
        gatt: BluetoothGatt,
        serviceUuid: UUID?,
        characteristicUuid: UUID,
        enable: Boolean
    ): Boolean {
        //I just hold the gatt instances I got from connect in this HashMap
        val characteristic =
            gatt.getService(serviceUuid).getCharacteristic(characteristicUuid)
        gatt.setCharacteristicNotification(characteristic, enable)
        val descriptor =
            characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)
        descriptor.value =
            if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else byteArrayOf(
                0x00,
                0x00
            )
        return gatt.writeDescriptor(descriptor) //descriptor write operation successfully started?
    }


    fun readCharacteristic(
        gatt: BluetoothGatt,
        serviceId: UUID,
        charId: UUID
    ) {
        val characteristic = gatt.getService(serviceId)?.getCharacteristic(charId)
        gatt.readCharacteristic(characteristic)
    }

    fun writeCharacteristic(
        gatt: BluetoothGatt,
        serviceId: UUID,
        charId: UUID,
        opCode: ByteArray
    ) {
        val characteristic = gatt.getService(serviceId)?.getCharacteristic(charId)
        val mWriteType = if (characteristic?.properties?.and(PROPERTY_WRITE_NO_RESPONSE) !== 0) {
            WRITE_TYPE_NO_RESPONSE
        } else {
            WRITE_TYPE_DEFAULT
        }
        val updatedCharacteristic = characteristic
        //val hexo = "01010500".toByteArray(Charsets.UTF_8)
        opCode.forEachIndexed { i,_ ->
            updatedCharacteristic?.value = opCode
            updatedCharacteristic?.writeType = mWriteType

            if (gatt.writeCharacteristic(updatedCharacteristic)) {
                toastMsg("Property Written $i")
            }
            Thread.sleep(10)
        }

    }

    fun Int.toByteArray() = byteArrayOf(
        this.toByte(),
        (this ushr 8).toByte(),
        (this ushr 16).toByte(),
        (this ushr 24).toByte()
    )
}