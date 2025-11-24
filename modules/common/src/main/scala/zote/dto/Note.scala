package zote.dto

import zio.json.JsonCodec

case class Note(
    header: NoteHeader,
    message: Option[String],
    assignees: Option[List[NotePerson]],
    parentNote: Option[NoteHeader],
    childrenNotes: Option[List[NoteHeader]],
) derives JsonCodec
