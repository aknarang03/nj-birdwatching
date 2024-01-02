package edu.monmouth.cs250.s1328134.njbirdwatching

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.monmouth.cs250.s1328134.njbirdwatching.databinding.ActivityBirdDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class BirdDetailActivity : AppCompatActivity(), FetchCompletionListener {

    private lateinit var binding : ActivityBirdDetailBinding
    var id: Int = 0
    lateinit var bird: Bird
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_PICK_IMAGE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirdDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#b6e5b0")))
        displayInfo()
    }

    private fun displayInfo() {
        // get bird object from intent
        id = intent.getIntExtra("id",0)
        bird = NJBirdModel.getBirdObject(id)
        // display data
        binding.comNameText.text = bird.comName
        binding.sciNameText.text = bird.sciName
        binding.familyComNameText.text = bird.familyComName
        binding.familySciNameText.text = bird.familySciName
        binding.seenSwitch.isChecked = bird.seen
        setImage()
        title = "Info: " + bird.comName
    }

    // set image view
    private fun setImage() {
        val imageFile = getFile()
        if (imageFile.exists()) {
            val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath) // encode image into bitmap
            binding.imageView.setImageBitmap(imageBitmap)
        } else {
            binding.imageView.setImageResource(R.drawable.blank_photo)
        }
    }

    // for saving image once captured
    private fun saveImage(image: Bitmap) {
        val file = getFile()
        val output = file.outputStream()
        image.compress(Bitmap.CompressFormat.PNG,85,output)
        output.flush()
        output.close()
    }

    // switch functionality
    fun onSeenSwitched (view: View) {
        bird.seen = binding.seenSwitch.isChecked
        MainActivity.prefs!!.putPref(bird)
    }

    // camera button functionality
    fun onCameraButtonPress (view: View) {
        dispatchTakePictureIntent()
    }

    // upload button functionality
    fun onUploadButtonPress (view: View) {
        dispatchUploadIntent()
    }

    // search button functionality
    fun onSearchButtonPress (view: View) {
        SearchService.fetchImages(bird.comName.replace(" ","%20"),this)
    }

    // go to filtered map
    fun onGoButtonPress (view: View) {
        RecentSightingsService.filterBirds(bird.comName)
        toMap = true
        finish()
    }

    // go back to item fragment
    fun onBackButtonPress (view: View) {
        toMap = false
        finish()
    }

    // open camera
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Log.i("dispatchTakePictureIntent",e.message!!)
        }
    }

    // open gallery
    private fun dispatchUploadIntent() {
        val uploadIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        try {
            startActivityForResult(uploadIntent, REQUEST_PICK_IMAGE)
        } catch (e: ActivityNotFoundException) {
            Log.i("dispatchTakePictureIntent",e.message!!)
        }
    }

    // on camera / upload activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imageView.setImageBitmap(imageBitmap)
            saveImage(imageBitmap)
        }
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            val imageUri: Uri? = data!!.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            binding.imageView.setImageBitmap(imageBitmap)
            saveImage(imageBitmap)
        }
    }

    // for FetchCompletionListener: runs when SearchService gets image
    override fun fetchCompleted(
        getBirds: String,
        completionResponse: Boolean,
        dataResponse: Boolean
    ) {
        runOnUiThread {
            if (completionResponse && dataResponse) {

                Log.i("main","fetch completed")
                val birdImg : String = SearchService.firstImage
                Log.i("fetchCompleted",birdImg)

                lifecycleScope.launch(Dispatchers.IO) {

                    // get image from url
                    val imageUrl = URL(birdImg)
                    val httpConnection = imageUrl.openConnection() as HttpURLConnection
                    httpConnection.doInput = true
                    httpConnection.connect()
                    val inputStream = httpConnection.inputStream

                    val bitmapImage = BitmapFactory.decodeStream(inputStream)
                    saveImage(bitmapImage)

                    // set image
                    launch(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(bitmapImage)
                    }

                }

            } else {
                // response or data fetch failed
                Log.i("main","fetch failed")
            }

        }
    }

    // get bird image file
    private fun getFile(): File {
        val filename = bird.comName + "_image.jpg"
        return File(getAppSpecificAlbumStorageDir(this, "photos"), filename)
    }

    // get image storage directory
    private fun getAppSpecificAlbumStorageDir(context: Context, albumName: String): File? {
        // get pictures directory inside app specific directory
        val file = File(context.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES), albumName)
        if (!file?.mkdirs()!!) {
            Log.e("get storage dir", "Directory not created")
        }
        return file
    }

    companion object {
        var toMap : Boolean = false
    }

}