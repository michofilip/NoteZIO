package zote.enums

import sttp.tapir.Schema
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

enum MessageType {
  case Debug
  case Error
  case Info
  case Warning
}

object MessageType {

  given JsonCodec[MessageType] = JsonCodec[MessageType](
    JsonEncoder[String].contramap(_.toString),
    JsonDecoder[String].map(MessageType.valueOf),
  )

  given Schema[MessageType] = Schema.derivedEnumeration.defaultStringBased
}
