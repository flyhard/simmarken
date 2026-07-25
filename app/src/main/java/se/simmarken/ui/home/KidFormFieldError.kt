package se.simmarken.ui.home

sealed interface KidFormFieldError {
    data object NameEmpty : KidFormFieldError
    data object NameTooLong : KidFormFieldError
    data object LoadFailed : KidFormFieldError
}
