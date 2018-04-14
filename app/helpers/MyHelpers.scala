package helpers

object MyHelpers {
  import views.html.helper.FieldConstructor
  implicit val myFields = FieldConstructor(views.html.field_constructor.registerfield.f)
}