package aether

import connector.SbtPluginRepositoryConnectorFactory
import java.io.File
import sbt.std.TaskStreams
import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import org.sonatype.aether.connector.wagon.{WagonRepositoryConnectorFactory, PlexusWagonConfigurator, WagonConfigurator, WagonProvider}
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.impl.MetadataGeneratorFactory
import org.sonatype.aether.repository.{RemoteRepository, Authentication, Proxy => AProxy, LocalRepository}
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.transfer.NoRepositoryConnectorException
import org.sonatype.aether.util.repository.DefaultProxySelector
import org.apache.maven.repository.internal.{MavenServiceLocator, MavenRepositorySystemSession}
import org.apache.maven.wagon.Wagon
import util.Properties
import java.net.URI

object Booter {
  def newRepositorySystem(plugin: Boolean, wagons: Seq[WagonWrapper]) = {
    val locator = new MavenServiceLocator()
    val connectors = List(
      new SbtPluginRepositoryConnectorFactory(new AsyncRepositoryConnectorFactory()) {
        override def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = {
          if (Option(repository.getProtocol).filter(_.contains("dav")).isDefined) {
            throw new NoRepositoryConnectorException(repository, "Dav not supported")
          }
          super.newInstance(session, repository)
        }
      },
      new SbtPluginRepositoryConnectorFactory(new WagonRepositoryConnectorFactory()),
      new SbtPluginRepositoryConnectorFactory(new FileRepositoryConnectorFactory())
    )
    locator.setServices(classOf[WagonProvider], new ExtraWagonProvider(wagons))
    locator.setService(classOf[WagonConfigurator], classOf[PlexusWagonConfigurator])
    locator.setServices(classOf[RepositoryConnectorFactory], connectors: _*)
    connectors.foreach(_.initService(locator))
    if (plugin) {
      locator.setServices(classOf[MetadataGeneratorFactory])
    }
    locator.getService(classOf[RepositorySystem])
  }

  def newSession(implicit system: RepositorySystem, localRepoDir: File, streams: TaskStreams[_]): RepositorySystemSession = {
    val localRepo = new LocalRepository(localRepoDir)
    val session = new MavenRepositorySystemSession()
    session.setProxySelector(SystemPropertyProxySelector)
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo))
    session.setTransferListener(new ConsoleTransferListener(streams.log))
    session.setRepositoryListener(new ConsoleRepositoryListener(streams.log))
    session
  }


  /*
   *  http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
   */
  private object SystemPropertyProxySelector extends DefaultProxySelector {
    loadProxies().foreach( p => {
      add(p, Properties.envOrNone("http.nonProxyHosts").orNull)
    })

    /**
     * java -Dhttp.proxyHost=myproxy -Dhttp.proxyPort=8080 -Dhttp.proxyUser=username -Dhttp.proxyPassword=mypassword
     * @return
     */
    private def loadProxies(): Option[AProxy] = {
      val env = Properties.envOrNone("http_proxy").map(URI.create(_)).map(uri => {
        val port = uri.getScheme -> uri.getPort match {
          case ("http", -1) => 80
          case ("https", -1) => 443
          case (_, p) => p
        }
        new AProxy(uri.getScheme, uri.getHost, port, null)
      })
      val http = Properties.propOrNone("http.proxyHost").map(host => new AProxy("http", host, Properties.propOrElse("http.proxyPort", "80").toInt, null))
      val https = Properties.propOrNone("https.proxyHost").map(host => new AProxy("https", host, Properties.propOrElse("https.proxyPort", "443").toInt, null))

      val auth = Properties.propOrNone("http.proxyUser") -> Properties.propOrNone("http.proxyPassword") match {
        case (Some(u), Some(p)) => new Authentication(u, p)
        case _ => null
      }

      env.orElse(http).orElse(https).map(u => u.setAuthentication(auth))
    }

  }

  private class ExtraWagonProvider(wagons: Seq[WagonWrapper]) extends WagonProvider {
    private val map = wagons.map(w => w.scheme -> w.wagon).toMap

    def lookup(roleHint: String): Wagon = {
      map.get(roleHint).getOrElse(throw new IllegalArgumentException("Unknown wagon type"))
    }

    def release(wagon: Wagon) {
      try {
        if (wagon != null) wagon.disconnect()
      }
      catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

}
