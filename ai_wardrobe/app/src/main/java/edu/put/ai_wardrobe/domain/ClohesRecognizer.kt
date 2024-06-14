package edu.put.ai_wardrobe.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream


class ClothesRecognizer(private val context: Context) {

    private val module: Module

    init {
        val assetManager = context.resources.assets
        val modelFileName = "fashion-recognizer.pt"
        val modelFile = File(context.filesDir, modelFileName)
        if (!modelFile.exists()) {
            copyAssetToFile(context, modelFileName, modelFile)
        }
        val inputStream = assetManager.open(modelFileName)
        try {
            // load model from  assets
            module = LiteModuleLoader.load(modelFile.absolutePath)
        } finally {
            inputStream.close()
        }
    }

    private fun copyAssetToFile(context: Context, assetName: String, outFile: File) {
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(outFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun classifyImage(bitmap: Bitmap): String {

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true)
        val oneDimensionMatrix = bitmapToGrayscaleFloatArray(resizedBitmap)

        val inputTensor = Tensor.allocateFloatBuffer(28*28)
        inputTensor.put(oneDimensionMatrix)

        val outputTensor = module.forward(IValue.from(Tensor.fromBlob(inputTensor, longArrayOf(1,1,28,28)))).toTensor()
        val scores = outputTensor.dataAsFloatArray
        val labels = listOf("T-shirt", "Trouser", "Pullover", "Dress", "Coat", "Sandal", "Shirt", "Sneaker", "Bag", "Ankle boot")

        // Find the index with the highest score
        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: -1
        val resultLabel = if (maxIndex != -1) labels[maxIndex] else "Unknown"

        // Example of logging outputs
        Log.d("classifyImage", "Input bitmap size: ${resizedBitmap.width} x ${resizedBitmap.height}")
        Log.d("classifyImage", "Input tensor: ${inputTensor}")
        Log.d("classifyImage", "Output tensor: ${outputTensor.dataAsFloatArray.asList()}")
        Log.d("classifyImage", "Predicted label: $resultLabel")


        return resultLabel

    }

    // to change 3 dim bitmap to array
    fun bitmapToGrayscaleFloatArray(bitmap: Bitmap): FloatArray {
        val floatArray = FloatArray(bitmap.width * bitmap.height)
        var index = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                val grayValue = (0.299 * red + 0.587 * green + 0.114 * blue).toFloat()

                val normalizedGray = grayValue / 255.0f


                floatArray[index++] = normalizedGray
            }
        }

        return floatArray
    }




}