package upm.gretaapp.workers

import android.content.Context
import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Write the header of a given CSV file
 *
 * @param [context] Application context
 * @param [filePath] Directory where the file will be stored
 */
fun writeCsvHeader(context: Context, filePath: String) {
    // Get external storage mount state
    val state = Environment.getExternalStorageState()
    // External storage is available and writable
    if (Environment.MEDIA_MOUNTED == state) {
        // Get the app-specific directory on external storage
        val dir = context.getExternalFilesDir(null)
        // Create a file in the external storage directory with the given name
        val file = File(dir, filePath)
        // Open a file output stream -> true to append mode
        val fileOutputStream = FileOutputStream(file, true)
        // Create an output stream writer
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)
        // BufferedWriter to make writings more efficient
        val bufferedWriter = BufferedWriter(outputStreamWriter)

        try {
            // Define header
            val header =
                "timestamp,latitude,longitude,altitude,speed_km_h," +
                        "acceleration,ax,ay,az,numSatellites\n"
            // Write the data to the file
            bufferedWriter.write(header)
        } finally {
            // Close output streams
            bufferedWriter.close()
            outputStreamWriter.close()
            fileOutputStream.close()
        }
    }
}

/**
 * Write data from a recording into a given CSV file
 *
 * @param [context] Application context
 * @param [recording] Recording where all the information is stored
 * @param [filePath] Directory where the file will be stored
 */
fun writeRecordingToCsv(context: Context, recording: Recording, filePath: String) {
    // Get external storage mount state
    val state = Environment.getExternalStorageState()
    // External storage is available and writable
    if (Environment.MEDIA_MOUNTED == state) {
        // Get the app-specific directory on external storage
        val dir = context.getExternalFilesDir(null)
        // Create a file in the external storage directory with the given name
        val file = File(dir, filePath)
        // Open a file output stream -> true to append mode
        val fileOutputStream = FileOutputStream(file, true)
        // Create an output stream writer
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)
        // BufferedWriter to make writings more efficient
        val bufferedWriter = BufferedWriter(outputStreamWriter)

        try {
            // Get data separated by ,
            val data = recording.toString()
            // Write the data to the file
            bufferedWriter.write(data)

        } finally {
            // Close output streams
            bufferedWriter.close()
            outputStreamWriter.close()
            fileOutputStream.close()
        }

    }
}

/**
 * Reads data of the state of the recording from a file
 *
 * @param [context] Application context
 * @param [filePath] Directory where the file is stored
 * @return The current state of the recording
 */
fun readState(context: Context, filePath: String): String {
    val state = Environment.getExternalStorageState()
    if (Environment.MEDIA_MOUNTED == state) {
        // Get the app-specific directory on external storage
        val dir = context.getExternalFilesDir(null)
        val file = File(dir, filePath)

        // Returns the first line of the file
        return file.readLines().first()
    }
    return ""
}