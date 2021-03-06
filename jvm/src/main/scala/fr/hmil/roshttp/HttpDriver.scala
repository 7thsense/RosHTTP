package fr.hmil.roshttp

import java.net.{HttpURLConnection, URL}
import java.nio.ByteBuffer

import fr.hmil.roshttp.tools.io.IO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}

private object HttpDriver extends DriverTrait {

  def send(req: HttpRequest): Future[HttpResponse] = {

    concurrent.Future {
      try {
        blocking {
          val connection = prepareConnection(req)
          readResponse(connection)
        }
      } catch {
        case e: HttpResponseError => throw e
        case e: Throwable => throw new HttpNetworkError(e)
      }
    }
  }

  private def prepareConnection(req: HttpRequest): HttpURLConnection = {
    val connection = new URL(req.url).openConnection().asInstanceOf[HttpURLConnection]
    req.headers.foreach(t => connection.addRequestProperty(t._1, t._2))
    connection.setRequestMethod(req.method.toString)
    req.body.foreach({part =>
      connection.setDoOutput(true)
      val os = connection.getOutputStream
      os.write(part.content.array())
      os.close()
    })
    connection
  }

  private def readResponse(connection: HttpURLConnection): HttpResponse = {
    val code = connection.getResponseCode
    val headerMap = HeaderMap(Iterator.from(0)
      .map(i => (i, connection.getHeaderField(i)))
      .takeWhile(_._2 != null)
      .flatMap({ t =>
        connection.getHeaderFieldKey(t._1) match {
          case null => None
          case key => Some((key, t._2.mkString.trim))
        }
      }).toMap[String, String])
    val charset = HttpUtils.charsetFromContentType(headerMap.getOrElse("content-type", null))

    if (code < 400) {
      new HttpResponse(
        code,
        ByteBuffer.wrap(IO.readInputStreamToByteArray(connection.getInputStream)),
        headerMap
      )
    } else {
      throw HttpResponseError.badStatus(new HttpResponse(
        code,
        ByteBuffer.wrap(Option(connection.getErrorStream)
          .map(IO.readInputStreamToByteArray)
          .getOrElse(Array.empty)),
        headerMap
      ))
    }
  }
}
