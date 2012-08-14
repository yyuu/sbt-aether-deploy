package aether

import org.sonatype.aether.repository.LocalRepository
import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import java.io.File
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.apache.maven.wagon.Wagon
import org.sonatype.aether.connector.wagon.{WagonRepositoryConnectorFactory, WagonProvider}
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.apache.maven.repository.internal.{MavenServiceLocator, MavenRepositorySystemSession}
import sbt.std.TaskStreams
import org.apache.maven.wagon.providers.ssh.jsch.{SftpWagon, ScpWagon}
import org.apache.maven.wagon.providers.ftp.FtpWagon
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory2
import org.sonatype.aether.impl.MetadataGeneratorFactory

object Booter {
  def newRepositorySystem(plugin: Boolean) = {
    val locator = new MavenServiceLocator()
    locator.addService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[AsyncRepositoryConnectorFactory2])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[WagonRepositoryConnectorFactory])
    locator.setServices(classOf[WagonProvider], ManualWagonProvider)
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

  //TODO: Separate this into its own plugin.
  object ManualWagonProvider extends WagonProvider {
    def lookup(roleHint: String ): Wagon = {
      roleHint match {
        case "scp" => new ScpWagon()
        case "sftp" => new SftpWagon()
        case "ftp" => new FtpWagon()
        case _ => throw new IllegalArgumentException("Unknown wagon type")
      }
    }

    def release(wagon: Wagon){}
  }
}
