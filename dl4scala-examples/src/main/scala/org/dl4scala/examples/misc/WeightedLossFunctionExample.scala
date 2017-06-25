package org.dl4scala.examples.misc

import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.impl.LossMCXENT

/**
  * Idea with a weighted loss function: it allows us to add a weight to the outputs.
  * For example, if we have 3 classes, and we consider predictions of the 3rd class to be more important, we might use
  * a weight array of [0.5,0.5,1.0]. This means that the first 2 outputs will contribute only half as much as they
  * normally would to the loss/score.
  * Note that the weights don't (and shouldn't necessarily) sum to 1.0 - and that a weight array of all 1s is equivalent
  * to having no weight array at all.
  * If the use case is dealing with class imbalance for classification, use smaller weights for frequently occurring
  * classes, and 1.0 or larger weights for infrequently occurring classes.
  * Training and the data pipelines when using weighted loss functions are identical to not using them, so this example
  * shows only how to configure the weighting.
  *
  * Created by endy on 2017/6/25.
  */
object WeightedLossFunctionExample {
  def main(args: Array[String]): Unit = {
    val numInputs = 4
    val numClasses = 3 //3 classes for classification

    // Create the weights array. Note that we have 3 output classes, therefore we have 3 weights
    val weightsArray = Nd4j.create(Array[Double](0.5, 0.5, 1.0))

    val conf = new NeuralNetConfiguration.Builder()
      .activation(Activation.RELU)
      .weightInit(WeightInit.XAVIER)
      .learningRate(0.1)
      .list()
      .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(5)
        .build())
      .layer(1, new DenseLayer.Builder().nIn(5).nOut(5)
        .build())
      .layer(2, new OutputLayer.Builder()
        .lossFunction(new LossMCXENT(weightsArray))     // *** Weighted loss function configured here ***
        .activation(Activation.SOFTMAX)
        .nIn(5).nOut(numClasses).build())
      .backprop(true).pretrain(false)
      .build()

    //Initialize and use the model as before
    val model = new MultiLayerNetwork(conf)
    model.init()
  }
}
