package zote.helpers

import com.softwaremill.quicklens.modify
import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.includes.given

trait DbHelper {
  def insertPerson(personEntity: PersonEntity): Task[PersonEntity]
  def insertLabel(labelEntity: LabelEntity): Task[LabelEntity]
  def insertNote(noteEntity: NoteEntity): Task[NoteEntity]
  def insertNoteLabel(noteLabelEntity: NoteLabelEntity): Task[NoteLabelEntity]
  def insertNotePerson(
      notePersonEntity: NotePersonEntity
  ): Task[NotePersonEntity]
}

object DbHelper {
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

case class DbHelperImpl(
    private val quillContext: QuillContext
) extends DbHelper {

  import quillContext.*

  override def insertPerson(personEntity: PersonEntity): Task[PersonEntity] =
    transaction {
      run(insertPersonQuery(lift(personEntity)))
        .map(_.head)
        .map(id => personEntity.modify(_.id).setTo(id))
    }

  override def insertLabel(labelEntity: LabelEntity): Task[LabelEntity] =
    transaction {
      run(insertLabelQuery(lift(labelEntity)))
        .map(_.head)
        .map(id => labelEntity.modify(_.id).setTo(id))
    }

  override def insertNote(noteEntity: NoteEntity): Task[NoteEntity] =
    transaction {
      run(insertNoteQuery(lift(noteEntity)))
        .map(_.head)
        .map(id => noteEntity.modify(_.id).setTo(id))
    }

  override def insertNoteLabel(
      noteLabelEntity: NoteLabelEntity
  ): Task[NoteLabelEntity] = transaction {
    run(insertNoteLabelQuery(lift(noteLabelEntity)))
      .as(noteLabelEntity)
  }

  override def insertNotePerson(
      notePersonEntity: NotePersonEntity
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

  private inline def insertNoteLabelQuery = quote {
    (noteLabelEntity: NoteLabelEntity) =>
      sql"""
      INSERT INTO note_label(note_id,label_id)
      VALUES (${noteLabelEntity.noteId},${noteLabelEntity.labelId})
    """
        .as[Insert[Any]]
  }

  private inline def insertNotePersonQuery = quote {
    (notePersonEntity: NotePersonEntity) =>
      sql"""
      INSERT INTO note_person(note_id,person_id,role) 
      VALUES (${notePersonEntity.noteId},${notePersonEntity.personId},${notePersonEntity.role})
    """
        .as[Insert[Any]]
  }
}

object DbHelperImpl {
  lazy val layer = ZLayer.derive[DbHelperImpl]
}
