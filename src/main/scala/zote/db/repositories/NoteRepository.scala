package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NoteUserEntity, UserEntity}

trait NoteRepository {
    def findAll: Task[Seq[(NoteEntity, Seq[UserEntity])]]

    def findById(id: Long): Task[Option[(NoteEntity, Seq[UserEntity])]]

    def save(noteEntity: NoteEntity): Task[Long]

    def delete(id: Long): Task[Unit]

    def updateNoteUsers(noteId: Long, userIds: Seq[Long]): Task[Unit]
}

object NoteRepository {
    lazy val layer = ZLayer.derive[NoteRepositoryImpl]
}

case class NoteRepositoryImpl(
    private val quillContext: QuillContext
) extends NoteRepository {

    import quillContext.postgres.*

    override def findAll: Task[Seq[(NoteEntity, Seq[UserEntity])]] = {
        val n = run(query[NoteEntity])
        val nuu = run(query[NoteUserEntity].join(query[UserEntity]).on((nu, u) => nu.userId == u.id))

        (n <&> nuu).map { case (notes, users) =>
            groupFetched(notes, users)
        }
    }

    override def findById(id: Long): Task[Option[(NoteEntity, Seq[UserEntity])]] = {
        val n = run(query[NoteEntity].filter(n => n.id == lift(id)))
        val nuu = run(
            query[NoteUserEntity]
                .join(query[UserEntity]).on((nu, u) => nu.userId == u.id)
                .filter { case (nu, _) => nu.noteId == lift(id) }
        )

        (n <&> nuu).map { case (notes, users) =>
            groupFetched(notes, users)
        }.map(_.headOption)
    }

    override def save(noteEntity: NoteEntity): Task[Long] = {
        if (noteEntity.id == 0) {
            run(insertNote(lift(noteEntity)))
        } else {
            run(updateNote(lift(noteEntity)))
        }
    }

    override def delete(id: Long): Task[Unit] = {
        run(query[NoteEntity].filter(i => i.id == lift(id)).delete).unit
    }

    override def updateNoteUsers(noteId: Long, userIds: Seq[Long]): Task[Unit] = transaction {
        for {
            currentUserIds <- run(query[NoteUserEntity].filter(nu => nu.noteId == lift(noteId)).map(_.userId))
            userIdsToDelete <- ZIO.succeed(currentUserIds.filterNot(userIds.contains))
            userIdsToAdd <- ZIO.succeed(userIds.filterNot(currentUserIds.contains))
            noteUserEntities <- ZIO.succeed(userIdsToAdd.map(userId => NoteUserEntity(noteId, userId)))
            _ <- run(query[NoteUserEntity].filter(nu => liftQuery(userIdsToDelete).contains(nu.userId) && nu.noteId == lift(noteId)).delete)
            _ <- run(liftQuery(noteUserEntities).foreach(nu => query[NoteUserEntity].insertValue(nu)))
        } yield ()
    }

    private inline def insertNote = quote { (noteEntity: NoteEntity) =>
        query[NoteEntity].insertValue(noteEntity).returning(i => i.id)
    }

    private inline def updateNote = quote { (noteEntity: NoteEntity) =>
        query[NoteEntity].filter(i => i.id == noteEntity.id).updateValue(noteEntity).returning(i => i.id)
    }

    private def groupFetched(notes: Seq[NoteEntity], users: Seq[(NoteUserEntity, UserEntity)]) = {
        val usersByNoteId = users.groupMap { case (noteUser, _) => noteUser.noteId } { case (_, user) => user }

        notes.map { note =>
            (note, usersByNoteId.getOrElse(note.id, Seq.empty))
        }
    }
}
