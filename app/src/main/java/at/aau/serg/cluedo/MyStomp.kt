import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.cluedo.Callbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

private  val WEBSOCKET_URI = "ws://10.0.2.2:8080/cluedo"; //localhost port 8080

class MyStomp(var callbacks: Callbacks) {

    private lateinit var username: String

    private lateinit var topicFlow: Flow<String>
    private lateinit var collector:Job

    private lateinit var jsonFlow: Flow<String>
    private lateinit var jsonCollector:Job

    private var client:StompClient = StompClient(OkHttpWebSocketClient()) // other config can be passed in here
    private lateinit var session: StompSession

    private val scope:CoroutineScope=CoroutineScope(Dispatchers.IO)

    fun init(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    fun connect() {
            scope.launch {
                session=client.connect(WEBSOCKET_URI)
                Log.e("tag","connected!")

                topicFlow= session.subscribeText("/topic/message-response")     //connect to topic hello-response
                collector=scope.launch {
                    topicFlow.collect{
                        msg->
                        //todo logic
                        Log.e("tag",msg.toString())
                        callback(msg)
                    }
                }

                jsonFlow= session.subscribeText("/topic/rcv-object")     //connect to topic rcv-object
                jsonCollector=scope.launch {
                    jsonFlow.collect{
                        msg->
                        var o=JSONObject(msg)
                        callback(o.get("text").toString())
                    }
                }
                callback("connected")
            }
    }

    fun login(username: String){
        if (::session.isInitialized) {
            scope.launch {
                Log.e("tag", "connecting to topic login")
                session.sendText("/app/login", username)
                // TODO handle login
            }
        }
    }

    fun disconnect(username: String) {
        scope.launch {
            Log.e("tag","connecting to topic disconnect")
            session.sendText("/app/disconnect",username)
            // TODO handle disconnect
            session.disconnect()

        }
    }

    private fun callback(msg:String){
        Handler(Looper.getMainLooper()).post{
            callbacks.onResponse(msg)
        }
    }

    fun sendMessage(message: String) {
        scope.launch {
            Log.e("tag","connecting to topic message")
            session.sendText("/app/message",message)
           }
    }

    fun sendJson(){
        var json=JSONObject();
        json.put("from","client")
        json.put("text","from client")
        var o=json.toString()
        scope.launch {
            Log.e("tag","connecting to topic json")
            session.sendText("/app/object",o);
        }
    }

}