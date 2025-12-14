package zote.helpers

import io.getquill.*
import zio.*
import zote.Ids.{LabelId, NoteId, UserId}
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.includes.given

case class DbHelper(
    private val quillContext: QuillContext,
) {

  import quillContext.*

  def insertUser(userEntity: UserEntity): Task[UserEntity] =
    transaction {
      run(insertUserQuery(lift(userEntity)))
        .map(_.head)
        .map(id => userEntity.copy(id = UserId(id)))
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

  def insertNoteUser(
                      noteUserEntity: NoteUserEntity,
  ): Task[NoteUserEntity] = transaction {
    run(insertNoteUserQuery(lift(noteUserEntity)))
      .as(noteUserEntity)
  }

  private inline def insertUserQuery = quote { (userEntity: UserEntity) =>
    sql"""
      SELECT ID
      FROM FINAL TABLE (
        INSERT INTO `user`(name)
        VALUES (${userEntity.name})
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

  private inline def insertNoteUserQuery = quote { (noteUserEntity: NoteUserEntity) =>
    sql"""
      INSERT INTO note_user(note_id,user_id,role) 
      VALUES (${noteUserEntity.noteId},${noteUserEntity.userId},${noteUserEntity.role})
    """
      .as[Insert[Any]]
  }
}

object DbHelper {
  lazy val layer = ZLayer.derive[DbHelper]

  def insertUser(userEntity: UserEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertUser(userEntity))

  def insertLabel(labelEntity: LabelEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertLabel(labelEntity))

  def insertNote(noteEntity: NoteEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertNote(noteEntity))

  def insertNoteLabel(noteLabelEntity: NoteLabelEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertNoteLabel(noteLabelEntity))

  def insertNoteUser(noteUserEntity: NoteUserEntity) =
    ZIO.serviceWithZIO[DbHelper](_.insertNoteUser(noteUserEntity))
}
