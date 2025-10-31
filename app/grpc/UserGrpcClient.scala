package grpc

import example.users.users._
import io.grpc.ManagedChannelBuilder
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

/** Cliente gRPC para el servicio Users */
class UserGrpcClient(implicit ec: ExecutionContext) {
  private val channel = ManagedChannelBuilder
    .forAddress("localhost", 50051)
    .usePlaintext()
    .build()

  private val stub = UsersGrpc.stub(channel)

  def addUser(name: String, fullname: String, age: Int): Future[AddUserReply] = {
    val request = AddUserRequest(
      name = name,
      fullname = fullname,
      age = age
    )
    stub.addUser(request)
  }

  def getUser(id: String): Future[GetUserReply] =
    stub.getUser(GetUserRequest(id))

  def listUsers(): Future[ListUsersReply] =
    stub.listUsers(ListUsersRequest())


  def addProductToUser(user_id: String, product_id: String): Future[AddProductToUserReply] = {
    stub.addProductToUser(AddProductToUserRequest(user_id, product_id))
  }
  def shutdown(): Unit = channel.shutdown()
}
