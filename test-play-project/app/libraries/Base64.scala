package libraries

import play.api.libs.json.Json

object Base64 {
  def encodeString(s: String) =
    java.util.Base64.getEncoder.encodeToString(Json.toJson(s).toString.getBytes("UTF-8"))
}
