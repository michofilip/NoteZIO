package zote.dto.form

import zio.json.JsonCodec
import zote.Ids.PersonId
import zote.enums.NotePersonRole

case class NotePersonForm(
    personId: PersonId,
    role: NotePersonRole,
) derives JsonCodec
