package de.handler.mobile.wifivideo

import android.os.AsyncTask
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WifiP2pConnectionManager(private val listener: OnDataListener) {
	private var receiveDataTask: AsyncTask<Void, Void, Int>? = null
	private var socket: Socket? = null
	private var serverSocket: ServerSocket? = null

	var inetAddress: InetAddress? = null

	interface OnDataListener {
		fun onServerBound(success: Boolean)
		fun onDataSend(data: Int, success: Boolean)
		fun onDataReceived(data: Int)
	}

	fun init(inetAddress: InetAddress?) {
		this.inetAddress = inetAddress
	}

	fun bindServer() {
		if (inetAddress != null) {
			BindServerTask(WeakReference(this), inetAddress!!).execute()
		}
	}

	fun sendData(message: Int) {
		SendDataTask(WeakReference(this)).execute(message)
	}

	fun receiveData(activate: Boolean) {
		if (activate) {
			receiveDataTask = ReceiveDataTask(WeakReference(this), listener).execute()
		} else {
			serverSocket?.close()
			receiveDataTask = null
		}
	}

	class ReceiveDataTask(private val manager: WeakReference<WifiP2pConnectionManager>, private val listener: OnDataListener) : AsyncTask<Void, Void, Int>() {
		override fun doInBackground(vararg p0: Void): Int {
			try {
				if (manager.get()?.serverSocket == null || manager.get()?.serverSocket!!.isClosed) {
					val serverSocket = ServerSocket(8888)
					manager.get()?.serverSocket = serverSocket
				}
				val clientSocket = manager.get()?.serverSocket!!.accept()
				val inputStream = clientSocket.getInputStream()
				val dataInputStream = DataInputStream(inputStream)
				return dataInputStream.readInt()
			} catch (e: IOException) {
				return 0
			}
		}

		override fun onPostExecute(result: Int) {
			listener.onDataReceived(result)
		}
	}

	class SendDataTask(private val manager: WeakReference<WifiP2pConnectionManager>) : AsyncTask<Int, Void, Boolean>() {
		private var message: Int? = null

		override fun doInBackground(vararg p0: Int?): Boolean {
			if (p0[0] == null) {
				return false
			} else {
				message = p0[0]
			}

			try {
				val outputStream = manager.get()?.socket?.getOutputStream() ?: return false
				val dataOutputStream = DataOutputStream(outputStream)
				p0[0]?.let { dataOutputStream.writeInt(it) }
				return true
			} catch (e: IOException) {
				return false
			}
		}

		override fun onPostExecute(result: Boolean?) {
			manager.get()?.listener?.onDataSend(message ?: 0, result ?: false)
		}
	}

	class BindServerTask(private val manager: WeakReference<WifiP2pConnectionManager>, private val inetAddress: InetAddress) : AsyncTask<Void, Void, Boolean>() {
		override fun doInBackground(vararg p0: Void): Boolean {
			try {
				val socketAddress = InetSocketAddress(inetAddress, 8888)
				val socket = Socket()
				socket.bind(socketAddress)
				manager.get()?.socket = socket
				manager.get()?.socket!!.connect(socketAddress, 2000)
				return true
			} catch (e: IOException) {
				Log.e("TAG_BIND_SERVER", e.message)
				return false
			}
		}

		override fun onPostExecute(result: Boolean?) {
			manager.get()?.listener?.onServerBound(result ?: false)
		}
	}
}