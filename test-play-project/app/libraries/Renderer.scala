package libraries

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}

object Renderer {
  def apply(value: String): String = {
    sanitize(Json.toJson(value).toString)
  }

  def sanitize(value: String): String = {
    value.replaceAll("<", "\\\\u003C")
  }
}
