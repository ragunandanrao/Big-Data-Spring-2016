/**
  * Created by raghu on 2/25/16.
  */
import java.io.{DataInputStream, PrintStream, IOException}
import java.net.{Socket, InetAddress}

object SocketClient {

  def findIpAdd():String =
  {
    val localhost = InetAddress.getLocalHost
    val localIpAddress = localhost.getHostAddress

    return localIpAddress
  }
  def sendHashTagsToDevice(string: String) : String =
  {
    // Simple server

    try {
      //Array(192.toByte, 168.toByte, 0.toByte, 14.toByte)
      lazy val address: Array[Byte] = Array(10.toByte, 99.toByte, 1.toByte, 11.toByte)
      val ia = InetAddress.getByAddress(address)
      val socket = new Socket(ia, 1234)
      val out = new PrintStream(socket.getOutputStream)
      //val in = new DataInputStream(socket.getInputStream())
      if(!string.isEmpty()) {
        out.print(string)
        out.flush()
      }

      //out.close()
      //in.close()
      //socket.close()
      return "Success"
    }
    catch {
      case e: IOException =>
        e.printStackTrace()
        return ""
    }
  }
  def receiveFilter() : String =
  {
    // Simple server
var filter ="";
    try {

      //Array(192.toByte, 168.toByte, 0.toByte, 14.toByte)
      lazy val address: Array[Byte] = Array(10.toByte, 99.toByte, 1.toByte, 11.toByte)
      val ia = InetAddress.getByAddress(address)
      val socket = new Socket(ia, 1234)
      val in = new DataInputStream(socket.getInputStream())
      filter = in.readLine();
      //socket.close()
      return filter

    }
    catch {
      case e: IOException =>
       e.printStackTrace()
        return ""
    }


  }

}