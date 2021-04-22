package pt.ulisboa.tecnico.cmov.shopist.ui.products

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
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
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.PriceByStoreDialog
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.io.File
import java.io.FileOutputStream
import java.util.*


class ProductUI : Fragment() {

    private lateinit var root: View
    private lateinit var menuRoot: Menu
    private lateinit var product: Product
    private lateinit var stores: List<Store>
    private var priceStore: Store? = null

    private lateinit var imageFolder: File
    private lateinit var localImageFolder: File

    companion object {
        val TAG = ProductUI::class.qualifiedName
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
                stores = p.stores.toList()
            }
        }
        setHasOptionsMenu(true)

        imageFolder = globalData.getImageFolder()
        localImageFolder = globalData.getLocalImageFolder()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_product, container, false)

        // Set product titles and buttons
        root.findViewById<TextView>(R.id.productName).text = product.name
        root.findViewById<ImageView>(R.id.imageButton).setOnClickListener {
            selectImage()
        }
        root.findViewById<Button>(R.id.addPriceButton).setOnClickListener {
            showDialogAddPrice()
        }
        root.findViewById<Button>(R.id.seePricesButton).setOnClickListener {
            showDialogPrices()
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        val globalData = requireActivity().applicationContext as ShopIST
        setEnableButtons(globalData.isAPIConnected)

        // Update prices from server for all stores, for this product in specific
        API.getInstance(requireContext()).getPricesForProduct(product, product.stores.mapNotNull { it.location }, { res ->
            // TODO: Show these prices
            res.forEach {
                globalData.getClosestStore(it.location)?.let { s ->
                    product.setPrice(s, it.price)
                }
            }
            globalData.savePersistent()
        }, {
        })

        // Update images
        showImages()
    }

    private fun setEnableButtons(enabled: Boolean) {
        val hasBarcode = product.barcode !== null
        if (this::menuRoot.isInitialized) {
            TopBarController.setSharedOptions(menuRoot, !hasBarcode || enabled)
        }
        root.findViewById<ImageButton>(R.id.imageButton).isEnabled = !hasBarcode || enabled
        root.findViewById<Button>(R.id.addPriceButton).isEnabled = !hasBarcode || enabled
        root.findViewById<Button>(R.id.seePricesButton).isEnabled = !hasBarcode || enabled
    }

    private fun showImages() {
        val globalData = requireActivity().applicationContext as ShopIST

        var hasImage = false

        // TODO: add multiple images
        if (product.barcode !== null) {
            // If not connected insert at least a local image
            if (!globalData.isAPIConnected) {
                if (product.images.size > 0) {
                    val imageFileName = product.getLastImageName()
                    val imagePath = File(globalData.getImageFolder(), imageFileName)
                    val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
                    root.findViewById<ImageView>(R.id.productImage).setImageBitmap(imageBitmap)
                }
            }

            // Get all images from cache (and then local if not available)
            API.getInstance(requireContext()).getProductImages(product, { imageIds ->
                imageIds.forEach { id ->
                    // Get image from cache
                    if (!hasImage) {
                        globalData.imageCache.getAsImage(UUID.fromString(id), { image ->
                            root.findViewById<ImageView>(R.id.productImage).setImageBitmap(image)
                        }, { // Ignore
                        })
                        hasImage = true
                    }
                }
            }, {
            })
        } else if (product.images.size > 0) {
            val imageFileName = product.getLastImageName()
            val imagePath = File(imageFolder, imageFileName)

            val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)

            root.findViewById<ImageView>(R.id.productImage).setImageBitmap(imageBitmap)
        }

    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>(
            getString(R.string.take_photo),
            getString(R.string.choose_from_gallery)
        )
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_new_photo))
        builder.setItems(options) { _, item ->
            when (options[item]) {
                getString(R.string.take_photo) -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, IMAGE_CAMERA)
                }
                getString(R.string.choose_from_gallery) -> {
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(intent, IMAGE_PICK_GALLERY)
                }
            }
        }
        builder.show()
    }

    private fun readBarcode() {
        val intent = Intent(activity?.applicationContext, BarcodeScannerActivity::class.java)
        startActivityForResult(intent, GET_BARCODE_PRODUCT)
    }

    private fun showDialogAddPrice() {
        // Inflate layout for dialog
        val inflater = requireActivity().layoutInflater
        val alert = AlertDialog.Builder(requireContext())
        alert.setTitle(getString(R.string.product_add_price))

        val alertRoot = inflater.inflate(R.layout.dialog_price_product, null)
        alert.setView(alertRoot)
        alert.setPositiveButton(getString(R.string.ok), null)
        alert.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        // Create spinner
        val spinner: Spinner = alertRoot.findViewById(R.id.store_spinner)

        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            stores.map { s -> s.name }
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val globalData = requireActivity().applicationContext as ShopIST

        // Set current store if has location
        globalData.currentLocation?.let {
            globalData.getClosestStore(it)?.let { s ->
                val idx = stores.indexOf(s)
                if (idx >= 0) {
                    spinner.setSelection(idx)
                }
            }
        }

        // On store selected
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                priceStore = stores[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Override Success button to make sure the user meets the conditions
        val dialog = alert.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val priceText = alertRoot.findViewById<EditText>(R.id.productPrice).text.toString()
                try {
                    if (priceText.isEmpty() || priceText.toDouble() <= 0) {
                        Toast.makeText(
                            context,
                            getString(R.string.price_above_zero),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        context,
                        getString(R.string.price_above_zero),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(ShopIST.TAG, "Invalid number inserted: \'$priceText\'")
                    return
                }

                val price = priceText.toDouble()
                if (priceStore !== null && product.barcode !== null) {
                    // Set price and send to server
                    product.setPrice(priceStore!!, price)

                    API.getInstance(requireContext()).submitPriceProduct(price, product,
                        priceStore!!, {
                        Log.d(ShopIST.TAG, "Product price sent")
                    }, {
                        Log.d(ShopIST.TAG, "Could not send price")
                    })

                    (requireActivity().applicationContext as ShopIST).savePersistent()
                    dialog.dismiss()
                } else if (priceStore == null) {
                    Toast.makeText(
                        context,
                        getString(R.string.first_choose_store),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Set local price
                    product.setPrice(priceStore!!, price)
                    dialog.dismiss()
                }
            }
        })
    }

    private fun showDialogPrices() {
        val dialog = PriceByStoreDialog(this, product)
        dialog.show()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menuRoot = menu
        val globalData = requireActivity().applicationContext as ShopIST
        setEnableButtons(globalData.isAPIConnected)

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

    private fun storeImage(bitmap: Bitmap, callback: (imageFilename: String?) -> Unit) {

        if (product.barcode !== null) {
            // Send to server
            API.getInstance(requireContext()).postProductImage(product, bitmap, { imageId ->
                val imageFileName = "${imageId}${ShopIST.IMAGE_EXTENSION}"
                val imagePath = File(localImageFolder, imageFileName)

                FileOutputStream(imagePath).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                product.addImage(imageId)

                // Store information about the image
                (requireActivity().applicationContext as ShopIST).savePersistent()
                callback(imageFileName)
            }, {

            })
        } else {
            val uuid = UUID.randomUUID()
            val imageFileName = "${uuid}${ShopIST.IMAGE_EXTENSION}"
            val imagePath = File(localImageFolder, imageFileName)

            try {
                val fos = FileOutputStream(imagePath)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()

                // Add image to product
                product.addImage(uuid.toString())

                // Store information about the image
                (requireActivity().applicationContext as ShopIST).savePersistent()
                callback(imageFileName)
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                callback(null)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                IMAGE_CAMERA -> {
                    if (data !== null) {
                        val bitmap = data.extras?.get("data") as Bitmap
                        // Store the image in product and device
                        storeImage(bitmap) {
                            if (it !== null) {
                                // Set the image in ImageView
                                root.findViewById<ImageView>(R.id.productImage)
                                    .setImageBitmap(bitmap)
                            }
                        }
                    }
                }
                IMAGE_PICK_GALLERY -> {
                    if (data !== null && data.data !== null && data.data!!.path !== null) {
                        val uri = data.data!!
                        val imageStream = requireContext().contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(imageStream)

                        // Store the image in product and device
                        storeImage(bitmap) {
                            if (it !== null) {
                                // Set the image in ImageView
                                root.findViewById<ImageView>(R.id.productImage)
                                    .setImageBitmap(bitmap)
                            }
                        }
                    }
                }
                GET_BARCODE_PRODUCT -> {
                    if (data !== null) {
                        val barcode = data.getStringExtra(BarcodeScannerActivity.BARCODE)
                        product.barcode = barcode

                        val globalData = activity?.applicationContext as ShopIST
                        globalData.savePersistent()

                        Toast.makeText(
                            context, String.format(
                                getString(R.string.barcode_read),
                                barcode
                            ), Toast.LENGTH_SHORT
                        ).show()
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