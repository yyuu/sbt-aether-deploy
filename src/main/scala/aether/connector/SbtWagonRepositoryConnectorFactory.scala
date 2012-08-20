package aether.connector

import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory
import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
class SbtWagonRepositoryConnectorFactory extends WagonRepositoryConnectorFactory with SbtPluginRepositoryConnectorFactory {
  override def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = {
    super.newInstance(session, repository)
  }
}
