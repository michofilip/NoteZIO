package zote.db.repositories

import zio.*
import zio.test.*
import zote.Ids.{NoteId, UserId}
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NoteUserEntity, UserEntity}
import zote.enums.{NoteUserRole, NoteStatus}
import zote.helpers.{DbHelper, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object NoteUserRepositorySpec extends ZIOSpecDefault {

  private val note1 = NoteEntity(
    title = "Note 1",
    message = Some("Message 1"),
    status = NoteStatus.Ongoing,
    parentId = None,
  )

  private val note2 = NoteEntity(
    title = "Note 2",
    message = Some("Message 2"),
    status = NoteStatus.Ongoing,
    parentId = None,
  )

  private val note3 = NoteEntity(
    title = "Note 3",
    message = Some("Message 3"),
    status = NoteStatus.Ongoing,
    parentId = None,
  )

  private val user1 = UserEntity(name = "Ala")
  private val user2 = UserEntity(name = "Ela")
  private val user3 = UserEntity(name = "Ola")

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NoteUserRepository")(
      suite("provides function 'findAllByNoteId' that")(
        test("returns list of NoteUserEntities if some exist") {
          for {
            note    <- DbHelper.insertNote(note1)
            user1 <- DbHelper.insertUser(user1)
            user2 <- DbHelper.insertUser(user2)
            noteUser1 <- DbHelper.insertNoteUser(
              NoteUserEntity(
                noteId = note.id,
                userId = user1.id,
                role = NoteUserRole.Owner,
              ),
            )
            noteUser2 <- DbHelper.insertNoteUser(
              NoteUserEntity(
                noteId = note.id,
                userId = user2.id,
                role = NoteUserRole.Maintainer,
              ),
            )

            noteUserRepository <- ZIO.service[NoteUserRepository]
            noteUserEntities   <- noteUserRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteUserEntities.size == 2 &&
            noteUserEntities.contains(noteUser1) &&
            noteUserEntities.contains(noteUser2)
          }
        },
        test("returns empty list if none exist") {
          for {
            note <- DbHelper.insertNote(note1)

            noteUserRepository <- ZIO.service[NoteUserRepository]
            noteUserEntities   <- noteUserRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteUserEntities.isEmpty
          }
        },
      ),
      suite("provides function 'findAllByUserId' that")(
        test("returns list of NoteUserEntities if some exist") {
          for {
            note1  <- DbHelper.insertNote(note1)
            note2  <- DbHelper.insertNote(note2)
            user <- DbHelper.insertUser(user1)
            noteUser1 <- DbHelper.insertNoteUser(
              NoteUserEntity(
                noteId = note1.id,
                userId = user.id,
                role = NoteUserRole.Owner,
              ),
            )
            noteUser2 <- DbHelper.insertNoteUser(
              NoteUserEntity(
                noteId = note2.id,
                userId = user.id,
                role = NoteUserRole.Maintainer,
              ),
            )

            noteUserRepository <- ZIO.service[NoteUserRepository]
            noteUserEntities   <- noteUserRepository.findAllByUserId(user.id)
          } yield assertTrue {
            noteUserEntities.size == 2 &&
            noteUserEntities.contains(noteUser1) &&
            noteUserEntities.contains(noteUser2)
          }
        },
        test("returns empty list if none exist") {
          for {
            _      <- DbHelper.insertNote(note1)
            _      <- DbHelper.insertNote(note2)
            _      <- DbHelper.insertNote(note3)
            user <- DbHelper.insertUser(user1)

            noteUserRepository <- ZIO.service[NoteUserRepository]
            noteUserEntities <- noteUserRepository.findAllByUserId(
              user.id,
            )
          } yield assertTrue {
            noteUserEntities.isEmpty
          }
        },
      ),
      suite("provides function 'upsert' that")(
        test("inserts NoteUserEntity if not exists") {
          for {
            note   <- DbHelper.insertNote(note1)
            user <- DbHelper.insertUser(user1)
            noteUser = NoteUserEntity(
              noteId = note.id,
              userId = user.id,
              role = NoteUserRole.Owner,
            )

            noteUserRepository <- ZIO.service[NoteUserRepository]
            _                    <- noteUserRepository.upsert(noteUser)

            noteUserEntitiesByNoteId   <- noteUserRepository.findAllByNoteId(note.id)
            noteUserEntitiesByUserId <- noteUserRepository.findAllByUserId(user.id)
          } yield assertTrue {
            noteUserEntitiesByNoteId.size == 1 &&
            noteUserEntitiesByNoteId.contains(noteUser) &&
            noteUserEntitiesByUserId.size == 1 &&
            noteUserEntitiesByUserId.contains(noteUser)
          }
        },
        test("updates NoteUserEntity if exists") {
          for {
            note   <- DbHelper.insertNote(note1)
            user <- DbHelper.insertUser(user1)
            _ <- DbHelper.insertNoteUser(
              NoteUserEntity(
                noteId = note.id,
                userId = user.id,
                role = NoteUserRole.Owner,
              ),
            )
            noteUser = NoteUserEntity(
              noteId = note.id,
              userId = user.id,
              role = NoteUserRole.Maintainer,
            )

            noteUserRepository <- ZIO.service[NoteUserRepository]
            _                    <- noteUserRepository.upsert(noteUser)

            noteUserEntitiesByNoteId   <- noteUserRepository.findAllByNoteId(note.id)
            noteUserEntitiesByUserId <- noteUserRepository.findAllByUserId(user.id)
          } yield assertTrue {
            noteUserEntitiesByNoteId.size == 1 &&
            noteUserEntitiesByNoteId.contains(noteUser) &&
            noteUserEntitiesByUserId.size == 1 &&
            noteUserEntitiesByUserId.contains(noteUser)
          }
        },
      ),
      suite("provides function 'delete' that")(
        test("deletes NoteUserEntity if exist") {
          for {
            note   <- DbHelper.insertNote(note1)
            user <- DbHelper.insertUser(user1)
            noteUser <- DbHelper.insertNoteUser(
              NoteUserEntity(
                noteId = note.id,
                userId = user.id,
                role = NoteUserRole.Owner,
              ),
            )

            noteUserRepository                     <- ZIO.service[NoteUserRepository]
            noteUserEntitiesByNoteIdBeforeDelete   <- noteUserRepository.findAllByNoteId(note.id)
            noteUserEntitiesByUserIdBeforeDelete <- noteUserRepository.findAllByUserId(user.id)
            _                                        <- noteUserRepository.delete(note.id, user.id)
            noteUserEntitiesByNoteIdAfterDelete    <- noteUserRepository.findAllByNoteId(note.id)
            noteUserEntitiesByUserIdAfterDelete  <- noteUserRepository.findAllByUserId(user.id)
          } yield assertTrue {
            noteUserEntitiesByNoteIdBeforeDelete.nonEmpty &&
            noteUserEntitiesByNoteIdAfterDelete.isEmpty &&
            noteUserEntitiesByUserIdBeforeDelete.nonEmpty &&
            noteUserEntitiesByUserIdAfterDelete.isEmpty
          }
        },
        test("does nothing if NoteUserEntity not exist") {
          for {
            noteUserRepository <- ZIO.service[NoteUserRepository]
            result               <- noteUserRepository.delete(NoteId(-1), UserId(-1)).exit
          } yield assertTrue {
            result.isSuccess
          }
        },
      ),
    )
      @@ TestAspectUtils.rollback
      @@ TestAspect.beforeAll(FlywayService.run)
      @@ TestAspect.sequential
  }.provide(
    FlywayServiceImpl.layer,
    FlywayConfig.layer,
    NoteUserRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}
