package zote.dto

import sttp.tapir.Schema
import zio.json.JsonCodec
import zote.Ids.PersonId

case class Person(
    id: PersonId,
    name: String,
) derives JsonCodec,
      Schema
