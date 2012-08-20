package aether.connector

import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
class SbtAsyncRepositoryConnectorFactory extends AsyncRepositoryConnectorFactory with SbtPluginRepositoryConnectorFactory {
  override def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = super.newInstance(session, repository)
}
