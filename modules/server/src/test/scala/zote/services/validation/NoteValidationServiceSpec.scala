package zote.services.validation

import zio.*
import zio.test.*
import zote.Ids.{LabelId, NoteId, PersonId}
import zote.dto.form.{NoteForm, NotePersonForm}
import zote.enums.{NotePersonRole, NoteStatus}
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
                  NotePersonForm.Raw(
                    personId = Some(PersonId(1)),
                    role = Some(NotePersonRole.Owner),
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
                  NotePersonForm(
                    personId = PersonId(1),
                    role = NotePersonRole.Owner,
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
                      NotePersonForm.Raw(),
                    ),
                  ),
                ),
              )
              .exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages =
                Set(
                  "personId is required",
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
