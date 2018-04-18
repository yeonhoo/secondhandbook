package models.aws

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model._
import java.io.File

import com.amazonaws.regions.Regions
import play.api.{Configuration, Logger}
import javax.inject.Inject

class S3Sender(image: File, imageName: String) {
  def send() = {
    val putRequest = new PutObjectRequest(S3Sender.s3bucket, imageName, image)
    putRequest.setCannedAcl(CannedAccessControlList.PublicRead)

    Logger.info("sending to s3: " + imageName)
    S3Sender.s3Client.putObject(putRequest)
    
    if (!image.delete) Logger.info("could not delete original file %s after sending it to s3".format(imageName))
  }
}

object S3Sender {
  //TODO: use DI
  val accessKey = "AKIAIURS6CUHD4O4QR2Q"
  val secretKey = "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"
  val s3bucket = "testando.yun"
  //val config = play.api.Play.current.configuration
  //val bucket = config.get[String]("aws.s3.bucket")


  val creds: BasicAWSCredentials  = new BasicAWSCredentials(accessKey, secretKey);
  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
                  .withRegion(Regions.SA_EAST_1)
                  .withCredentials(new AWSStaticCredentialsProvider(creds)).build()
}

class S3Cred @Inject() (config: Configuration) {
  val accessKey = config.get[String]("aws.accessKey")
  val secretKey = config.get[String]("aws.secretKey")
  val s3bucket = config.get[String]("aws.s3.bucket")


}