package fi.liikennevirasto.digiroad2.tnits.aineistot

import java.io.{InputStream, OutputStream}
import java.net.{URLDecoder, URLEncoder, _}
import java.time.Instant

import com.jcraft.jsch._
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID
import org.apache.commons.net.ftp.{FTP, FTPClient}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.language.reflectiveCalls

case class RemoteDatasetsException(cause: Throwable) extends RuntimeException(cause)

/** Accesses the aineistot.liikennevirasto.fi FTP, the ava.liikennevirasto.fi SFTP and HTTP service.
  *
  * @see [[config.urls.aineistot]]
  * @see [[config.logins.aineistot]]
  * @see [[config.urls.aineistotSFTP]]
  * @see [[config.logins.aineistotSFTP]]
  */
object RemoteDatasets {
  private val logins =
    config.logins.aineistot

  private val baseUrl =
    config.urls.aineistot.dir

  /** @return names of all datasets in the configured directory. */
  def index: Seq[String] = {
    val response = createClient.execute(new HttpGet(config.urls.aineistot.dir))
    val contents = EntityUtils.toString(response.getEntity, "UTF-8")
    val doc = Jsoup.parse(contents, baseUrl)
    val links = doc.select("a")
    links.asScala
      .map(_.attr("href"))
      .filter(_.endsWith(".xml"))
      .map(_.dropRight(".xml".length))
      .map(URLDecoder.decode(_, "UTF-8"))
  }

  /** @return the ending timestamp of the latest dataset. */
  def getLatestEndTime: Option[Instant] = {
    val dataSets = try {
      RemoteDatasets.index
    } catch {
      case err: Throwable =>
        throw RemoteDatasetsException(err)
    }

    if (dataSets.nonEmpty) {
      Some(dataSets
        .map { id => DatasetID.decode(URLDecoder.decode(id, "UTF-8")) }
        .map { _.endDate }
        .max)
    } else {
      None
    }
  }

  /** @return a readable stream to a dataset. */
  def get(id: String): InputStream = {
    val get = new HttpGet(dataSetUrl(id))
    val response = createClient.execute(get)
    response.getEntity.getContent
  }

  /** @return a writable stream to a new dataset. */
  def getOutputStream(filename: String): OutputStream = {
    val client = new FTPClient
    client.connect(config.urls.aineistot.ftp)
    client.enterLocalPassiveMode()
    if (!client.login(logins.username, logins.password))
      throw new IllegalArgumentException("Login failed")
    if (!client.changeWorkingDirectory(config.dir))
      throw new IllegalStateException("No such directory: tn-its")
    val files = client.listNames()
    if (files == null)
      throw new IllegalStateException(client.getReplyString)
    if (files.contains(filename))
      throw new IllegalArgumentException(s"$filename already exists on server")
    if (!client.setFileType(FTP.BINARY_FILE_TYPE))
      throw new IllegalStateException(client.getReplyString)

    val output = client.storeFileStream(filename)

    new OutputStream {
      override def write(b: Int): Unit = output.write(b)
      override def write(b: Array[Byte]): Unit = output.write(b)
      override def write(b: Array[Byte], off: Int, len: Int): Unit = output.write(b, off, len)

      override def close(): Unit = {
        output.close()
        if (!client.completePendingCommand())
          throw new IllegalStateException(client.getReplyString)
        if (!client.logout())
          throw new IllegalStateException(client.getReplyString)
        client.disconnect()
      }
    }
  }

  /** @return a writable stream to a new dataset using SFTP transfer process. */
  def getOutputStreamSFTP(fileName: String): OutputStream = {
    val jschClient = new JSch()

    val session = jschClient.getSession(config.logins.aineistotSFTP.username, config.urls.aineistotSFTP.sftp, config.apiPortSFTP)
    session.setPassword(config.logins.aineistotSFTP.password)
    session.setConfig("StrictHostKeyChecking", "no")

    if (!session.isConnected()) {
      try {
        session.connect()
      } catch {
        case jsche: JSchException =>
          throw new IllegalArgumentException("Login failed")
      }
    }

    val channel = session.openChannel("sftp")
    channel.connect()

    val channelSftp = channel.asInstanceOf[ChannelSftp]

    try {
      channelSftp.cd(config.dirSFTP)
    } catch {
      case e: SftpException =>
        throw new IllegalStateException("No such directory: tn-its")
    }

    //verify if file already exist, if not, return a exception and continue, if exist, throw IllegalArgumentException
    if (fileExist(channelSftp, fileName))
      throw new IllegalArgumentException(s"$fileName already exists on server") //when file exist

    //when file doesn't exist
    val output = channelSftp.put(fileName)

    new OutputStream {
      override def write(b: Int): Unit = output.write(b)

      override def write(b: Array[Byte]): Unit = output.write(b)

      override def write(b: Array[Byte], off: Int, len: Int): Unit = output.write(b, off, len)

      override def close(): Unit = {
        output.close()
        channelSftp.exit()
        channel.disconnect()
        session.disconnect()
      }
    }
  }

  private def fileExist(channelSftp: ChannelSftp, fileName: String): Boolean = {
    try {
      channelSftp.ls(fileName)
      true
    } catch {
      case sftpe: SftpException if (sftpe.id == 2) => false
    }
  }

  private def dataSetUrl(id: String): String = {
    // We need to encode the filename twice, because the concrete filename is in URL-encoded form. So we need the
    // URL-encoded encoding of the URL-encoded filename.
    val once = URLEncoder.encode(id, "UTF-8")
    val twice = URLEncoder.encode(once, "UTF-8")
    s"$baseUrl/$twice.xml"
  }

  private def createClient = {
    val credentialsProvider = new BasicCredentialsProvider
    val dataSetsUri = URI.create(config.urls.aineistot.dir)
    credentialsProvider.setCredentials(
      new AuthScope(dataSetsUri.getHost, dataSetsUri.getPort),
      new UsernamePasswordCredentials(config.logins.aineistot.username, config.logins.aineistot.password))

    HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).setMaxConnPerRoute(10).build()
  }
}
