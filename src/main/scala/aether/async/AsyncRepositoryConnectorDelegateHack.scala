package org.sonatype.aether.connector.async

import org.sonatype.aether.spi.connector._
import java.util
import org.sonatype.aether.repository.RemoteRepository
import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.spi.io.FileProcessor
import org.sonatype.aether.RepositorySystemSession
import aether.layout.SbtPluginLayout

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
class AsyncRepositoryConnectorDelegateHack(repository: RemoteRepository, session: RepositorySystemSession, fileProcessor: FileProcessor, logger: Logger) extends RepositoryConnector {

  val delegate = {
    val del = new AsyncRepositoryConnector(new RemoteRepository(repository).setContentType("default"), session, fileProcessor, logger)
    if ("sbt-plugin".equals(repository.getContentType)) {
      scala.util.control.Exception.allCatch.opt(del.getClass.getDeclaredField("layout")).foreach{ f =>
        f.setAccessible(true)
        f.set(del, new SbtPluginLayout)
      }
    }
    del
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
