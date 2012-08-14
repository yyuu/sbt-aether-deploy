package org.sonatype.aether.connector.async

import org.sonatype.aether.spi.connector._
import java.util
import org.sonatype.aether.repository.RemoteRepository
import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.spi.io.FileProcessor
import org.sonatype.aether.RepositorySystemSession
import aether.sbtlayout.SbtPluginLayout

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
class AsyncRepositoryConnectorDelegateHack(repository: RemoteRepository, session: RepositorySystemSession, fileProcessor: FileProcessor, logger: Logger) extends RepositoryConnector {

  val delegate = {
    if ("sbt-plugin".equals(repository.getContentType)) {
      val hack = new RemoteRepository(repository).setContentType("default")
      val del = new AsyncRepositoryConnector(hack, session, fileProcessor, logger)
      val field = scala.util.control.Exception.allCatch.opt(del.getClass.getField("layout"))
      field.foreach{ f =>
        f.setAccessible(true)
        f.set(del, new SbtPluginLayout)
      }
      del
    } else {
      new AsyncRepositoryConnector(repository, session, fileProcessor, logger)
    }

  }

  def get(artifactDownloads: util.Collection[_ <: ArtifactDownload], metadataDownloads: util.Collection[_ <: MetadataDownload]) {
    delegate.get(artifactDownloads, metadataDownloads)
  }

  def put(artifactUploads: util.Collection[_ <: ArtifactUpload], metadataUploads: util.Collection[_ <: MetadataUpload]) {
    delegate.put(artifactUploads, metadataUploads)
  }

  def close() {
    delegate.close()
  }
}
