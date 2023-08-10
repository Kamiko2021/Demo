package com.smartherd.productapp

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartherd.productapp.adapters.ItemListAdapter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var addBtn : FloatingActionButton
    private lateinit var recv : RecyclerView
    private lateinit var searchEditext : EditText
    private lateinit var itemList:ArrayList<Items>
    private lateinit var itemList_temp:ArrayList<Items>
    private lateinit var itemAdapter:ItemListAdapter


    private lateinit var SharedPreferences : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var gson : Gson
    lateinit var Itemname:String
    lateinit var Itemprice:String
    lateinit var Itemquantity:String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        addBtn = findViewById(R.id.addItemBtn)
        recv =findViewById(R.id.itemListRecyclerView)
        searchEditext = findViewById(R.id.searchEdtxt)


        itemList = ArrayList()
        val infilter = LayoutInflater.from(this@MainActivity)

        itemAdapter = ItemListAdapter(this@MainActivity, itemList)
        recv.layoutManager = LinearLayoutManager(this@MainActivity)
        recv.adapter = itemAdapter

        itemAdapter.setOnItemClickListener(object : ItemListAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
               val selectedItem=itemList[position]
                val name=selectedItem.itemName.toString()
                val price = selectedItem.productPrice.toString()
                val quantity = selectedItem.productQuantity.toString()
                val update = infilter.inflate(R.layout.add_item, null)
                if (update.parent !=null) {
                    (update.parent as ViewGroup).removeView(update)
                }
                Itemname = name
                Itemprice = price
                Itemquantity = quantity
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val currentDate = LocalDateTime.now().format(formatter)
                saveItem(update, "update",position,selectedItem.dateCreated.toString(),currentDate)
                loadData()
            }

        })
        val save = infilter.inflate(R.layout.add_item, null)
        addBtn.setOnClickListener {
             if (save.parent !=null) {
                 (save.parent as ViewGroup).removeView(save)
             }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val currentDate = LocalDateTime.now().format(formatter)
            saveItem(save,"save",0,currentDate) }

//        searchEditext.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                itemAdapter.filterItems(searchEditext.text.toString()) // Call the filterItems function
//            }
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//        })
        searchEditext.doAfterTextChanged {
            itemAdapter.filterItems(searchEditext.text.toString())
            Log.d("search input: ", searchEditext.text.toString())
        }

        loadData()
    }

    // check if it is double or integer..
    fun isInteger(str:String): Boolean {
        return try {
            str.toInt()
            true
        } catch (e: NumberFormatException){
            false
        }
    }
    fun isDouble (str: String): Boolean {
        return try {
            str.toDouble()
            true
        }catch (e: NumberFormatException){
            false
        }
    }




    // showing addItem Modal....
    private fun saveItem(view: View, method:String?=null, position: Int, createdDate:String?=null,updatedDate:String?=null){


        // EditTexts for adding data
        val itemName = view.findViewById<EditText>(R.id.itemNameEditext)
        val itemPrice = view.findViewById<EditText>(R.id.itemPriceEditext)
        val itemQuantity = view.findViewById<EditText>(R.id.itemQuantityEditext)


        // Validation textviews
        val header = view.findViewById<TextView>(R.id.headerName)
        val isEmpty = view.findViewById<TextView>(R.id.isEmpty)
        val isEmptyPrice = view.findViewById<TextView>(R.id.isEmptyPrice)
        val isEmptyQuantity = view.findViewById<TextView>(R.id.isEmptyQuantity)
        val isNotInteger = view.findViewById<TextView>(R.id.isEmptyPriceNotInteger)
        val isNotDouble = view.findViewById<TextView>(R.id.isEmptyPriceNotDouble)
        val duplicationError =view.findViewById<TextView>(R.id.duplicationError)

        //Button
        val okBTN = view.findViewById<Button>(R.id.okBtn)
        val closeBTN = view.findViewById<Button>(R.id.closeBtn)
        okBTN.isEnabled =false
        val regex = """\s+""".toRegex()

        val addDialog = AlertDialog.Builder(this@MainActivity)
        addDialog.setView(view)
        val alert = addDialog.create()

        fun updateOkButtonState() {
            val itemNameNotEmpty = itemName.text.isNotEmpty()
            val itemPriceNotEmpty = itemPrice.text.isNotEmpty()
            val itemQuantityNotEmpty = itemQuantity.text.isNotEmpty()

            okBTN.isEnabled = itemNameNotEmpty && itemPriceNotEmpty && itemQuantityNotEmpty
        }
        if (method.equals("save")){

            // onTextChange validation for edittext
            itemName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
                override fun afterTextChanged(s: Editable?) {
                    if(itemName.text.isEmpty() || itemName.text.toString().matches(regex)){
                        isEmpty.visibility = View.VISIBLE
                        okBTN.isEnabled = false
                    }else {
                        isEmpty.visibility = View.GONE
                        updateOkButtonState()
                    }
                }

            })
            itemPrice.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
                override fun afterTextChanged(s: Editable?) {
                    if (itemPrice.text.toString().isEmpty()){
                        isEmptyPrice.visibility = View.VISIBLE
                        isNotDouble.visibility = View.GONE
                        okBTN.isEnabled = false
                    }else if (!isDouble(itemPrice.text.toString()) || itemPrice.text.toString().toDouble() <= 0){
                        isNotDouble.visibility = View.VISIBLE
                        isEmptyPrice.visibility = View.GONE
                        okBTN.isEnabled = false
                    } else{
                        isNotDouble.visibility = View.GONE
                        isEmptyPrice.visibility = View.GONE
                        updateOkButtonState()
                    }
                }

            })
            itemQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if (itemQuantity.text.toString().isEmpty()){
                        isEmptyQuantity.visibility = View.VISIBLE
                        isNotInteger.visibility = View.GONE
                        okBTN.isEnabled = false
                    } else if (!isInteger(itemQuantity.text.toString()) || itemQuantity.text.toString().toInt() < 0){
                        isNotInteger.visibility = View.VISIBLE
                        isEmptyQuantity.visibility = View.GONE
                        okBTN.isEnabled = false
                    }else {
                        isNotInteger.visibility = View.GONE
                        isEmptyQuantity.visibility = View.GONE
                        updateOkButtonState()
                    }
                }

            })

        }

        else if (method.equals("update")){

            header.text = "Update"
            okBTN.text = "Update"
            itemName.setText(Itemname)
            itemPrice.setText(Itemprice)
            itemQuantity.setText(Itemquantity)

            itemName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
                override fun afterTextChanged(s: Editable?) {
                    if(itemName.text.isEmpty() || itemName.text.toString().matches(regex)){
                        isEmpty.visibility = View.VISIBLE
                        okBTN.isEnabled = false
                    }else {
                        isEmpty.visibility = View.GONE
                        updateOkButtonState()
                    }
                }

            })
            itemPrice.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
                override fun afterTextChanged(s: Editable?) {
                    if (itemPrice.text.toString().isEmpty()){
                        isEmptyPrice.visibility = View.VISIBLE
                        isNotDouble.visibility = View.GONE
                        okBTN.isEnabled = false
                    }else if (!isDouble(itemPrice.text.toString()) || itemPrice.text.toString().toDouble() <= 0){
                        isNotDouble.visibility = View.VISIBLE
                        isEmptyPrice.visibility = View.GONE
                        okBTN.isEnabled = false
                    } else{
                        isNotDouble.visibility = View.GONE
                        isEmptyPrice.visibility = View.GONE
                        updateOkButtonState()
                    }
                }

            })
            itemQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if (itemQuantity.text.toString().isEmpty()){
                        isEmptyQuantity.visibility = View.VISIBLE
                        isNotInteger.visibility = View.GONE
                        okBTN.isEnabled = false
                    } else if (!isInteger(itemQuantity.text.toString()) || itemQuantity.text.toString().toInt() < 0){
                        isNotInteger.visibility = View.VISIBLE
                        isEmptyQuantity.visibility = View.GONE
                        okBTN.isEnabled = false
                    }else {
                        isNotInteger.visibility = View.GONE
                        isEmptyQuantity.visibility = View.GONE
                        updateOkButtonState()
                    }
                }

            })

        }

        // setting up conditions


        okBTN.setOnClickListener {

        if (method.equals("save")){
            val name = itemName.text.toString().trim().toLowerCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val price = itemPrice.text.toString()
            val quantity = itemQuantity.text.toString()

            if (quantity.toInt() < 0 && price.toDouble() < 0){
                isNotInteger.visibility = View.VISIBLE
                isNotDouble.visibility = View.VISIBLE
                okBTN.isEnabled = false
            } else if (quantity.toInt() < 0){
                isNotInteger.visibility = View.VISIBLE
                okBTN.isEnabled = false
            } else if (price.toDouble() < 0){
                isNotDouble.visibility = View.VISIBLE
                okBTN.isEnabled = false
            } else {
                crud(name, price, quantity, position, method, createdDate)
                itemName.setText("")
                itemPrice.setText("")
                itemQuantity.setText("")
                alert.dismiss()
            }

            }

            else if (method.equals("update")) {
            val name = itemName.text.toString().trim().toLowerCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val price = itemPrice.text.toString()
            val quantity = itemQuantity.text.toString()

            if (quantity.toInt() < 0 && price.toDouble() < 0){
                isNotInteger.visibility = View.VISIBLE
                isNotDouble.visibility = View.VISIBLE
                okBTN.isEnabled = false
            } else if (quantity.toInt() < 0){
                isNotInteger.visibility = View.VISIBLE
                okBTN.isEnabled = false
            } else if (price.toDouble() < 0){
                isNotDouble.visibility = View.VISIBLE
                okBTN.isEnabled = false
            } else {
                crud(name, price, quantity, position, method, createdDate, updatedDate)
                itemName.setText("")
                itemPrice.setText("")
                itemQuantity.setText("")
                alert.dismiss()
            }


        }
        }
        closeBTN.setOnClickListener {
            itemName.setText("")
            itemPrice.setText("")
            itemQuantity.setText("")
            isEmpty.visibility = View.GONE
            isEmptyPrice.visibility = View.GONE
            isEmptyQuantity.visibility =View.GONE
            isNotInteger.visibility = View.GONE
            isNotDouble.visibility = View.GONE
            alert.dismiss()
            Toast.makeText(this@MainActivity, "Closed",Toast.LENGTH_SHORT).show()

        }
        alert.show()
    }

    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
    // loading data retrieved from local if the app is closed...
    private fun loadData() {
        SharedPreferences = applicationContext.getSharedPreferences("data", MODE_PRIVATE)
        var gson = Gson()
        val sharedPref = SharedPreferences.getString("item_data", null)
        val json:String? = sharedPref


        if (!json.isNullOrBlank()){

            val type = object : TypeToken<ArrayList<Items>>() {}.type
            val itemListfromJson : ArrayList<Items> = gson.fromJson(json, type)


            itemList.clear()
            itemList.addAll(itemListfromJson)

        }

    }



    // method in saving data to the local...
    private fun crud(name: String, price: String, quantity: String, position: Int, crudType: String? = null, createdDate: String?=null,updatedDate: String?=null) {
        var gson = Gson()
        var unformatPrice=price.toDouble()
        Log.d("Double not rounded off", "${unformatPrice}")
//        val roundOff = DecimalFormat("#0.00")
        val roundedOffPrice = BigDecimal(unformatPrice).setScale(2, RoundingMode.HALF_UP).toDouble().toString()
        Log.d("rounded off", "${roundedOffPrice}")
        val existingItem = itemList.find {
            it.itemName.equals(name, ignoreCase = true)
        }

        val infilter = LayoutInflater.from(this@MainActivity)
        val modal = infilter.inflate(R.layout.modal_alert, null)



        if (crudType.equals("update")) {

            if (existingItem != null) {
                // Update the price and quantity of the existing item
                existingItem.productPrice = roundedOffPrice
                existingItem.productQuantity = quantity.toInt()
                existingItem.dateUpdated =updatedDate
                messageModal(modal, "Item Updated", "Success")
                saveItemListToSharedPreferences()
            } else {
                itemList.set(position, Items("$name", roundedOffPrice, quantity.toInt(),createdDate,updatedDate))
                messageModal(modal, "Item Updated", "Success")
                saveItemListToSharedPreferences()
                loadData()
            }
        } else if (crudType.equals("save")) {
            if (existingItem == null) {
                itemList.add(position, Items("$name", roundedOffPrice, quantity.toInt(), createdDate))
                messageModal(modal, "Item Saved", "Success")
                saveItemListToSharedPreferences()
                loadData()
            } else {
                messageModal(modal, "Already Exist", "Fail")
            }
        }
    }

    private fun saveItemListToSharedPreferences() {
        val gson = Gson()
        val itemListJson = gson.toJson(itemList)

        SharedPreferences = applicationContext.getSharedPreferences("data", MODE_PRIVATE)
        editor = SharedPreferences.edit()
        editor.putString("item_data", itemListJson)
        editor.apply()

        itemAdapter.notifyDataSetChanged()
        Log.d("Saved Data", "items: $itemListJson")
    }

    private fun messageModal(view: View, message: String?, messageType:String?){
        val messageTxt = view.findViewById<TextView>(R.id.messagetxt)
        val messageBtn = view.findViewById<TextView>(R.id.messageExit)
        val addDialog = AlertDialog.Builder(this@MainActivity)
        addDialog.setView(view)
        val alert = addDialog.create()

        if (messageType.equals("Fail")){
            messageTxt.setTextColor(Color.RED)
            messageTxt.text = message
        }else if (messageType.equals("Success")){
            messageTxt.setTextColor(Color.GREEN)
            messageTxt.text = message
        }

        messageBtn.setOnClickListener{
            alert.dismiss()
        }

        alert.show()
    }



}