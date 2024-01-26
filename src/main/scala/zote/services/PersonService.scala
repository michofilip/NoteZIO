package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.db.repositories.{NotePersonRepository, PersonRepository}
import zote.dto.{NotePerson, Person, PersonForm}
import zote.exceptions.NotFoundException

trait PersonService {
    def getAll: Task[Seq[Person]]

    def getById(id: Long): Task[Person]

    def create(personForm: PersonForm): Task[Person]

    def update(id: Long, personForm: PersonForm): Task[Person]

    def delete(id: Long): Task[Unit]

    def getNotePersonsByNoteIds(noteIds: Seq[Long]): Task[Map[Long, List[NotePerson]]]

    def validatePersonsExist(personIds: Set[Long]): Task[Unit]
}

object PersonService {
    lazy val layer = ZLayer.derive[PersonServiceImpl]
}

case class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val notePersonRepository: NotePersonRepository,
    private val quillContext: QuillContext
) extends PersonService {

    import quillContext.postgres.*

    override def getAll: Task[Seq[Person]] = transaction {
        personRepository.findAll.flatMap(toDtos)
    }

    override def getById(id: Long): Task[Person] = transaction {
        getEntityById(id).flatMap(toDto)
    }

    override def create(personForm: PersonForm): Task[Person] = upsert(personForm) {
        ZIO.succeed {
            PersonEntity(name = personForm.name)
        }
    }

    override def update(id: Long, personForm: PersonForm): Task[Person] = upsert(personForm) {
        getEntityById(id).map(_
            .modify(_.name).setTo(personForm.name)
        )
    }

    private def upsert(personForm: PersonForm)(f: => Task[PersonEntity]): Task[Person] = transaction {
        for {
            _ <- PersonForm.validateZIO(personForm)
            personEntity <- f
            personEntity <- personRepository.upsert(personEntity)
            person <- toDto(personEntity)
        } yield person
    }

    override def delete(id: Long): Task[Unit] = transaction {
        for {
            _ <- getEntityById(id)
            _ <- personRepository.delete(id)
        } yield ()
    }

    override def getNotePersonsByNoteIds(noteIds: Seq[Long]): Task[Map[Long, List[NotePerson]]] = {
        for {
            personNoteEntitiesWithPersonEntities <- notePersonRepository.findByNoteIds(noteIds)
            personEntities <- ZIO.succeed(personNoteEntitiesWithPersonEntities.map(_._2).distinct)
            personById <- toDtos(personEntities).map(_.map(u => u.id -> u).toMap)
            notePersonsByNoteIds <- ZIO.succeed {
                personNoteEntitiesWithPersonEntities.groupMap { case (notePersonEntity, _) =>
                    notePersonEntity.noteId
                } { case (notePersonEntity, personEntity) =>
                    NotePerson(
                        person = personById(personEntity.id),
                        owner = notePersonEntity.owner
                    )
                }
            }
        } yield notePersonsByNoteIds
    }

    override def validatePersonsExist(personIds: Set[Long]): Task[Unit] = {
        for {
            existingPersonIds <- personRepository.findExistingIds(personIds)
            missingPersonIds <- ZIO.succeed(personIds -- existingPersonIds)
            _ <- ZIO.unless(missingPersonIds.isEmpty) {
                ZIO.fail(NotFoundException(s"Persons ids: ${missingPersonIds.mkString(", ")} not found"))
            }
        } yield ()
    }

    private def getEntityById(id: Long): Task[PersonEntity] = {
        personRepository.findById(id).someOrFail(NotFoundException(s"Person id: $id not found"))
    }

    private def toDtos(personEntities: Seq[PersonEntity]): Task[List[Person]] = {
        ZIO.succeed {
            personEntities.map { personEntity =>
                Person(
                    id = personEntity.id,
                    name = personEntity.name,
                )
            }.toList
        }
    }

    private def toDto(personEntity: PersonEntity): Task[Person] = {
        toDtos(List(personEntity)).map(_.head)
    }
}
