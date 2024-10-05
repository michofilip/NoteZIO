package zote.helpers

import com.softwaremill.quicklens.modify
import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.includes.given

trait DbHelper {
  def insertPersons(personEntities: Seq[PersonEntity]): Task[List[PersonEntity]]
  def insertLabels(labelEntities: Seq[LabelEntity]): Task[List[LabelEntity]]
  def insertNotes(noteEntities: Seq[NoteEntity]): Task[List[NoteEntity]]
  def insertNoteLabels(
      noteLabelEntities: Seq[NoteLabelEntity]
  ): Task[List[NoteLabelEntity]]
  def insertNotePersons(
      notePersonEntities: Seq[NotePersonEntity]
  ): Task[List[NotePersonEntity]]
}

object DbHelper {
  def insertPersons(personEntities: Seq[PersonEntity]) =
    ZIO.serviceWithZIO[DbHelper](_.insertPersons(personEntities))

  def insertPerson(personEntity: PersonEntity) =
    insertPersons(List(personEntity)).map(_.head)

  def insertLabels(labelEntities: Seq[LabelEntity]) =
    ZIO.serviceWithZIO[DbHelper](_.insertLabels(labelEntities))

  def insertLabel(labelEntity: LabelEntity) =
    insertLabels(List(labelEntity)).map(_.head)

  def insertNotes(noteEntities: Seq[NoteEntity]) =
    ZIO.serviceWithZIO[DbHelper](_.insertNotes(noteEntities))

  def insertNote(noteEntity: NoteEntity) =
    insertNotes(List(noteEntity)).map(_.head)

  def insertNoteLabels(noteLabelEntities: Seq[NoteLabelEntity]) =
    ZIO.serviceWithZIO[DbHelper](_.insertNoteLabels(noteLabelEntities))

  def insertNoteLabel(noteLabelEntity: NoteLabelEntity) =
    insertNoteLabels(List(noteLabelEntity)).map(_.head)

  def insertNotePersons(notePersonEntities: Seq[NotePersonEntity]) =
    ZIO.serviceWithZIO[DbHelper](_.insertNotePersons(notePersonEntities))

  def insertNotePerson(notePersonEntity: NotePersonEntity) =
    insertNotePersons(List(notePersonEntity)).map(_.head)
}

case class DbHelperImpl(
    private val quillContext: QuillContext
) extends DbHelper {

  import quillContext.*

  override def insertPersons(
      personEntities: Seq[PersonEntity]
  ): Task[List[PersonEntity]] = transaction {
    ZIO
      .foreach(personEntities) { personEntity =>
        run(insertPersonQuery(lift(personEntity)))
          .map(_.head)
          .map(id => personEntity.modify(_.id).setTo(id))
      }
      .map(_.toList)
  }

  override def insertLabels(
      labelEntities: Seq[LabelEntity]
  ): Task[List[LabelEntity]] = transaction {
    ZIO
      .foreach(labelEntities) { labelEntity =>
        run(insertLabelQuery(lift(labelEntity)))
          .map(_.head)
          .map(id => labelEntity.modify(_.id).setTo(id))
      }
      .map(_.toList)
  }

  override def insertNotes(
      noteEntities: Seq[NoteEntity]
  ): Task[List[NoteEntity]] = transaction {
    ZIO
      .foreach(noteEntities) { noteEntity =>
        run(insertNoteQuery(lift(noteEntity)))
          .map(_.head)
          .map(id => noteEntity.modify(_.id).setTo(id))
      }
      .map(_.toList)
  }

  override def insertNoteLabels(
      noteLabelEntities: Seq[NoteLabelEntity]
  ): Task[List[NoteLabelEntity]] = transaction {
    ZIO
      .foreach(noteLabelEntities) { noteLabelEntity =>
        run(insertNoteLabelQuery(lift(noteLabelEntity)))
          .as(noteLabelEntity)
      }
      .map(_.toList)
  }

  override def insertNotePersons(
      notePersonEntities: Seq[NotePersonEntity]
  ): Task[List[NotePersonEntity]] = transaction {
    ZIO
      .foreach(notePersonEntities) { notePersonEntity =>
        run(insertNotePersonQuery(lift(notePersonEntity)))
          .as(notePersonEntity)
      }
      .map(_.toList)
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
