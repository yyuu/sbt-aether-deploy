package aether.connector

import org.sonatype.aether.RepositorySystemSession
import org.sonatype.aether.repository.RemoteRepository
import aether.layout.SbtPluginLayout
import org.sonatype.aether.spi.connector.{RepositoryConnector, RepositoryConnectorFactory}
import scala.util.control.Exception.allCatch
import java.lang.reflect.Field
import org.sonatype.aether.spi.locator.{ServiceLocator, Service}

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
class SbtPluginRepositoryConnectorFactory(delegate: RepositoryConnectorFactory with Service) extends RepositoryConnectorFactory with Service {

  def newInstance(session: RepositorySystemSession, repository: RemoteRepository) = {
    println("DELEGATE" + delegate.getClass)
    if ("sbt-plugin".equals(repository.getContentType)) {
      val instance = delegate.newInstance(session, new RemoteRepository(repository).setContentType("default"))
      val field = getLayoutField(instance)
      field.foreach {
        f =>
          f.setAccessible(true)
          f.set(instance, new SbtPluginLayout)
      }
      instance
    }
    else {
      delegate.newInstance(session, repository)
    }
  }

  def initService(locator: ServiceLocator) {
    delegate.initService(locator)
  }

  def getPriority = delegate.getPriority

  def getLayoutField(instance: RepositoryConnector): Option[Field] = {
    val clazz = instance.getClass
    allCatch.opt(Option(clazz.getDeclaredField("layout"))).
      orElse(allCatch.opt(Option(clazz.getField("layout")))).flatMap(identity)
  }
}
