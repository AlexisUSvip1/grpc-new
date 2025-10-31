package grpc
import example.users.users._
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class UserImpl(implicit ec: ExecutionContext) extends UsersGrpc.Users {
  private val users = ListBuffer.empty[GetUserReply]
  private val userProducts = ListBuffer.empty[(String, String)]

  override def addUser(req: AddUserRequest): Future[AddUserReply] = {
    val user = GetUserReply(
      id = UUID.randomUUID().toString,
      name = req.name,
      fullname = req.fullname,
      age = req.age
    )

    users += user

    val msg = s"Usuario agregado: ${req.fullname} (edad: ${req.age}) con ID: ${user.id}"
    Future.successful(AddUserReply(msg))
  }

  override def getUser(req: GetUserRequest): Future[GetUserReply] = {
    val found = users.find(_.id == req.id)
      .getOrElse(GetUserReply("", "No encontrado", "", 0))
    Future.successful(found)
  }

  override def listUsers(req: ListUsersRequest): Future[ListUsersReply] = {
    Future.successful(ListUsersReply(users.toSeq))
  }

  override def addProductToUser(req: AddProductToUserRequest): Future[AddProductToUserReply] = {
    val userExists = users.exists(_.id == req.userId)

    val msg =
      if (userExists) {
        userProducts += ((req.userId, req.productId))
        s"✅ Producto ${req.productId} agregado al usuario ${req.userId}"
      } else {
        s"❌ Usuario con ID ${req.userId} no encontrado"
      }

    Future.successful(AddProductToUserReply(msg))
  }
}
