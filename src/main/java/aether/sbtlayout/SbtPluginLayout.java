package aether.sbtlayout;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.util.layout.RepositoryLayout;

import java.net.URI;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public class SbtPluginLayout implements RepositoryLayout {

    public static final String SBT_VERSION = "sbt-version";
    public static final String SCALA_VERSION = "scala-version";

    @Override
    public URI getPath(Artifact artifact) {
        String sbtVersion = artifact.getProperty(SBT_VERSION, "0.12");
        String scalaVersion = artifact.getProperty(SCALA_VERSION, "2.9.2");

        StringBuilder path = new StringBuilder(128);

        path.append(artifact.getGroupId().replace('.', '/')).append('/');

        path.append(artifact.getArtifactId()).append('_').append(scalaVersion).append('_').append(sbtVersion).append('/');

        path.append(artifact.getBaseVersion()).append('/');

        path.append(artifact.getArtifactId()).append('-').append(artifact.getVersion());

        if (artifact.getExtension().length() > 0) {
            path.append('.').append(artifact.getExtension());
        }

        return URI.create(path.toString());
    }

    @Override
    public URI getPath(Metadata metadata) {
        StringBuilder path = new StringBuilder(128);

        if (!metadata.getGroupId().isEmpty()) {
            path.append(metadata.getGroupId().replace('.', '/')).append('/');

            if (!metadata.getArtifactId().isEmpty()) {
                path.append(metadata.getArtifactId()).append('/');

                if (!metadata.getVersion().isEmpty()) {
                    path.append(metadata.getVersion()).append('/');
                }
            }
        }

        path.append(metadata.getType());

        return URI.create(path.toString());

    }
}
