package com.example.notepadusingkotlin

import android.annotation.SuppressLint
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.notepadusingkotlin.DataBase.DbManager
import kotlinx.android.synthetic.main.activity_add_note.*
import java.lang.Exception

class AddNoteActivity : AppCompatActivity() {

    val dbTable = "Weather List"
    private var id = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        try {
            val bundle : Bundle? = intent.extras
            id = bundle!!.getInt("ID", 0)
            if (id!=0)
            {
                //update note
                //change actionbar title
                supportActionBar!!.title = "Update Note"
                //change button text
                addBtn.text = "Update"
                titleEt.setText(bundle.getString("name"))
                descEt.setText(bundle.getString("des"))
            }
        }catch (ex : Exception){

        }

    }

    fun addFunc(view: View)
    {
        val dbManager = DbManager(this)

        val values = ContentValues()
        values.put("Title", titleEt.text.toString())
        values.put("Description", descEt.text.toString())

        if (id == 0)
        {
            val ind = dbManager.insert(values)
            if (ind>0)
            {

                Toast.makeText(this, "Note is added", Toast.LENGTH_SHORT).show()
                finish()
            }
            else
            {
                Toast.makeText(this, "Error adding note...", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            val selectionArgs = arrayOf(id.toString())
            val ind = dbManager.update(values, "ID=?", selectionArgs)
            if (ind>0)
            {
                Toast.makeText(this, "Note is updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            else
            {
                Toast.makeText(this, "Error updating note...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
