package eu.stratosphere.peel.core.config

import java.util
import java.util.HashMap

import com.typesafe.config.{Config, ConfigObject}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

trait Model {

  case class Pair(name: String, value: Any) {}

}

object Model {

  class Site(val c: Config, val prefix: String) extends Model {

    val properties = {

      val buffer = ListBuffer[Pair]()

      def collect(c: Config): Unit = {
        for (e <- c.entrySet().asScala) e.getValue match {
          case c: Config => collect(c)
          case _ => buffer += Pair(e.getKey.stripPrefix(s"$prefix.").stripSuffix(".\"_root_\""), c.getString(e.getKey))
        }
      }

      collect(c.withOnlyPath(prefix))

      buffer.toList.sortBy(x => x.name).asJava
    }
  }

  class Env(val c: Config, val prefix: String) extends java.util.HashMap[String, String] with Model {

    // constructor
    {
      def collect(c: Config): Unit = {
        for (e <- c.entrySet().asScala) e.getValue match {
          case c: Config => collect(c)
          case _ => this.put(e.getKey.stripPrefix(s"$prefix.").stripSuffix(".\"_root_\""), c.getString(e.getKey))
        }
      }

      collect(c.withOnlyPath(prefix))
    }
  }

  class Yaml(val c: Config, val prefix: String) extends util.HashMap[String, Object] with Model {

    // constructor
    {
      def collect(c: ConfigObject, m: util.HashMap[String, Object]): Unit = {
        val keys = (for (x <- c.entrySet().asScala) yield x.getKey.split('.').head).toSet
        for (k <- keys) c.get(k) match {
          case v: ConfigObject =>
            val child = new util.HashMap[String, Object]
            m.put(k, child)
            collect(v, child)
          case _ =>
            m.put(k, c.get(k).unwrapped())
        }
      }

      collect(c.getConfig(prefix).root(), this)
      Unit
    }
  }

  class Hosts(val c: Config, val key: String) extends Model {

    val hosts = c.getStringList(key)
  }

  def factory[T <: Model](config: Config, prefix: String)(implicit m: Manifest[T]) = {
    val constructor = m.runtimeClass.getConstructor(classOf[Config], classOf[String])
    constructor.newInstance(config, prefix)
  }
}
