package aether.connector

import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
class SbtFileRepositoryConnectorFactory extends FileRepositoryConnectorFactory with SbtPluginRepositoryConnectorFactory {
  override def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = {
    super.newInstance(session, repository)
  }
}
