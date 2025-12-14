package zote.enums

import sttp.tapir.Schema
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

enum NoteUserRole {
  case Owner
  case Maintainer
  case Observer
}

object NoteUserRole {

  given JsonCodec[NoteUserRole] = JsonCodec[NoteUserRole](
    JsonEncoder[String].contramap(_.toString),
    JsonDecoder[String].map(NoteUserRole.valueOf),
  )

  given Schema[NoteUserRole] = Schema.derivedEnumeration.defaultStringBased
}
