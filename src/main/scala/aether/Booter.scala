package aether

import connector.SbtPluginRepositoryConnectorFactory
import org.sonatype.aether.repository.{RemoteRepository, LocalRepository}
import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import java.io.File
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.apache.maven.wagon.Wagon
import org.sonatype.aether.connector.wagon.{WagonRepositoryConnectorFactory, PlexusWagonConfigurator, WagonConfigurator, WagonProvider}
import org.apache.maven.repository.internal.{MavenServiceLocator, MavenRepositorySystemSession}
import sbt.std.TaskStreams
import org.sonatype.aether.impl.MetadataGeneratorFactory
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.transfer.NoRepositoryConnectorException

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
      val session = new MavenRepositorySystemSession()

      val localRepo = new LocalRepository(localRepoDir)
      session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo))
      session.setTransferListener(new ConsoleTransferListener(streams.log))
      session.setRepositoryListener(new ConsoleRepositoryListener(streams.log))
      session
  }

  private class ExtraWagonProvider(wagons: Seq[WagonWrapper]) extends WagonProvider {
    private val map = wagons.map(w => w.scheme -> w.wagon).toMap

    def lookup(roleHint: String ): Wagon = {
      map.get(roleHint).getOrElse(throw new IllegalArgumentException("Unknown wagon type"))
    }

    def release(wagon: Wagon){
      try {
        if (wagon != null) wagon.disconnect()
      }
      catch {
        case e:Exception => e.printStackTrace()
      }
    }
  }
}
