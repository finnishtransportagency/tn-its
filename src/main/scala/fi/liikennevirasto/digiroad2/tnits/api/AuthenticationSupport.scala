package fi.liikennevirasto.digiroad2.tnits.api

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import fi.liikennevirasto.digiroad2.tnits.config
import org.scalatra.ScalatraBase
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.auth.strategy.{BasicAuthSupport, BasicAuthStrategy}


case class BasicAuthUser(username: String)

class IntegrationAuthStrategy(protected override val app: ScalatraBase, realm: String)
  extends BasicAuthStrategy[BasicAuthUser](app, realm) {

  private val login = config.logins.converter

  override protected def getUserId(user: BasicAuthUser)(implicit request: HttpServletRequest, response: HttpServletResponse): String =
    user.username

  override protected def validate(userName: String, password: String)(implicit request: HttpServletRequest, response: HttpServletResponse): Option[BasicAuthUser] = {
    if (userName == login.username && password == login.password) Some(BasicAuthUser(userName))
    else None
  }
}

trait AuthenticationSupport extends ScentrySupport[BasicAuthUser] with BasicAuthSupport[BasicAuthUser] {
  self: ScalatraBase =>

  val realm = "Digiroad 2 TN-ITS API"

  protected def fromSession = { case id: String => BasicAuthUser(id)  }
  protected def toSession = { case user: BasicAuthUser => user.username }

  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

  override protected def configureScentry = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    scentry.register("Basic", app => new IntegrationAuthStrategy(app, realm))
  }
}