package zote.enums

import io.getquill.MappedEncoding
import sttp.tapir.Schema
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

enum NoteStatus {
  case Draft
  case Ongoing
  case Complete
}

object NoteStatus {

  given JsonCodec[NoteStatus] = JsonCodec[NoteStatus](
    JsonEncoder[String].contramap(_.toString),
    JsonDecoder[String].map(NoteStatus.valueOf),
  )

  given MappedEncoding[NoteStatus, String] = MappedEncoding(_.toString)

  given MappedEncoding[String, NoteStatus] = MappedEncoding(NoteStatus.valueOf)

  given Schema[NoteStatus] = Schema.derivedEnumeration.defaultStringBased
}