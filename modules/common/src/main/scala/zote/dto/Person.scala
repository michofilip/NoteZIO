package zote.dto

import zio.json.JsonCodec
import zote.Ids.PersonId

case class Person(
    id: PersonId,
    name: String,
) derives JsonCodec
