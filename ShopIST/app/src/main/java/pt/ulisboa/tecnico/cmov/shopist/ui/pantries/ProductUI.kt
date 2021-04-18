package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ulisboa.tecnico.cmov.shopist.BarcodeScannerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.io.File
import java.io.FileOutputStream
import java.util.*


class ProductUI : Fragment() {

    private lateinit var root: View
    private lateinit var product: Product

    private lateinit var folder: File

    companion object {
        const val TAG = "${ShopIST.TAG}.productUI"
        const val ARG_PRODUCT_ID = "productId"
        const val GET_BARCODE_PRODUCT = 0
        const val IMAGE_CAMERA = 1
        const val IMAGE_PICK_GALLERY = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val globalData = requireActivity().applicationContext as ShopIST
        arguments?.let {
            val productId = UUID.fromString(it.getString(ARG_PRODUCT_ID))

            globalData.getProduct(productId)?.let { p ->
                product = p
            }
        }
        setHasOptionsMenu(true)

        folder = globalData.getImageFolder()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_product, container, false)

        // Set product titles
        root.findViewById<TextView>(R.id.productName).text = product.name
        root.findViewById<ImageView>(R.id.imageButton).setOnClickListener {
            selectImage()
        }

        // Set images
        if (product.images.size > 0) {
            showImages()
        }
        return root
    }

    private fun showImages() {
        // TODO: add multiple images
        val index = product.getLastImageIndex()
        val imageFileName = "${product.uuid}_$index${ShopIST.IMAGE_EXTENSION}"
        val imagePath = File(folder, imageFileName)

        val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)

        root.findViewById<ImageView>(R.id.productImage).setImageBitmap(imageBitmap)
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>(getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel))
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_new_photo))
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                getString(R.string.take_photo) -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, IMAGE_CAMERA)
                }
                getString(R.string.choose_from_gallery) -> {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, IMAGE_PICK_GALLERY)
                }
                getString(R.string.cancel) -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun readBarcode() {
        val intent = Intent(activity?.applicationContext, BarcodeScannerActivity::class.java)
        startActivityForResult(intent, GET_BARCODE_PRODUCT)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        TopBarController.optionsMenu(
            menu, requireActivity(), product.name,
            listOf(TopBarItems.Edit, TopBarItems.ScanBarcode)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_scan_barcode -> {
                readBarcode()
            }
            R.id.action_edit -> {
                findNavController().navigate(
                    R.id.action_nav_view_product_to_nav_create_product,
                    bundleOf(
                        CreateProductUI.ARG_PRODUCT_ID to product.uuid.toString()
                    )
                )
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun storeImage(bitmap: Bitmap): String? {
        val index = product.getLastImageIndex() + 1
        val imageFileName = "${product.uuid}_$index${ShopIST.IMAGE_EXTENSION}"
        val imagePath = File(folder, imageFileName)

        try {
            val fos = FileOutputStream(imagePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            // Add image to product
            product.addImage(imageFileName)

            // Store information about the image
            (requireActivity().applicationContext as ShopIST).savePersistent()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return null
        }
        return imageFileName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                IMAGE_CAMERA -> {
                    if (data !== null) {
                        val bitmap = data.extras?.get("data") as Bitmap
                        // Store the image in product and device
                        val imageFileName = storeImage(bitmap)

                        if (imageFileName !== null) {
                            // Set the image in ImageView
                            root.findViewById<ImageView>(R.id.productImage).setImageBitmap(bitmap)
                        }
                    }
                }
                IMAGE_PICK_GALLERY -> {
                    if (data !== null && data.data !== null && data.data!!.path !== null) {
                        val uri = data.data!!
                        val imageStream = requireContext().contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(imageStream)

                        // Store the image in product and device
                        val imageFileName = storeImage(bitmap)

                        if (imageFileName !== null) {
                            // Set the image in ImageView
                            root.findViewById<ImageView>(R.id.productImage).setImageBitmap(bitmap)
                        }
                    }
                }
                GET_BARCODE_PRODUCT -> {
                    if (data !== null) {
                        val barcode = data.getStringExtra(BarcodeScannerActivity.BARCODE)
                        product.barcode = barcode

                        val globalData = activity?.applicationContext as ShopIST
                        globalData.savePersistent()

                        Toast.makeText(context, String.format(
                            getString(R.string.barcode_read),
                            barcode
                        ), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun cancel() {
        findNavController().popBackStack()
    }

    private fun saveAndReturn() {
        (requireActivity().applicationContext as ShopIST).savePersistent()
        cancel()
    }
}
