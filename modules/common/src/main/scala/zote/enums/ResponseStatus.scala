package zote.enums

import sttp.tapir.Schema
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

enum ResponseStatus {
  case Success
  case Failure
}

object ResponseStatus {

  given JsonCodec[ResponseStatus] = JsonCodec[ResponseStatus](
    JsonEncoder[String].contramap(_.toString),
    JsonDecoder[String].map(ResponseStatus.valueOf),
  )

  given Schema[ResponseStatus] = Schema.derivedEnumeration.defaultStringBased
}
