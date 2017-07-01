package org.dl4scala.examples.misc.externalerrors

import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.{NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.factory.Nd4j

/**
  * This example: shows how to train a MultiLayerNetwork where the errors come from an external source, instead
  * of using an Output layer and a labels array.
  * <p>
  * Possible use cases for this are reinforcement learning and testing/development of new algorithms.
  * <p>
  * For some uses cases, the following alternatives may be worth considering:
  * - Implement a custom loss function
  * - Implement a custom (output) layer
  * <p>
  *
  * Created by endy on 2017/7/1.
  */
object MultiLayerNetworkExternalErrors {
  def main(array: Array[String]): Unit = {
    //Create the model
    val nIn = 4
    val nOut = 3
    Nd4j.getRandom.setSeed(12345)

    val conf = new NeuralNetConfiguration.Builder()
      .seed(12345)
      .activation(Activation.TANH)
      .weightInit(WeightInit.XAVIER)
      .updater(Updater.NESTEROVS)
      .learningRate(0.1)
      .list()
      .layer(0, new DenseLayer.Builder().nIn(nIn).nOut(3).build())
      .layer(1, new DenseLayer.Builder().nIn(3).nOut(3).build())
      .backprop(true).pretrain(false)
      .build()

    val model = new MultiLayerNetwork(conf)
    model.init()

    //Calculate gradient with respect to an external error//Calculate gradient with respect to an external error

    val minibatch = 32
    val input = Nd4j.rand(minibatch, nIn)
    val output = model.output(input) //Do forward pass. Normally: calculate the error based on this

    val externalError = Nd4j.rand(minibatch, nOut)
    val p = model.backpropGradient(externalError) //Calculate backprop gradient based on error array

    //Update the gradient: apply learning rate, momentum, etc
    //This modifies the Gradient object in-place
    val gradient = p.getFirst
    val iteration = 0
    model.getUpdater.update(model, gradient, iteration, minibatch)

    //Get a row vector gradient array, and apply it to the parameters to update the model
    val updateVector = gradient.gradient
    model.params.subi(updateVector)
  }
}
