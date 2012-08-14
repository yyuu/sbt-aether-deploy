package org.sonatype.aether.connector.async

import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.spi.locator.{ServiceLocator, Service}
import org.sonatype.aether.spi.log.{NullLogger, Logger}
import org.sonatype.aether.spi.io.FileProcessor
import org.codehaus.plexus.component.annotations.Requirement
import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
class AsyncRepositoryConnectorFactory2(_logger: Logger, _fileProcessor: FileProcessor) extends RepositoryConnectorFactory with Service{
  @Requirement var logger: Logger = _logger
  @Requirement var fileProcessor: FileProcessor = _fileProcessor

  @scala.reflect.BeanProperty
  var priority: Int = 100

  def this() {
    this(NullLogger.INSTANCE, null)
  }

  def initService(locator: ServiceLocator) {
    logger = locator.getService(classOf[Logger])
    fileProcessor = locator.getService(classOf[FileProcessor])
  }

  def newInstance(session: RepositorySystemSession, repository: RemoteRepository) =
    new AsyncRepositoryConnectorDelegateHack(repository, session, fileProcessor, logger)
}
