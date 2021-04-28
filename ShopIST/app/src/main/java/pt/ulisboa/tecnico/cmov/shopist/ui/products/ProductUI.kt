package pt.ulisboa.tecnico.cmov.shopist.ui.products

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import pt.ulisboa.tecnico.cmov.shopist.BarcodeScannerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ConfirmationDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.PriceByStoreDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.RatingDialog
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ProductUI : Fragment() {

    private lateinit var root: View
    private lateinit var menuRoot: Menu
    private lateinit var product: Product
    private lateinit var stores: List<Store>
    private var priceStore: Store? = null
    private lateinit var globalData: ShopIST

    private lateinit var imageFolder: File
    private lateinit var localImageFolder: File
    private var photoFile: File? = null

    private var personalRating: Int? = null

    companion object {
        const val TAG = "${ShopIST.TAG}.productUI"
        const val ARG_PRODUCT_ID = "productId"
        const val GET_BARCODE_PRODUCT = 0
        const val IMAGE_CAMERA = 1
        const val IMAGE_PICK_GALLERY = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalData = requireActivity().applicationContext as ShopIST
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
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_product, container, false)

        // Set product titles and buttons
        root.findViewById<TextView>(R.id.productName).text = product.name
        // FIXME: Improve this layout
        val barcodeShown = if (product.barcode !== null) product.barcode else getString(R.string.not_available)
        root.findViewById<TextView>(R.id.barcodeValue).text =
            String.format(getString(R.string.barcode_value), barcodeShown)
        root.findViewById<ImageView>(R.id.imageButton).setOnClickListener {
            selectImage()
        }
        root.findViewById<Button>(R.id.addPriceButton).setOnClickListener {
            showDialogAddPrice()
        }
        root.findViewById<Button>(R.id.seePricesButton).setOnClickListener {
            showDialogPrices()
        }
        root.findViewById<LinearLayout>(R.id.product_rating).setOnClickListener {
            showDialogRating()
        }

        prepareChart()

        return root
    }

    override fun onResume() {
        super.onResume()

        // setEnableButtons(globalData.isAPIConnected)

        // Update prices from server for all stores, for this product in specific
        API.getInstance(requireContext()).getPricesForProduct(
            product,
            product.stores.mapNotNull { it.location },
            { res ->
                res.forEach {
                    globalData.getClosestStore(it.location)?.let { s ->
                        product.setPrice(s, it.price)
                    }
                }
                globalData.savePersistent()
            },
            {
            })

        if (product.barcode != null) {
            updateRatings()
        }

        // Update images
        showImages()
    }

    private fun updateRatings() {
        API.getInstance(requireContext()).getProductRating(
            product.barcode!!, globalData.deviceId
        ) { ratings, personalRating ->
            val totalRatings = ratings.values.sum()

            val rating =
                ratings.entries.fold(0f) { acc, (stars, num) -> acc + stars * num } / totalRatings

            val text = if (totalRatings > 0) String.format("%.1f", rating) else "---"
            root.findViewById<TextView>(R.id.rating_text).text = text
            prepareData(ratings)
            this.personalRating = personalRating
        }
    }

    private fun prepareChart() {
        val chart = root.findViewById<HorizontalBarChart>(R.id.histogram)
        chart.setDrawValueAboveBar(true)
        chart.setTouchEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setFitBars(true)
        chart.legend.isEnabled = false
        chart.description.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = ChartAxisFormatter()
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        chart.axisRight.isEnabled = false
        chart.axisRight.axisMinimum = 0f
        chart.axisLeft.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
    }

    private fun prepareData(ratings: Map<Int, Int>) {
        val barWidth = 0.75f
        val values = ratings.entries
            .sortedByDescending { it.key }
            .map { BarEntry(it.key.toFloat(), it.value.toFloat()) }

        val set = BarDataSet(values, "")
        set.setDrawIcons(false)
        set.valueFormatter = ChartAxisFormatter()
        set.valueTextSize = 12f
        set.setDrawIcons(false)

        val colors = mutableListOf(
            Color.rgb(121, 201, 161),
            Color.rgb(174, 216, 136),
            Color.rgb(255, 217, 53),
            Color.rgb(255, 178, 53),
            Color.rgb(255, 140, 90),
        )
        set.colors = colors

        val chart = root.findViewById<HorizontalBarChart>(R.id.histogram)
        chart.data = BarData(set)
        chart.data.barWidth = barWidth
        chart.invalidate()
    }

    inner class ChartAxisFormatter : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    private fun setEnableButtons(enabled: Boolean) {
        val hasBarcode = product.barcode !== null
        if (this::menuRoot.isInitialized) {
            TopBarController.setSharedOptions(menuRoot, !hasBarcode || enabled)
        }
        TopBarController.setOnlineOptions(menuRoot, enabled)
        root.findViewById<ImageButton>(R.id.imageButton).isEnabled = !hasBarcode || enabled
        root.findViewById<Button>(R.id.addPriceButton).isEnabled = !hasBarcode || enabled
        root.findViewById<Button>(R.id.seePricesButton).isEnabled = !hasBarcode || enabled
        root.findViewById<TextView>(R.id.product_rating_disabled).visibility = if (hasBarcode && enabled) View.GONE else View.VISIBLE
        root.findViewById<LinearLayout>(R.id.product_rating).visibility = if (hasBarcode && enabled) View.VISIBLE else View.GONE
    }

    private fun showImages() {
        val layout = root.findViewById<LinearLayout>(R.id.productImageLayout)
        layout.removeAllViews()

        if (product.images.size > 0) {
            root.findViewById<ImageView>(R.id.productImage).visibility = View.GONE
            root.findViewById<HorizontalScrollView>(R.id.horizontal_scroll).visibility = View.VISIBLE
            product.images.forEachIndexed { i: Int, s: String ->
                val uuid = UUID.fromString(s)
                globalData.imageCache.getAsImage(uuid, {
                    val imageView = ImageView(requireContext())
                    imageView.id = i
                    imageView.setImageBitmap(it)
                    imageView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    layout.addView(imageView)
                }, { })
            }
        } else {
            root.findViewById<ImageView>(R.id.productImage).visibility = View.VISIBLE
            root.findViewById<HorizontalScrollView>(R.id.horizontal_scroll).visibility = View.GONE
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

                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    photoFile = getPhotoFileUri(timeStamp)
                    val fileProvider = FileProvider.getUriForFile(requireContext(),
                        "pt.ulisboa.tecnico.cmov.shopist.fileprovider",
                        photoFile!!
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
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

    private fun getPhotoFileUri(fileName: String): File {
        val mediaStorageDir =
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory for temp photo")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
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

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Override Success button to make sure the user meets the conditions
        val dialog = alert.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(object :
            View.OnClickListener {
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
                    Log.d(TAG, "Invalid number inserted: \'$priceText\'")
                    return
                }

                val price = priceText.toDouble()
                if (priceStore !== null && product.barcode !== null) {
                    // Set price and send to server
                    product.setPrice(priceStore!!, price)

                    API.getInstance(requireContext()).submitPriceProduct(price, product,
                        priceStore!!, {
                            Log.d(TAG, "Product price sent")
                        }, {
                            Log.d(TAG, "Could not send price")
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

    private fun showDialogRating() {
        val dialog = RatingDialog(this, personalRating) { rating ->
            API.getInstance(requireContext()).submitProductRating(
                product.barcode!!,
                globalData.deviceId,
                rating
            ) {
                personalRating = rating
                updateRatings()
            }
        }
        dialog.show()
    }

    private fun shareProduct() {
        API.getInstance(requireContext()).postProduct(product, {
            // Stores product as shared
            product.share()
            globalData.addProduct(product)
            globalData.savePersistent()

            // Share code with the user
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT, getString(R.string.share_product_message).format(
                        product.name, ShopIST.createUri(product)
                    )
                )
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }, {
            Toast.makeText(context, getString(R.string.error_getting_link), Toast.LENGTH_SHORT)
                .show()
        })
    }

    private fun confirmDeleteProduct() {
        ConfirmationDialog(
            requireContext(),
            getString(R.string.confirm_product_delete),
            {
                deleteProduct()
                findNavController().popBackStack()
            }, {}
        )
    }

    private fun deleteProduct() {
        val globalData = requireContext().applicationContext as ShopIST
        if (product.isShared) {
            val pantriesToUpdate = globalData.getPantriesWithProduct(product.uuid)

            globalData.removeProduct(product.uuid)
            // TODO: What happens if the server does not respond? the pantry stays without the item?
            pantriesToUpdate.forEach { pantryList ->
                API.getInstance(requireContext()).updatePantry(pantryList)
            }
        } else {
            globalData.removeProduct(product.uuid)
            globalData.savePersistent()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menuRoot = menu
        setEnableButtons(globalData.isAPIConnected)

        TopBarController.optionsMenu(
            menu, requireActivity(), product.getTranslatedName(),
            listOf(TopBarItems.Edit, TopBarItems.ScanBarcode, TopBarItems.Share, TopBarItems.Delete)
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
            R.id.action_share -> shareProduct()
            R.id.action_delete -> confirmDeleteProduct()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun storeImage(bitmap: Bitmap, callback: (imageFilename: String?) -> Unit) {

        if (product.barcode !== null) {
            val id = UUID.randomUUID()
            val imageFileName = "${id}${ShopIST.IMAGE_EXTENSION}"
            val imagePath = File(localImageFolder, imageFileName)

            FileOutputStream(imagePath).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            product.addImage(id.toString())

            // Send to server
            API.getInstance(requireContext()).postProductImage(product, bitmap, id, { imageId ->
                Log.d(TAG, "Image: $imageId")
            }, {
                // TODO: Cancel adding
            })

            // Store information about the image
            val globalData = requireActivity().applicationContext as ShopIST
            globalData.imageCache.putImage(id, bitmap, true)
            globalData.savePersistent()
            callback(imageFileName)
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
                val globalData = requireActivity().applicationContext as ShopIST
                globalData.imageCache.putImage(uuid, bitmap, true)
                globalData.savePersistent()
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
                    data?.let {
                        // Store the image in product and device
                        val takenImage = BitmapFactory.decodeFile(photoFile?.absolutePath)
                        storeImage(takenImage) {
                            it?.let {
                                showImages()
                            }
                        }
                    }
                }
                IMAGE_PICK_GALLERY -> {
                    data?.data?.path?.let {
                        val uri = data.data!!
                        val imageStream = requireContext().contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(imageStream)

                        // Store the image in product and device
                        storeImage(bitmap) {
                            it?.let {
                                showImages()
                            }
                        }
                    }
                }
                GET_BARCODE_PRODUCT -> {
                    data?.let {
                        val barcode = data.getStringExtra(BarcodeScannerActivity.BARCODE)

                        product.barcode = barcode
                        root.findViewById<TextView>(R.id.barcodeValue).text =
                            String.format(getString(R.string.barcode_value), barcode)

                        globalData.savePersistent()

                        API.getInstance(requireContext()).postProduct(product, {
                            Log.d(TAG, "Product sent for update")
                        }, {
                            Log.d(TAG, "Could not send product")
                        })

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
}
