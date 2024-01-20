package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.NoteEntity

trait NoteRepository {
    def findAll: Task[List[NoteEntity]]

    def findById(id: Long): Task[Option[NoteEntity]]

    def save(noteEntity: NoteEntity): Task[NoteEntity]

    def delete(id: Long): Task[NoteEntity]
}

object NoteRepository {
    lazy val layer = ZLayer.derive[NoteRepositoryImpl]
}

case class NoteRepositoryImpl(
    private val quillContext: QuillContext
) extends NoteRepository {

    import quillContext.postgres.*

    override def findAll: Task[List[NoteEntity]] = {
        run(query[NoteEntity])
    }

    override def findById(id: Long): Task[Option[NoteEntity]] = {
        run(query[NoteEntity].filter(n => n.id == lift(id)))
            .map(_.headOption)
    }

    override def save(noteEntity: NoteEntity): Task[NoteEntity] = {
        if (noteEntity.id == 0) {
            run(insertNote(lift(noteEntity)))
        } else {
            run(updateNote(lift(noteEntity)))
        }
    }

    override def delete(id: Long): Task[NoteEntity] = {
        run(query[NoteEntity].filter(i => i.id == lift(id)).delete.returning(n => n))
    }

    private inline def insertNote = quote { (noteEntity: NoteEntity) =>
        query[NoteEntity].insertValue(noteEntity).returning(n => n)
    }

    private inline def updateNote = quote { (noteEntity: NoteEntity) =>
        query[NoteEntity].filter(i => i.id == noteEntity.id).updateValue(noteEntity).returning(n => n)
    }
}
