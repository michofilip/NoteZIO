package zote

import zio.*
import zote.dto.form.{LabelForm, NoteForm, NoteUserForm, UserForm}
import zote.enums.{NoteUserRole, NoteStatus}
import zote.services.{LabelService, NoteService, UserService}

case class InitHelper(
    private val noteService: NoteService,
    private val userService: UserService,
    private val labelService: LabelService,
) {
  def initDb(): Task[Unit] = {
    for {
      label1 <- labelService.create(LabelForm(name = "Red"))
      label2 <- labelService.create(LabelForm(name = "Green"))
      label3 <- labelService.create(LabelForm(name = "Blue"))

      user1 <- userService.create(UserForm(name = "Ala"))
      user2 <- userService.create(UserForm(name = "Ela"))
      user3 <- userService.create(UserForm(name = "Ola"))
      user4 <- userService.create(UserForm(name = "Ula"))

      note1 <- noteService.create(
        NoteForm(
          title = "Title 1",
          message = Some("Message 1"),
          status = NoteStatus.Draft,
          assignees = Set(
            NoteUserForm(userId = user1.id, role = NoteUserRole.Owner),
          ),
          parentId = None,
          labels = Set(label1.id),
        ),
      )
      note2 <- noteService.create(
        NoteForm(
          title = "Title 2",
          message = Some("Message 2"),
          status = NoteStatus.Ongoing,
          assignees = Set(
            NoteUserForm(userId = user1.id, role = NoteUserRole.Owner),
            NoteUserForm(
              userId = user2.id,
              role = NoteUserRole.Maintainer,
            ),
            NoteUserForm(
              userId = user3.id,
              role = NoteUserRole.Observer,
            ),
          ),
          parentId = Some(note1.header.id),
          labels = Set(label1.id, label2.id, label3.id),
        ),
      )
      note3 <- noteService.create(
        NoteForm(
          title = "Title 3",
          message = Some("Message 3"),
          status = NoteStatus.Ongoing,
          assignees = Set(
            NoteUserForm(userId = user3.id, role = NoteUserRole.Owner),
          ),
          parentId = Some(note2.header.id),
          labels = Set(label2.id),
        ),
      )
      note4 <- noteService.create(
        NoteForm(
          title = "Title 4",
          message = Some("Message 4"),
          status = NoteStatus.Complete,
          assignees = Set(
            NoteUserForm(userId = user4.id, role = NoteUserRole.Owner),
          ),
          parentId = Some(note2.header.id),
          labels = Set(label3.id),
        ),
      )
      note5 <- noteService.create(
        NoteForm(
          title = "Title 5",
          message = None,
          status = NoteStatus.Draft,
          assignees = Set(),
          parentId = None,
          labels = Set(),
        ),
      )
    } yield ()
  }
}

object InitHelper {
  lazy val layer = ZLayer.derive[InitHelper]

  def initDb() = ZIO.serviceWithZIO[InitHelper](_.initDb())
}
