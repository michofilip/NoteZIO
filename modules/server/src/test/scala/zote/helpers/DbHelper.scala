package zote.helpers

import io.getquill.*
import zio.*
import zote.Ids.{LabelId, NoteId, PersonId}
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.includes.given

case class DbHelper(
    private val quillContext: QuillContext,
) {

  import quillContext.*

  def insertPerson(personEntity: PersonEntity): Task[PersonEntity] =
    transaction {
      run(insertPersonQuery(lift(personEntity)))
        .map(_.head)
        .map(id => personEntity.copy(id = PersonId(id)))
    }

  def insertLabel(labelEntity: LabelEntity): Task[LabelEntity] =
    transaction {
      run(insertLabelQuery(lift(labelEntity)))
        .map(_.head)
        .map(id => labelEntity.copy(id = LabelId(id)))
    }

  def insertNote(noteEntity: NoteEntity): Task[NoteEntity] =
    transaction {
      run(insertNoteQuery(lift(noteEntity)))
        .map(_.head)
        .map(id => noteEntity.copy(id = NoteId(id)))
    }

  def insertNoteLabel(
      noteLabelEntity: NoteLabelEntity,
  ): Task[NoteLabelEntity] = transaction {
    run(insertNoteLabelQuery(lift(noteLabelEntity)))
      .as(noteLabelEntity)
  }

  def insertNotePerson(
      notePersonEntity: NotePersonEntity,
  ): Task[NotePersonEntity] = transaction {
    run(insertNotePersonQuery(lift(notePersonEntity)))
      .as(notePersonEntity)
  }

  private inline def insertPersonQuery = quote { (personEntity: PersonEntity) =>
    sql"""
      SELECT ID
      FROM FINAL TABLE (
        INSERT INTO person(name)
        VALUES (${personEntity.name})
    ) AUTHOR
    """
      .as[Query[Long]]
  }

  private inline def insertLabelQuery = quote { (labelEntity: LabelEntity) =>
    sql"""
      SELECT ID
      FROM FINAL TABLE (
        INSERT INTO label(name)
        VALUES (${labelEntity.name})
      ) AUTHOR
    """
      .as[Query[Long]]
  }

  private inline def insertNoteQuery = quote { (noteEntity: NoteEntity) =>
    sql"""
      SELECT ID
      FROM FINAL TABLE (
        INSERT INTO note(title,message,status,parent_id)
        VALUES (${noteEntity.title},${noteEntity.message},${noteEntity.status},${noteEntity.parentId})
      ) AUTHOR
    """
      .as[Query[Long]]
  }

  private inline def insertNoteLabelQuery = quote { (noteLabelEntity: NoteLabelEntity) =>
    sql"""
      INSERT INTO note_label(note_id,label_id)
      VALUES (${noteLabelEntity.noteId},${noteLabelEntity.labelId})
    """
      .as[Insert[Any]]
  }

  private inline def insertNotePersonQuery = quote { (notePersonEntity: NotePersonEntity) =>
    sql"""
      INSERT INTO note_person(note_id,person_id,role) 
      VALUES (${notePersonEntity.noteId},${notePersonEntity.personId},${notePersonEntity.role})
    """
      .as[Insert[Any]]
  }
}

object DbHelper {
  lazy val layer = ZLayer.derive[DbHelper]

  def insertPerson(personEntity: PersonEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertPerson(personEntity))

  def insertLabel(labelEntity: LabelEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertLabel(labelEntity))

  def insertNote(noteEntity: NoteEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertNote(noteEntity))

  def insertNoteLabel(noteLabelEntity: NoteLabelEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertNoteLabel(noteLabelEntity))

  def insertNotePerson(notePersonEntity: NotePersonEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertNotePerson(notePersonEntity))
}
