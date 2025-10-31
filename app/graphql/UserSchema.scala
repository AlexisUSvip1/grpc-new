package graphql

import example.products.products.GetProductReply
import example.users.users._
import sangria.schema._
import grpc.UserGrpcClient

import scala.concurrent.{ExecutionContext, Future}

/** Esquema GraphQL que usa el cliente gRPC de usuarios */
object UserSchema {

  def createSchema(client: UserGrpcClient)(implicit ec: ExecutionContext): Schema[Unit, Unit] = {

    val UserType: ObjectType[Unit, GetUserReply] = ObjectType(
      "User",
      fields[Unit, GetUserReply](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field("fullname", StringType, resolve = _.value.fullname),
        Field("age", IntType, resolve = _.value.age)
      )
    )

    val ProductType: ObjectType[Unit, GetProductReply] = ObjectType(
      "Product",
      fields[Unit, GetProductReply](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field("price", IntType, resolve = _.value.price),
        Field("order", StringType, resolve = _.value.order)
      )
    )

    val UserProductsType: ObjectType[Unit, (GetUserReply, Seq[GetProductReply])] = ObjectType(
      "UserProducts",
      fields[Unit, (GetUserReply, Seq[GetProductReply])](
        Field("userId", StringType, resolve = _.value._1.id),
        Field("userName", StringType, resolve = _.value._1.fullname),
        Field("products", ListType(ProductType), resolve = _.value._2)
      )
    )

    val AddUserReplyType: ObjectType[Unit, AddUserReply] = ObjectType(
      "AddUserReply",
      fields[Unit, AddUserReply](
        Field("message", StringType, resolve = _.value.message)
      )
    )

    val QueryType = ObjectType("Query", fields[Unit, Unit](
      Field("users", ListType(UserType),
        description = Some("Lista de usuarios registrados"),
        resolve = _ => client.listUsers().map(_.users)
      ),
      Field("user", OptionType(UserType),
        arguments = Argument("id", StringType) :: Nil,
        description = Some("Obtener usuario por ID"),
        resolve = ctx => client.getUser(ctx.arg[String]("id")).map(Some(_))
      ),
      Field("userProducts", ListType(UserProductsType),
        description = Some("Lista de usuarios con sus productos asociados"),
        resolve = _ => {
          for {
            users <- client.listUsers().map(_.users)
            allProducts <- client.listProducts().map(_.products)
          } yield {
            users.map { user =>
              val userProds = allProducts.filter(_.id == user.productId)
              (user, userProds)
            }
          }
        }
      )
    ))

    val MutationType = ObjectType("Mutation", fields[Unit, Unit](
      Field("addUser", AddUserReplyType,
        arguments = List(
          Argument("name", StringType),
          Argument("fullname", StringType),
          Argument("age", IntType)
        ),
        resolve = ctx => client.addUser(
          ctx.arg[String]("name"),
          ctx.arg[String]("fullname"),
          ctx.arg[Int]("age")
        )
      )
    ))

    Schema(QueryType, Some(MutationType))
  }
}
