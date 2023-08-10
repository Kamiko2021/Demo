package com.smartherd.productapp.adapters

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartherd.productapp.Items
import com.smartherd.productapp.R
import java.lang.IndexOutOfBoundsException

class ItemListAdapter(val c:Context, private val originalItemList:ArrayList<Items>) :
    RecyclerView.Adapter<ItemListAdapter.MyViewHolder>() {

    private var itemlist: ArrayList<Items> = originalItemList
    private lateinit var mListener : onItemClickListener
    private var filteredList: ArrayList<Items> = ArrayList() // Add this line
    private lateinit var SharedPreferences : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    // ... Rest of the adapter code ...

    fun filterItems(query: String?=null) {
        filteredList.clear()

        if (query != null) {
            val lowerCaseQuery = query.trim().toLowerCase()

            if (lowerCaseQuery.isNotEmpty()) {
                for (item in originalItemList) {
                    val itemNameWithoutSpaces = item.itemName!!.toLowerCase().replace("\\s+".toRegex(), "")
                    if (itemNameWithoutSpaces.contains(lowerCaseQuery) && itemNameWithoutSpaces.length > 1) {
                        filteredList.add(item)
                    }
                }
            }
            if (query.isBlank()) {
                Log.d("isBlank ", "true")
                // Add items with more than one word to the filtered list
                for (item in originalItemList) {
                    val itemNameWords = item.itemName!!.split(" ").filter { it.isNotBlank() }
                    if (itemNameWords.size > 1) {
                        filteredList.add(item)
                    }
                }

            }
            if (query.isNullOrEmpty()){
//                filteredList = originalItemList
                Log.d("isEmpty ", "true")
                loadData()
            }

            itemlist = filteredList
        }

        notifyDataSetChanged()
    }
//    fun checkWhiteSpace(str :String){
//        val regex = """\s""".toRegex()
//        val containsNoWhitespace = regex.matches(str)
//        Log.d("white space", containsNoWhitespace.toString())
//        if (str.isEmpty()) {
//            itemlist = originalItemList
//        }
//    }


    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemListAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: ItemListAdapter.MyViewHolder, position: Int) {
        val currentItem = itemlist[position]
        val itemQuantity = holder.itemQuantity
        val itemName = holder.itemName
        val itemPrice= holder.itemPrice
        val deleteBtn = holder.deleteItem


        holder.itemName.text = currentItem.itemName.toString().trim()
        val formatted =currentItem.productPrice.toString().replace(Regex("(\\d)(?=(\\d{3})+\$)"), "$1,")
        Log.d("formatted price:", "${formatted}")
        holder.itemPrice.text = "$ "+ currentItem.productPrice.toString()

       // Validations for Quantity

       when(currentItem.productQuantity){
            1 -> {
                holder.itemQuantity.text = currentItem.productQuantity.toString()+ " pc"
            }
            else -> {
                holder.itemQuantity.text = currentItem.productQuantity.toString()+ " pcs"
            }
        }
        if (itemQuantity.text.toString() == "0 pcs"){
            val infilter = LayoutInflater.from(c)
            val modal = infilter.inflate(R.layout.modal_alert, null)
            deleteBtn.isEnabled = true
            deleteBtn.setOnClickListener {
                try {
                    itemlist.removeAt(position)
                    notifyItemRemoved(position)
                    updateItemListToSharedPreferences(itemlist)
                    loadData()
                    messageModal(modal, "Deleted Successfully", "Success")
                }catch (e:IndexOutOfBoundsException){
                    messageModal(modal, "error: ${e.toString()}", "Fail")
                }


            }
        } else {
            deleteBtn.isEnabled = false
        }
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }

   inner class MyViewHolder(val itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        val itemName : TextView = itemView.findViewById(R.id.itemName)
        val itemPrice : TextView = itemView.findViewById(R.id.itemPrice)
        val itemQuantity : TextView = itemView.findViewById(R.id.itemQuantity)
        val deleteItem: Button = itemView.findViewById(R.id.deleteBtn)

       init {
           itemView.setOnClickListener {
               listener.onItemClick(adapterPosition)
           }
       }
    }

    private fun messageModal(view: View, message: String?, messageType:String?){
        val messageTxt = view.findViewById<TextView>(R.id.messagetxt)
        val messageBtn = view.findViewById<TextView>(R.id.messageExit)
        val addDialog = AlertDialog.Builder(c)
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
    private fun updateItemListToSharedPreferences(list: ArrayList<Items>) {
        val itemListJson = Gson().toJson(list)


        // Assuming you have access to the SharedPreferences instance.
       SharedPreferences = c.getSharedPreferences("data",
            AppCompatActivity.MODE_PRIVATE
        )
        editor = SharedPreferences.edit()
        editor.putString("item_data", itemListJson)
        editor.apply()


    }

    private fun loadData() {
        SharedPreferences = c.getSharedPreferences(
            "data",
            AppCompatActivity.MODE_PRIVATE
        )
        var gson = Gson()
        val sharedPref = SharedPreferences.getString("item_data", null)
        val json: String? = sharedPref


        if (!json.isNullOrBlank()) {

            val type = object : TypeToken<ArrayList<Items>>() {}.type
            val itemListfromJson: ArrayList<Items> = gson.fromJson(json, type)


            itemlist.clear()
            itemlist.addAll(itemListfromJson)

        }

    }
}