package zote.services.validation

import zio.*
import zio.test.*
import zote.Ids.{LabelId, NoteId, UserId}
import zote.dto.form.{NoteForm, NoteUserForm}
import zote.enums.{NoteUserRole, NoteStatus}
import zote.exceptions.ValidationException

object NoteValidationServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NoteValidationService")(
      suite("provides function 'validate' that")(
        test("returns valid NoteForm if correct") {
          val noteFormRaw =
            NoteForm.Raw(
              title = Some("title"),
              status = Some(NoteStatus.Draft),
              message = Some("message"),
              assignees = Some(
                Set(
                  NoteUserForm.Raw(
                    userId = Some(UserId(1)),
                    role = Some(NoteUserRole.Owner),
                  ),
                ),
              ),
              parentId = Some(NoteId(1)),
              labels = Some(Set(LabelId(1))),
            )

          for {
            noteValidationService <- ZIO.service[NoteValidationService]
            result                <- noteValidationService.validate(noteFormRaw).exit
          } yield assertTrue {
            result == Exit.succeed {
              NoteForm(
                title = "title",
                status = NoteStatus.Draft,
                message = Some("message"),
                assignees = Set(
                  NoteUserForm(
                    userId = UserId(1),
                    role = NoteUserRole.Owner,
                  ),
                ),
                parentId = Some(NoteId(1)),
                labels = Set(LabelId(1)),
              )
            }
          }
        },
        test("returns ValidationException if required attributes are missing") {
          for {
            noteValidationService <- ZIO.service[NoteValidationService]
            result                <- noteValidationService.validate(NoteForm.Raw()).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages =
                Set(
                  "title is required",
                  "status is required",
                ),
              )
            }
          }
        },
        test("returns ValidationException if title is blank") {
          for {
            noteValidationService <- ZIO.service[NoteValidationService]
            result <- noteValidationService
              .validate(
                NoteForm.Raw(
                  title = Some(" "),
                  status = Some(NoteStatus.Draft),
                ),
              )
              .exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("title must not be blank"))
            }
          }
        },
        test("returns ValidationException if title is too long") {
          for {
            noteValidationService <- ZIO.service[NoteValidationService]
            result <- noteValidationService
              .validate(
                NoteForm.Raw(
                  title = Some(List.fill(256)("x").mkString),
                  status = Some(NoteStatus.Draft),
                ),
              )
              .exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("title must be shorter or equal to 255"))
            }
          }
        },
        test("returns ValidationException if required attributes in assignees are missing") {
          for {
            noteValidationService <- ZIO.service[NoteValidationService]
            result <- noteValidationService
              .validate(
                NoteForm.Raw(
                  title = Some("title"),
                  status = Some(NoteStatus.Draft),
                  assignees = Some(
                    Set(
                      NoteUserForm.Raw(),
                    ),
                  ),
                ),
              )
              .exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages =
                Set(
                  "userId is required",
                  "role is required",
                ),
              )
            }
          }
        },
      ),
    ) @@ TestAspect.sequential
  }.provide(
    NoteValidationServiceImpl.layer,
  )
}
