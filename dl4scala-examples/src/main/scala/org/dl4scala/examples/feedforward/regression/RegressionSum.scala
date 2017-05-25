package org.dl4scala.examples.feedforward.regression


import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Created by endy on 2017/5/25.
  */
object RegressionSum extends App{
  val logger: Logger = LoggerFactory.getLogger(RegressionSum.getClass)

  // Random number generator seed, for reproducability
  val seed = 12345
  // Number of iterations per minibatch
  val iterations = 1
  // Number of epochs (full passes of the data)
  val nEpochs = 200
  // Number of data points
  val nSamples = 1000
  // Batch size: i.e., each epoch has nSamples/batchSize parameter updates
  val batchSize = 100
  // Network learning rate
  val learningRate = 0.01
  // The range of the sample data, data in range (0-1 is sensitive for NN, you can try other ranges and see how it effects the results
  // also try changing the range along with changing the activation function
  var MIN_RANGE = 0
  var MAX_RANGE = 3

  val rng = new Random(seed)

  // Generate the training data//Generate the training data

  val iterator = getTrainingData(batchSize, rng)

  // Create the network
  val numInput = 2
  val numOutputs = 1
  val nHidden = 10

  val net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
    .seed(seed)
    .iterations(iterations)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .learningRate(learningRate)
    .weightInit(WeightInit.XAVIER)
    .updater(Updater.NESTEROVS).momentum(0.9)
    .list()
    .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(nHidden)
      .activation(Activation.TANH)
      .build())
    .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
      .activation(Activation.IDENTITY)
      .nIn(nHidden).nOut(numOutputs).build())
    .pretrain(false).backprop(true).build())


  net.init()
  net.setListeners(new ScoreIterationListener(1))

  (0 until nEpochs).foreach{ _ =>
    iterator.reset()
    net.fit(iterator)
  }


  // Test the addition of 2 numbers (Try different numbers here)
  val input: INDArray = Nd4j.create(Array[Double](0.111111, 0.3333333333333), Array[Int](1, 2))
  val out: INDArray = net.output(input, false)

  logger.info(out.toString)


  private def getTrainingData(batchSize: Int, rand: Random): DataSetIterator = {
    val sum = new Array[Double](nSamples)
    val input1 = new Array[Double](nSamples)
    val input2 = new Array[Double](nSamples)

    (0 until nSamples).foreach{i =>
      input1(i) = MIN_RANGE + (MAX_RANGE - MIN_RANGE) * rand.nextDouble
      input2(i) = MIN_RANGE + (MAX_RANGE - MIN_RANGE) * rand.nextDouble
      sum(i) = input1(i) + input2(i)
    }

    val inputNDArray1 = Nd4j.create(input1, Array[Int](nSamples, 1))
    val inputNDArray2 = Nd4j.create(input2, Array[Int](nSamples, 1))
    val inputNDArray = Nd4j.hstack(inputNDArray1, inputNDArray2)
    val outPut = Nd4j.create(sum, Array[Int](nSamples, 1))
    val dataSet = new DataSet(inputNDArray, outPut)
    val listDs = dataSet.asList

    val shuffle = Random.shuffle(listDs.asScala)
    new ListDataSetIterator(shuffle.asJavaCollection, batchSize)
  }
}
