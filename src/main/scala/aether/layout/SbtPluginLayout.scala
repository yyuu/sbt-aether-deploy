package aether.layout

import org.sonatype.aether.util.layout.RepositoryLayout
import org.sonatype.aether.metadata.Metadata
import org.sonatype.aether.artifact.Artifact
import java.net.URI

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
class SbtPluginLayout extends RepositoryLayout {
  import aether.MavenCoordinates._
  def getPath(artifact: Artifact) = {
    val sbtVersion = artifact.getProperty(SbtVersion, DefaultSbtVersion)
    val scalaVersion = artifact.getProperty(ScalaVersion, DefaultScalaVersion)
    val path = new StringBuilder(128)
    path.append(artifact.getGroupId.replace('.', '/')).append('/')
    path.append(artifact.getArtifactId).append('_').append(scalaVersion).append('_').append(sbtVersion).append('/')
    path.append(artifact.getBaseVersion).append('/')
    path.append(artifact.getArtifactId).append('-').append(artifact.getVersion)
    if (artifact.getExtension.length > 0) {
      path.append('.').append(artifact.getExtension)
    }
    URI.create(path.toString())
  }

  def getPath(metadata: Metadata) = {
    sys.error("We do not generate metadata here.")
  }
}
