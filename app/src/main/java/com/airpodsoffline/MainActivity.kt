package com.airpodsoffline

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var list: LinearLayout
    private val adapter by lazy { (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 31 &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), 7)
        }
        draw()
    }

    override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){ super.onRequestPermissionsResult(r,p,g); draw() }

    private fun draw() {
        val root = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; setPadding(42,50,42,30); setBackgroundColor(Color.rgb(246,247,251))
        }
        root.addView(TextView(this).apply { text="AirPods Offline"; textSize=30f; setTypeface(typeface, Typeface.BOLD); setTextColor(Color.rgb(20,25,35)); gravity=Gravity.END })
        root.addView(TextView(this).apply { text="סוללה וחיבור • עובד אופליין • ללא חשבון"; textSize=16f; setTextColor(Color.GRAY); gravity=Gravity.END })
        val refresh=Button(this).apply { text="רענן אוזניות"; textSize=17f; isAllCaps=false; setOnClickListener{ load() } }
        root.addView(refresh, ViewGroup.LayoutParams(-1,-2))
        list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(list, ViewGroup.LayoutParams(-1,-1))
        setContentView(root)
        load()
    }

    private fun load() {
        list.removeAllViews()
        if (adapter == null) { addText("Bluetooth לא זמין במכשיר"); return }
        if (!adapter.isEnabled) { addText("הפעל Bluetooth כדי לראות אוזניות"); return }
        if (android.os.Build.VERSION.SDK_INT >= 31 &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            addText("נדרשת הרשאת Bluetooth"); return
        }
        val devices=adapter.bondedDevices.filter { d ->
            val n=d.name?.lowercase()?:""; n.contains("airpod") || n.contains("buds") || n.contains("ear") || n.contains("head")
        }
        if(devices.isEmpty()) addText("לא נמצאו אוזניות מצומדות")
        devices.forEach { device -> addDevice(device) }
    }

    private fun addDevice(d: BluetoothDevice) {
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(30,28,30,28); setBackgroundColor(Color.WHITE) }
        box.addView(TextView(this).apply { text=d.name ?: "אוזניות Bluetooth"; textSize=21f; gravity=Gravity.END; setTextColor(Color.BLACK) })
        val battery = batteryLevel(d)
        box.addView(TextView(this).apply {
            text= if(battery in 0..100) "סוללה: $battery%" else "סוללה: לא זמין כרגע"
            textSize=17f; gravity=Gravity.END
        })
        list.addView(box, LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,20,0,0) })
    }

    private fun batteryLevel(d: BluetoothDevice): Int {
        return try {
            val m=d.javaClass.getMethod("getBatteryLevel")
            (m.invoke(d) as? Int) ?: -1
        } catch (_:Exception) { -1 }
    }

    private fun addText(s:String){ list.addView(TextView(this).apply { text=s; textSize=18f; gravity=Gravity.CENTER; setPadding(0,80,0,0) }) }
}
