package zote.services

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.*
import zote.dto.*
import zote.dto.form.{NoteForm, NotePersonForm}
import zote.enums.{NotePersonRole, NoteStatus}
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}

object NoteServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NoteService")(
      suite("provides function 'getAll' that")(
        test("returns list of NoteHeaders if some exist") {
          for {
            noteEntities <- DbHelper.insertNotes(
              List(
                NoteEntity(
                  title = "Note 1",
                  message = "Message 1",
                  status = NoteStatus.Ongoing,
                  parentId = None
                ),
                NoteEntity(
                  title = "Note 2",
                  message = "Message 2",
                  status = NoteStatus.Ongoing,
                  parentId = None
                )
              )
            )
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            _ <- DbHelper.insertNoteLabels(
              noteEntities.map { noteEntity =>
                NoteLabelEntity(
                  noteId = noteEntity.id,
                  labelId = labelEntity.id
                )
              }
            )

            noteService <- ZIO.service[NoteService]
            noteHeaders <- noteService.getAll
          } yield assertTrue {
            noteHeaders.size == noteEntities.size
            && noteHeaders.forall { noteHeader =>
              noteEntities
                .find(noteEntity => noteEntity.id == noteHeader.id)
                .exists { noteEntity =>
                  noteHeader.title == noteEntity.title
                  && noteHeader.status == noteEntity.status
                }
              && noteHeader.labels.contains(
                List(Label(id = labelEntity.id, name = labelEntity.name))
              )
            }
          }
        },
        test("returns empty list if none exist") {
          for {
            noteService <- ZIO.service[NoteService]
            noteHeaders <- noteService.getAll
          } yield assertTrue {
            noteHeaders.isEmpty
          }
        }
      ),
      suite("provides function 'getById' that")(
        test("returns Note if exists") {
          for {
            parentNoteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 2",
                message = "Message 2",
                status = NoteStatus.Ongoing,
                parentId = Some(parentNoteEntity.id)
              )
            )
            childNoteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 3",
                message = "Message 3",
                status = NoteStatus.Ongoing,
                parentId = Some(noteEntity.id)
              )
            )
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            _ <- DbHelper.insertNoteLabels(
              List(
                NoteLabelEntity(
                  noteId = noteEntity.id,
                  labelId = labelEntity.id
                )
              )
            )
            _ <- DbHelper.insertNotePersons(
              List(
                NotePersonEntity(
                  noteId = noteEntity.id,
                  personId = personEntity.id,
                  role = NotePersonRole.Owner
                )
              )
            )

            noteService <- ZIO.service[NoteService]
            note <- noteService.getById(noteEntity.id)
          } yield assertTrue {
            note == Note(
              header = NoteHeader(
                id = noteEntity.id,
                title = noteEntity.title,
                status = noteEntity.status,
                labels = Some(
                  List(Label(id = labelEntity.id, name = labelEntity.name))
                )
              ),
              message = noteEntity.message,
              assignees = Some(
                List(
                  NotePerson(
                    person =
                      Person(id = personEntity.id, name = personEntity.name),
                    roles = List(NotePersonRole.Owner)
                  )
                )
              ),
              parentNote = Some(
                NoteHeader(
                  id = parentNoteEntity.id,
                  title = parentNoteEntity.title,
                  status = parentNoteEntity.status,
                  labels = None
                )
              ),
              childrenNotes = Some(
                List(
                  NoteHeader(
                    id = childNoteEntity.id,
                    title = childNoteEntity.title,
                    status = childNoteEntity.status,
                    labels = None
                  )
                )
              )
            )
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            noteService <- ZIO.service[NoteService]
            result <- noteService
              .getById(-1)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Note id: -1 not found"
              case _ => false
          }
        }
      ),
      suite("provides function 'create' that")(
        test("creates and returns Note") {
          for {
            parentNoteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))

            noteService <- ZIO.service[NoteService]
            note <- noteService.create(
              NoteForm(
                title = "title",
                message = "message",
                status = NoteStatus.Ongoing,
                assignees = Set(
                  NotePersonForm(
                    personId = personEntity.id,
                    role = NotePersonRole.Owner
                  )
                ),
                parentId = Some(parentNoteEntity.id),
                labels = Set(labelEntity.id)
              )
            )
          } yield assertTrue {
            note == Note(
              header = NoteHeader(
                id = note.header.id,
                title = "title",
                status = NoteStatus.Ongoing,
                labels = Some(
                  List(Label(id = labelEntity.id, name = labelEntity.name))
                )
              ),
              message = "message",
              assignees = Some(
                List(
                  NotePerson(
                    person =
                      Person(id = personEntity.id, name = personEntity.name),
                    roles = List(NotePersonRole.Owner)
                  )
                )
              ),
              parentNote = Some(
                NoteHeader(
                  id = parentNoteEntity.id,
                  title = parentNoteEntity.title,
                  status = parentNoteEntity.status,
                  labels = None
                )
              ),
              childrenNotes = None
            )
          }
        }
      ),
      suite("provides function 'update' that")(
        test("updates and returns Note") {
          for {
            parentNoteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 2",
                message = "Message 2",
                status = NoteStatus.Ongoing,
                parentId = Some(parentNoteEntity.id)
              )
            )
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))

            noteService <- ZIO.service[NoteService]
            note <- noteService.update(
              noteEntity.id,
              NoteForm(
                title = "title",
                message = "message",
                status = NoteStatus.Complete,
                assignees = Set(
                  NotePersonForm(
                    personId = personEntity.id,
                    role = NotePersonRole.Owner
                  )
                ),
                parentId = Some(parentNoteEntity.id),
                labels = Set(labelEntity.id)
              )
            )
          } yield assertTrue {
            note == Note(
              header = NoteHeader(
                id = noteEntity.id,
                title = "title",
                status = NoteStatus.Complete,
                labels = Some(
                  List(Label(id = labelEntity.id, name = labelEntity.name))
                )
              ),
              message = "message",
              assignees = Some(
                List(
                  NotePerson(
                    person =
                      Person(id = personEntity.id, name = personEntity.name),
                    roles = List(NotePersonRole.Owner)
                  )
                )
              ),
              parentNote = Some(
                NoteHeader(
                  id = parentNoteEntity.id,
                  title = parentNoteEntity.title,
                  status = parentNoteEntity.status,
                  labels = None
                )
              ),
              childrenNotes = None
            )
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            noteService <- ZIO.service[NoteService]
            result <- noteService
              .update(
                -1,
                NoteForm(
                  title = "title",
                  message = "message",
                  status = NoteStatus.Ongoing,
                  assignees = Set(),
                  parentId = None,
                  labels = Set()
                )
              )
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Note id: -1 not found"
              case _ => false
          }
        }
      ),
      suite("provides function 'delete' that")(
        test("deletes Label") {
          for {
            parentNoteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 2",
                message = "Message 2",
                status = NoteStatus.Ongoing,
                parentId = Some(parentNoteEntity.id)
              )
            )
            _ <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 3",
                message = "Message 3",
                status = NoteStatus.Ongoing,
                parentId = Some(noteEntity.id)
              )
            )
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            _ <- DbHelper.insertNoteLabels(
              List(
                NoteLabelEntity(
                  noteId = noteEntity.id,
                  labelId = labelEntity.id
                )
              )
            )
            _ <- DbHelper.insertNotePersons(
              List(
                NotePersonEntity(
                  noteId = noteEntity.id,
                  personId = personEntity.id,
                  role = NotePersonRole.Owner
                )
              )
            )

            noteService <- ZIO.service[NoteService]
            _ <- noteService.delete(noteEntity.id)
            result <- noteService
              .getById(noteEntity.id)
              .fold(_ => true, _ => false)
          } yield assertTrue(result)
        },
        test("returns NotFoundException if not exists") {
          for {
            noteService <- ZIO.service[NoteService]
            result <- noteService
              .delete(-1)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Note id: -1 not found"
              case _ => false
          }
        }
      )
    )
      @@ TestAspectUtils.rollback
      @@ TestAspect.beforeAll(FlywayService.run)
      @@ TestAspect.sequential
  }.provide(
    FlywayServiceImpl.layer,
    FlywayConfig.layer,
    NoteServiceImpl.layer,
    PersonServiceImpl.layer,
    LabelServiceImpl.layer,
    NoteRepositoryImpl.layer,
    LabelRepositoryImpl.layer,
    PersonRepositoryImpl.layer,
    NotePersonRepositoryImpl.layer,
    NoteLabelRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}
