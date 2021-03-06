package com.rahulsinghai.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.imagebuilder.model.{ListImagesRequest, ListImagesResult}
import com.amazonaws.services.imagebuilder.{AWSimagebuilder, AWSimagebuilderClientBuilder}
import com.rahulsinghai.conf.AWSToolkitConfig
import com.rahulsinghai.model.ActionPerformed
import com.typesafe.scalalogging.StrictLogging

import scala.jdk.CollectionConverters._

object ImageBuilderActor extends StrictLogging {

  // actor protocol
  sealed trait ImageBuilderCommand
  case class ListImages(replyTo: ActorRef[ActionPerformed]) extends ImageBuilderCommand
  case class CreateNewImage(replyTo: ActorRef[ActionPerformed]) extends ImageBuilderCommand

  // Set up the Amazon image builder client
  val awsImageBuilderClient: AWSimagebuilder = AWSimagebuilderClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(AWSToolkitConfig.awsCredentials))
    .withRegion(AWSToolkitConfig.region)
    .build

  def apply(): Behavior[ImageBuilderCommand] = behaviour()

  private def behaviour(): Behavior[ImageBuilderCommand] =
    Behaviors.receiveMessage {
      case ListImages(replyTo) =>
        val l: ListImagesResult = awsImageBuilderClient.listImages(new ListImagesRequest().withOwner("Self"))
        replyTo ! ActionPerformed(l.getImageVersionList.asScala.map(_.toString).toList.mkString(", "))
        Behaviors.same

      case CreateNewImage(replyTo) =>
        Behaviors.same
    }
}
