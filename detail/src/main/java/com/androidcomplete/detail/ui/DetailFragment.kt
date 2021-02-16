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
import java.math.BigInteger
import java.util.*
import kotlin.experimental.and


/**
 * Created by mouleshs on 05,October,2020
 */


/**
 * Service: 00001800-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a00-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a01-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a04-0000-1000-8000-00805f9b34fb
Characteristic: 	00002aa6-0000-1000-8000-00805f9b34fb

Service: 00001801-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a05-0000-1000-8000-00805f9b34fb
Service: 8102010	0-76df-11e7-b5a5-be2e44b06b34
Characteristic: 	81020101-76df-11e7-b5a5-be2e44b06b34
Characteristic: 	81020102-76df-11e7-b5a5-be2e44b06b34
Characteristic: 	81020103-76df-11e7-b5a5-be2e44b06b34

Service: 0000fd55-0000-1000-8000-00805f9b34fb
Characteristic: 	81020801-76df-11e7-b5a5-be2e44b06b34
Characteristic: 	81020802-76df-11e7-b5a5-be2e44b06b34
Characteristic: 	81020803-76df-11e7-b5a5-be2e44b06b34
Characteristic: 	81020804-76df-11e7-b5a5-be2e44b06b34

Service: 0000180f-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a19-0000-1000-8000-00805f9b34fb

Service: 0000180a-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a29-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a24-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a25-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a27-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a26-0000-1000-8000-00805f9b34fb
Characteristic: 	00002a28-0000-1000-8000-00805f9b34fb
 */


const val ACCESS_LOCATION_REQUEST = 101
const val REQUEST_ENABLE_BT = 102
const val TAG_BLUETOOTH = "BLUETOOTH"
private const val GATT_MAX_MTU_SIZE = 517
private val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID =
    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
private val VITAL_CHARATERISTIC_UUID = UUID.fromString("81020801-76df-11e7-b5a5-be2e44b06b34")
private val BIOSENSOR_SERVICE_UUID = UUID.fromString("0000fd55-0000-1000-8000-00805f9b34fb")
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
                    setCharacteristicNotification(
                        gatt,
                        BIOSENSOR_SERVICE_UUID,
                        VITAL_CHARATERISTIC_UUID,
                        true
                    )
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

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            characteristic?.let { parseCharacteristic(it) }
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

    override fun onStop() {
        super.onStop()
        val stopPacket = Packet(
            1, 2, 5, 0,
            null, null, null
        ).toByteArray()
        writeCharacteristic(
            bluetoothGatt,
            BIOSENSOR_SERVICE_UUID,
            StreamConfigCharId,
            stopPacket
        )
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
                val seconds = timeStamp / 1000

                val array = byteArrayOf(
                    0x01,
                    0x01,
                    0x05,
                    0x00,
                    0x00,
                    0x39,
                    0x21,
                    0x60,
                    0x0D,
                    0x15,
                    0xEA.toByte(),
                    0x5E,
                    0x3C,
                    0x00
                )

                val packetArray = Packet(
                    1, 1, 5, 0,
                    seconds, charArrayOf('M', 'O', 'U', 'L'), 60
                ).toByteArray()

                //val timeArray = hexTime.toByteArray(Charset.defaultCharset())
                //val finalArray = array.plus(timeArray)
                //val sampleSequence = "010105005FED14800D15EA5E3C00"
                //val byteArray = decodeHexString(sampleSequence)

                writeCharacteristic(
                    it,
                    BIOSENSOR_SERVICE_UUID,
                    StreamConfigCharId,
                    packetArray
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
        gatt: BluetoothGatt?,
        serviceId: UUID,
        charId: UUID
    ) {
        val characteristic = gatt?.getService(serviceId)?.getCharacteristic(charId)
        gatt?.readCharacteristic(characteristic)
    }

    fun writeCharacteristic(
        gatt: BluetoothGatt?,
        serviceId: UUID,
        charId: UUID,
        opCode: ByteArray
    ) {
        val characteristic = gatt?.getService(serviceId)?.getCharacteristic(charId)

        val timeStamp = System.currentTimeMillis()
        characteristic?.value = opCode

//        val packet = Packet(1, 1, 5, 0, (timeStamp / 1000).toInt(), 219540062, 60)
//        characteristic?.write(packet)
        Log.d("written packate", characteristic?.value?.toHexString() ?: "")
        if (characteristic != null && gatt?.writeCharacteristic(characteristic) == true) {
            toastMsg("Property Written")
        }
    }

    fun Int.toByteArray() = byteArrayOf(
        this.toByte(),
        (this ushr 8).toByte(),
        (this ushr 16).toByte(),
        (this ushr 24).toByte()
    )

    fun decodeHexString(hexString: String): ByteArray? {
        require(hexString.length % 2 != 1) { "Invalid hexadecimal String supplied." }
        val bytes = ByteArray(hexString.length / 2)
        var i = 0
        while (i < hexString.length) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2))
            i += 2
        }
        return bytes
    }

    fun hexToByte(hexString: String): Byte {
        val firstDigit: Int = toDigit(hexString[0])
        val secondDigit: Int = toDigit(hexString[1])
        return ((firstDigit shl 4) + secondDigit).toByte()
    }

    private fun toDigit(hexChar: Char): Int {
        val digit = Character.digit(hexChar, 16)
        require(digit != -1) { "Invalid Hexadecimal Character: $hexChar" }
        return digit
    }

    @ExperimentalUnsignedTypes
    fun parseCharacteristic(characteristic: BluetoothGattCharacteristic) {
        when (characteristic.uuid) {
            VITAL_CHARATERISTIC_UUID -> {
                if (characteristic.value.size == 20) {
                    val tokenArray = Arrays.copyOfRange(characteristic.value, 4, 8)
                    val epoch = Arrays.copyOfRange(characteristic.value, 8, 12)
                    val heartRate = Arrays.copyOfRange(characteristic.value, 12, 14).reversedArray()
                    val skinTemp = Arrays.copyOfRange(characteristic.value, 14, 16).reversedArray()
                    val ambientTemp =
                        Arrays.copyOfRange(characteristic.value, 16, 18).reversedArray()
                    val ECG_LEAD_OFF = characteristic.value[2].and(0x01)

                    Log.d(
                        "Property",
                        " \nPacketType: " + characteristic.value[0].toUInt()
                                + " \nSequence Num: " + characteristic.value[1].toUInt()
                                + " \nEvent: " + characteristic.value[2].toUInt()
                                + " \nBattery: " + characteristic.value[3].toUInt()
                                + " \nToken: " + tokenArray.toHexString()
                                + " \nEpoch: " + epoch.toHexString()
                                + " \nHeart_rate: " + BigInteger(heartRate).toInt()
                                + " \nSkin_temp: " + BigInteger(skinTemp).toInt() / 10
                                + " \nAmbient_temp: " + BigInteger(ambientTemp).toInt() / 10
                                + " \nMotion: " + characteristic.value[18].toUInt()
                                + " \nRespiratory rate: " + characteristic.value[19].toUInt()
                                + "\nECG_LEAD_OFF" + ECG_LEAD_OFF
                    )
                }
            }

        }
    }
}

data class Packet(
    val opCode: Byte,
    val sessionAction: Byte,
    val runMode: Byte,
    val reserved: Byte,
    val sessionTime: Long?,
    val token: CharArray?,
    val interval: Int?
) {
    fun toByteArray(): ByteArray {
        var byteArray = byteArrayOf(
            opCode,
            sessionAction,
            runMode,
            reserved
        )
        sessionTime?.let { byteArray = byteArray.plus(longToUInt32ByteArray(sessionTime)) }
        if (token != null && token.size == 4 && interval != null) {
            val tokeArray = ByteArray(4)
            tokeArray[0] = token[0].toByte()
            tokeArray[1] = token[1].toByte()
            tokeArray[2] = token[2].toByte()
            tokeArray[3] = token[3].toByte()
            byteArray = byteArray.plus(tokeArray)
            byteArray = byteArray.plus(intToUInt16ByteArray(interval))
        }
        return byteArray
    }

    private fun longToUInt32ByteArray(value: Long): ByteArray {

        val bytes = ByteArray(4)
        bytes[0] = (value and 0xFFFF).toByte()
        bytes[1] = ((value ushr 8) and 0xFFFF).toByte()
        bytes[2] = ((value ushr 16) and 0xFFFF).toByte()
        bytes[3] = ((value ushr 24) and 0xFFFF).toByte()
        return bytes
    }

    private fun intToUInt16ByteArray(value: Int): ByteArray {
        val bytes = ByteArray(2)
        bytes[1] = ((value ushr 8) and 0xFFFF).toByte()
        bytes[0] = (value and 0xFFFF).toByte()
        return bytes
    }
}
