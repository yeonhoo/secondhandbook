package models.aws

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model._
import java.io.File

import com.amazonaws.regions.Regions
import play.api.{Logger}


trait Sender {
  def send(image: File, imageName: String)
}

class S3Sender(accessKey: String, secretKey: String, s3bucket: String) extends Sender {

  def send(image: File, imageName: String) = {
    val putRequest = new PutObjectRequest(s3bucket, imageName, image)
    putRequest.setCannedAcl(CannedAccessControlList.PublicRead)

    Logger.info("sending to s3: " + imageName)
    s3Client.putObject(putRequest)

    if (!image.delete) Logger.info("could not delete original file %s after sending it to s3".format(imageName))
  }

  val credentials: BasicAWSCredentials  = new BasicAWSCredentials(accessKey, secretKey)

  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withRegion(Regions.SA_EAST_1)
    .withCredentials(new AWSStaticCredentialsProvider(credentials)).build()

}