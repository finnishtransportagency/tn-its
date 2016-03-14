package fi.liikennevirasto.digiroad2.tnits

import org.scalatra._

class Digiroad2TnItsApi extends Digiroad2TnitsStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

}
