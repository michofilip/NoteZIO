package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.{NoteUserEntity, UserEntity}

trait NoteUserRepository {

    def findUsersWithNoteIds(noteIds: Seq[Long]): Task[List[(UserEntity, Long)]]

    def updateNoteUsers(noteId: Long, userIds: Seq[Long]): Task[Unit]
}

object NoteUserRepository {
    lazy val layer = ZLayer.derive[NoteUserRepositoryImpl]
}

case class NoteUserRepositoryImpl(
    private val quillContext: QuillContext
) extends NoteUserRepository {

    import quillContext.postgres.*

    override def findUsersWithNoteIds(noteIds: Seq[Long]): Task[List[(UserEntity, Long)]] = transaction {
        run {
            query[NoteUserEntity]
                .join(query[UserEntity]).on((nu, u) => nu.userId == u.id)
                .filter { case (nu, _) => liftQuery(noteIds).contains(nu.noteId) }
                .map { case (nu, u) => (u, nu.noteId) }
        }
    }

    override def updateNoteUsers(noteId: Long, userIds: Seq[Long]): Task[Unit] = transaction {
        for {
            currentUserIds <- run(query[NoteUserEntity].filter(nu => nu.noteId == lift(noteId)).map(_.userId))

            userIdsToDelete <- ZIO.succeed(currentUserIds.filterNot(userIds.contains))
            _ <- run(query[NoteUserEntity].filter(nu => nu.noteId == lift(noteId) && liftQuery(userIdsToDelete).contains(nu.userId)).delete)

            userIdsToAdd <- ZIO.succeed(userIds.filterNot(currentUserIds.contains))
            noteUserEntities <- ZIO.succeed(userIdsToAdd.map(userId => NoteUserEntity(noteId, userId)))
            _ <- run(liftQuery(noteUserEntities).foreach(nu => query[NoteUserEntity].insertValue(nu)))
        } yield ()
    }
}
