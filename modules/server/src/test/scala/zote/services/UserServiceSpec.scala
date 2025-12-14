package zote.services

import zio.*
import zio.test.*
import zote.Ids.UserId
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NoteUserEntity, UserEntity}
import zote.db.repositories.{NoteUserRepositoryImpl, UserRepositoryImpl}
import zote.dto.User
import zote.dto.form.UserForm
import zote.enums.{NoteUserRole, NoteStatus}
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, TestAspectUtils}

object UserServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("UserService")(
      suite("provides function 'getAll' that")(
        test("returns list of Users if some exist") {
          for {
            userEntity1 <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userEntity2 <- DbHelper.insertUser(UserEntity(name = "Ela"))

            userService <- ZIO.service[UserService]
            users       <- userService.getAll
          } yield assertTrue {
            users.size == 2 &&
            users.contains(User(id = userEntity1.id, name = userEntity1.name)) &&
            users.contains(User(id = userEntity2.id, name = userEntity2.name))
          }
        },
        test("returns empty list if none exist") {
          for {
            userService <- ZIO.service[UserService]
            users       <- userService.getAll
          } yield assertTrue {
            users.isEmpty
          }
        },
      ),
      suite("provides function 'getById' that")(
        test("returns User if exists") {
          for {
            userEntity  <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userService <- ZIO.service[UserService]
            user        <- userService.getById(userEntity.id)
          } yield assertTrue {
            user.id == userEntity.id &&
            user.name == userEntity.name
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            userService <- ZIO.service[UserService]
            result        <- userService.getById(UserId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("User id: -1 not found"))
          }
        },
      ),
      suite("provides function 'create' that")(
        test("creates and returns User") {
          for {
            userService <- ZIO.service[UserService]
            user        <- userService.create(UserForm(name = "Ala"))
          } yield assertTrue {
            !user.id.isZero &&
            user.name == "Ala"
          }
        },
      ),
      suite("provides function 'update' that")(
        test("updates and returns User") {
          for {
            userEntity  <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userService <- ZIO.service[UserService]
            user <- userService.update(
              userEntity.id,
              UserForm(name = "Ela"),
            )
          } yield assertTrue {
            user.id == userEntity.id &&
            user.name == "Ela"
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            userService <- ZIO.service[UserService]
            result        <- userService.update(UserId(-1), UserForm(name = "Ela")).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("User id: -1 not found"))
          }
        },
      ),
      suite("provides function 'delete' that")(
        test("deletes User") {
          for {
            userEntity <- DbHelper.insertUser(UserEntity(name = "Ala"))
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = Some("Message 1"),
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            noteUserEntity <- DbHelper.insertNoteUser(
              NoteUserEntity(
                noteId = noteEntity.id,
                userId = userEntity.id,
                role = NoteUserRole.Owner,
              ),
            )

            userService      <- ZIO.service[UserService]
            resultBeforeDelete <- userService.getById(userEntity.id).exit
            _                  <- userService.delete(userEntity.id)
            resultAfterDelete  <- userService.getById(userEntity.id).exit
          } yield assertTrue {
            resultBeforeDelete.isSuccess &&
            resultAfterDelete.isFailure
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            userService <- ZIO.service[UserService]
            result        <- userService.delete(UserId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("User id: -1 not found"))
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
    UserServiceImpl.layer,
    UserRepositoryImpl.layer,
    NoteUserRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}
