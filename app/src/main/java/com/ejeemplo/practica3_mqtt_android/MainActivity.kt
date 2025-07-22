package com.ejeemplo.practica3_mqtt_android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebaled.ui.theme.PruebaLEDTheme
import kotlinx.coroutines.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*



class MainActivity : ComponentActivity() {
    private lateinit var mqttClient: MqttClient
    private val brokerUri = "tcp://192.168.143.213:1883"
    // ↓↓↓ Comentado: Topic de temperatura
// private val topicTemperatura = "sensor/temperatura"
    private val topicLed1 = "comando/led1"
    private val topicLed2 = "comando/led2"
    private val conectadoGlobal = mutableStateOf(false)
    // ↓↓↓ Comentado: Estado global de temperatura
// private val temperaturaGlobal = mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMqtt()
        setContent {
            PruebaLEDTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFB3E5FC))
                ) {
                    ControlUI()
                }
            }
        }
    }
    private fun initMqtt() {
        val clientId = UUID.randomUUID().toString()
        mqttClient = MqttClient(brokerUri, clientId, MemoryPersistence())
        val options = MqttConnectOptions().apply {
            isCleanSession = true
        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                mqttClient.connect(options)
                conectadoGlobal.value = true
// ↓↓↓ Comentado: suscripción al topic de temperatura
// mqttClient.subscribe(topicTemperatura)
                mqttClient.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        Log.e("MQTT", " Conexión perdida: ${cause?.message}")
                        conectadoGlobal.value = false
                    }
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
// ↓↓↓ Comentado: manejo del mensaje de temperatura
                        /*
                        if (topic == topicTemperatura && message != null) {
                        temperaturaGlobal.value = message.toString()
                        Log.d("MQTT", "🌡️ Temperatura: ${message}")
                        }
                        */
                    }
                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })
                Log.i("MQTT", " Conectado correctamente")
            } catch (e: Exception) {
                Log.e("MQTT", " Error conectando: ${e.message}")
                conectadoGlobal.value = false
            }
        }
    }
    private fun publishMessage(topic: String, message: String) {
        try {
            if (mqttClient.isConnected) {
                val mqttMessage = MqttMessage(message.toByteArray())
                mqttClient.publish(topic, mqttMessage)
                Log.d("MQTT", "Publicado: $topic → $message")
            }
        } catch (e: Exception) {
            Log.e("MQTT", " Error al publicar: ${e.message}")
        }
    }
    @Composable
    fun ControlUI() {
        var led1State by remember { mutableStateOf(false) }
        var led2State by remember { mutableStateOf(false) }
        val conectado by conectadoGlobal
// ↓↓↓ Comentado: lectura de temperatura
// val temperatura by temperaturaGlobal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFB3E5FC))
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Control de LEDs",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LED 1", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color =
                    Color.DarkGray)
                Switch(
                    checked = led1State,
                    onCheckedChange = {
                        led1State = it
                        publishMessage(topicLed1, if (it) "ON" else "OFF")
                    },
                    enabled = conectado,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF3D00),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LED 2", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color =
                    Color.DarkGray)
                Switch(
                    checked = led2State,
                    onCheckedChange = {
                        led2State = it
                        publishMessage(topicLed2, if (it) "ON" else "OFF")
                    },
                    enabled = conectado,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00C853),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
// ↓↓↓ Comentado: visualización de temperatura
            /*
            Spacer(modifier = Modifier.height(30.dp))
            Text(
            text = "Temperatura: ${temperatura ?: "Sin conexión"}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (temperatura != null) Color.Black else Color.Red
            )
            */
            if (!conectado) {
                Text(
                    text = "🔌 No conectado al broker",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}
