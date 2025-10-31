package grpc
import example.products.products.ProductsGrpc
import example.users.users.UsersGrpc
import io.grpc.ServerBuilder

import scala.concurrent.ExecutionContext


object GrpcServer extends App {
  implicit val ec: ExecutionContext = ExecutionContext.global

  val server =
    ServerBuilder
      .forPort(50051)
      // Registrar ambos servicios
      .addService(ProductsGrpc.bindService(new ProductImpl(), ec))
      .addService(UsersGrpc.bindService(new UserImpl(), ec))
      .build()
      .start()

  println("✅ Servidor gRPC corriendo con Greeter y Farewell")
  server.awaitTermination()
}