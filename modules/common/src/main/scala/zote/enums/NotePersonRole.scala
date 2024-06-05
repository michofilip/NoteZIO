package zote.enums

import io.getquill.MappedEncoding
import sttp.tapir.Schema
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

enum NotePersonRole {
  case Owner
  case Maintainer
  case Observer
}

object NotePersonRole {

  given JsonCodec[NotePersonRole] = JsonCodec[NotePersonRole](
    JsonEncoder[String].contramap(_.toString),
    JsonDecoder[String].map(NotePersonRole.valueOf),
  )

  given MappedEncoding[NotePersonRole, String] = MappedEncoding(_.toString)

  given MappedEncoding[String, NotePersonRole] = MappedEncoding(NotePersonRole.valueOf)

  given Schema[NotePersonRole] = Schema.derivedEnumeration.defaultStringBased
}