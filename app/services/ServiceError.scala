package services

abstract class ServiceError {
  val errorCode: Int
  val errorMessage: String
}
