package aether.connector

import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository
import aether.layout.SbtPluginLayout
import org.sonatype.aether.spi.connector.{RepositoryConnector, RepositoryConnectorFactory}
import scala.util.control.Exception.allCatch
import java.lang.reflect.Field

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
trait SbtPluginRepositoryConnectorFactory extends RepositoryConnectorFactory {

  def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = {
    if ("sbt-plugin".equals(repository.getContentType)) {
      val instance = super.newInstance(session, new RemoteRepository(repository).setContentType("default"))
      getLayoutField(instance).foreach {
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


  def getLayoutField(instance: RepositoryConnector): Option[Field] = {
    val clazz = instance.getClass
    allCatch.opt(Option(clazz.getDeclaredField("layout"))).
      orElse(allCatch.opt(Option(clazz.getField("layout")))).flatMap(identity)
  }
}
