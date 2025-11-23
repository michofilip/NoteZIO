package zote

import sttp.tapir.Schema
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

object Ids {
  opaque type NoteId = Long

  object NoteId {
    def apply(value: Long): NoteId = value
    val zero: NoteId               = NoteId(0)

    extension (noteId: NoteId) {
      def value: Long     = noteId
      def isZero: Boolean = noteId == zero
    }

    given JsonCodec[NoteId] = JsonCodec[NoteId](
      JsonEncoder[Long].contramap(_.value),
      JsonDecoder[Long].map(NoteId.apply),
    )

    given Schema[NoteId] = Schema.schemaForLong
  }

  opaque type LabelId = Long

  object LabelId {
    def apply(value: Long): LabelId = value
    val zero: LabelId               = LabelId(0)

    extension (labelId: LabelId) {
      def value: Long     = labelId
      def isZero: Boolean = labelId == zero
    }

    given JsonCodec[LabelId] = JsonCodec[LabelId](
      JsonEncoder[Long].contramap(_.value),
      JsonDecoder[Long].map(NoteId.apply),
    )

    given Schema[LabelId] = Schema.schemaForLong
  }

  opaque type PersonId = Long

  object PersonId {
    def apply(value: Long): PersonId = value
    val zero: PersonId               = PersonId(0)

    extension (personId: PersonId) {
      def value: Long     = personId
      def isZero: Boolean = personId == zero
    }

    given JsonCodec[PersonId] = JsonCodec[PersonId](
      JsonEncoder[Long].contramap(_.value),
      JsonDecoder[Long].map(NoteId.apply),
    )

    given Schema[PersonId] = Schema.schemaForLong
  }
}
