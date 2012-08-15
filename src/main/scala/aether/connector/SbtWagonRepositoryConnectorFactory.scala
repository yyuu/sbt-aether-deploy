package aether.connector

import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory
import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository
import aether.layout.SbtPluginLayout

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
class SbtWagonRepositoryConnectorFactory extends WagonRepositoryConnectorFactory {
  override def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = {
    if ("sbt-plugin".equals(repository.getContentType)) {
      val instance = super.newInstance(session, new RemoteRepository(repository).setContentType("default"))
      scala.util.control.Exception.allCatch.opt(Option(instance.getClass.getDeclaredField("layout"))).flatten.foreach {
        f =>
          f.setAccessible(true)
          f.set(instance, new SbtPluginLayout)
      }
      instance
    }
    else {
      super.newInstance(session, repository)
    }
  }
}
