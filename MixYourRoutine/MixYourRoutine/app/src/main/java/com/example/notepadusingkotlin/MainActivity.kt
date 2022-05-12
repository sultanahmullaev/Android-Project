package com.example.notepadusingkotlin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.content.*
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notepadusingkotlin.DataBase.DbManager
import com.example.notepadusingkotlin.Model.Note
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var listNotes = ArrayList<Note>()

    //shared preference
    private var mSharedPref : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSharedPref = this.getSharedPreferences("My_Data", Context.MODE_PRIVATE)

        //load sorting technique as selected before, default setting will be newest
        when(mSharedPref!!.getString("Sort", "newest"))
        {
            "newest" -> LoadQueryNewest("%")
            "oldest" -> LoadQueryOldest("%")
            "ascending" -> LoadQueryAscending("%")
            "descending" -> LoadQueryDescending("%")
        }

    }

    override fun onResume() {
        super.onResume()
        //load sorting technique as selected before, default setting will be newest
        val mSorting = mSharedPref!!.getString("Sort", "newest")
        when(mSorting)
        {
            "newest" -> LoadQueryNewest("%")
            "oldest" -> LoadQueryOldest("%")
            "ascending" -> LoadQueryAscending("%")
            "descending" -> LoadQueryDescending("%")
        }
    }

    private fun LoadAllRustam(title: String) {
        val dbManager = DbManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.Query(projections, "Title like ?", selectionArgs, "Title")
        if (cursor.moveToFirst()) {
            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val thread = Thread {
                    try {
                        getJsonFromURL(Title,ID)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                thread.start()
            } while (cursor.moveToNext())
        }
        Thread.sleep(2_000)
        refresh()

    }

    private fun refresh(){
        val dbManager = DbManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf("%")
        //sort by title
        val cursor = dbManager.Query(projections, "Title like ?", selectionArgs, "Title")
        listNotes.clear()
        //ascending
        if (cursor.moveToFirst()) {
            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))

            } while (cursor.moveToNext())
        }

        //adapter
        val myNotesAdapter = MyNotesAdapter(this, listNotes)
        //set adapter
        noteLv.adapter = myNotesAdapter
        /*
        val first = Intent(this, MainActivity::class.java)
        val intent = Intent(this, AddNoteActivity::class.java)
        startActivity(intent)
        startActivity(first)
         */
    }


    private fun getJsonFromURL(city: String,p_id:Int) {
        try {
            val api = "99f6ebbd4a2e2d86bfbcc270e879070f"
            val pUrl = "https://api.openweathermap.org/data/2.5/weather/?q=$city&units=metric&lang=ru&appid=$api"
            val url = URL(pUrl)
            val connection = url.openConnection()
            BufferedReader(InputStreamReader(connection.getInputStream())).use { inp ->
                var line: String?
                while (inp.readLine().also { line = it } != null) {
                    //println(line)
                    val obj = JSONObject(line)
                    val wthr: JSONObject = obj.getJSONObject("main")
                    val jsonArray: JSONArray = obj.getJSONArray("weather")
                    val wthrDescr = JSONObject(jsonArray.getString(0))
                    println(wthr.getString("temp")+"°C")
                    println(wthrDescr.getString("description"))

                    val dbManager = DbManager(this)
                    val selectionArgsID = arrayOf(p_id.toString())
                    val values = ContentValues()
                    values.put("Title", city)
                    values.put("Description", wthr.getString("temp")+"°C\n"+wthrDescr.getString("description"))
                    dbManager.update(values, "ID=?",selectionArgsID)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun LoadQueryAscending(title: String) {
        val dbManager = DbManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf(title)
        //sort by title
        val cursor = dbManager.Query(projections, "Title like ?", selectionArgs, "Title")
        listNotes.clear()
        //ascending
        if (cursor.moveToFirst()) {
            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))

            } while (cursor.moveToNext())
        }

        //adapter
        val myNotesAdapter = MyNotesAdapter(this, listNotes)
        //set adapter
        noteLv.adapter = myNotesAdapter

        //get total number of tasks from List
        val total = noteLv.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null)
        {
            //set to actionbar as subtitle of actionbar
            mActionBar.subtitle = "You have $total note(s) in list..."
        }
    }

    private fun LoadQueryDescending(title: String) {
        val dbManager = DbManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf(title)
        //sort by title
        val cursor = dbManager.Query(projections, "Title like ?", selectionArgs, "Title")
        listNotes.clear()
        //descending
        if (cursor.moveToLast()) {
            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))

            } while (cursor.moveToPrevious())
        }

        //adapter
        val myNotesAdapter = MyNotesAdapter(this, listNotes)
        //set adapter
        noteLv.adapter = myNotesAdapter

        //get total number of tasks from List
        val total = noteLv.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null)
        {
            //set to actionbar as subtitle of actionbar
            mActionBar.subtitle = "You have $total note(s) in list..."
        }
    }

    private fun LoadQueryNewest(title: String) {
        val dbManager = DbManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf(title)
        //sort by ID
        val cursor = dbManager.Query(projections, "ID like ?", selectionArgs, "ID")
        listNotes.clear()

        //Newest first(the record will be entered at the bottom of previous records and has larger ID then previous records)

        if (cursor.moveToLast()) {
            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))

            } while (cursor.moveToPrevious())
        }

        //adapter
        val myNotesAdapter = MyNotesAdapter(this, listNotes)
        //set adapter
        noteLv.adapter = myNotesAdapter

        //get total number of tasks from List
        val total = noteLv.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null)
        {
            //set to actionbar as subtitle of actionbar
            mActionBar.subtitle = "You have $total note(s) in list..."
        }
    }

    private fun LoadQueryOldest(title: String) {
        val dbManager = DbManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf(title)
        //sort by ID
        val cursor = dbManager.Query(projections, "ID like ?", selectionArgs, "ID")
        listNotes.clear()

        //oldest first(the record will be entered at the bottom of previous records and has larger ID then previous records, so lesser the ID is the oldest the record is)

        if (cursor.moveToFirst()) {
            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))

            } while (cursor.moveToNext())
        }

        //adapter
        val myNotesAdapter = MyNotesAdapter(this, listNotes)
        //set adapter
        noteLv.adapter = myNotesAdapter

        //get total number of tasks from List
        val total = noteLv.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null)
        {
            //set to actionbar as subtitle of actionbar
            mActionBar.subtitle = "You have $total note(s) in list..."
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val sv:SearchView = menu!!.findItem(R.id.app_bar_search).actionView as SearchView

        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        sv.setSearchableInfo(sm.getSearchableInfo(componentName))
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                LoadQueryAscending("%"+query+"%")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                LoadQueryAscending("%"+newText+"%")
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item != null)
        {
            when(item.itemId)
            {
                R.id.addNote ->
                {
                    startActivity(Intent(this, AddNoteActivity::class.java))
                }
                R.id.copyBtn ->
                {
                    LoadAllRustam("%")
                }
                R.id.action_sort ->
                {
                    //show sorting dialog
                    showSortDialog()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSortDialog() {
        val sortOption = arrayOf("Newest", "Older", "Title(Ascending)", "Title(Descending)")
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Sort by")
        mBuilder.setIcon(R.drawable.ic_sort_black_24dp)
        mBuilder.setSingleChoiceItems(sortOption, -1)
        {
            dialogInterface, i ->
            if (i==0)
            {
                //newest first
                Toast.makeText(this,"Newest",Toast.LENGTH_SHORT).show()
                val editor = mSharedPref!!.edit()
                editor.putString("Sort", "newest")
                editor.apply()
                LoadQueryNewest("%")

            }
            if (i==1)
            {
                //older first
                Toast.makeText(this,"Oldest",Toast.LENGTH_SHORT).show()
                val editor = mSharedPref!!.edit()
                editor.putString("Sort", "oldest")
                editor.apply()
                LoadQueryOldest("%")
            }
            if (i==2)
            {
                //title ascending
                Toast.makeText(this,"Title(Ascending)",Toast.LENGTH_SHORT).show()
                val editor = mSharedPref!!.edit()
                editor.putString("Sort", "ascending")
                editor.apply()
                LoadQueryAscending("%")
            }
            if (i==3)
            {
                //title descending
                Toast.makeText(this,"Title(Descending)",Toast.LENGTH_SHORT).show()
                val editor = mSharedPref!!.edit()
                editor.putString("Sort", "Descending")
                editor.apply()
                LoadQueryDescending("%")
            }
            dialogInterface.dismiss()
        }

        val mDialog = mBuilder.create()
        mDialog.show()
    }

    inner class MyNotesAdapter(context: Context, var listNotesAdapter: ArrayList<Note>) :
        BaseAdapter() {
        var context: Context? = context


        @SuppressLint("ViewHolder", "InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            //inflate layout row.xml
            val myView = layoutInflater.inflate(R.layout.row, null)
            val myNote = listNotesAdapter[position]
            myView.titleTv.text = myNote.noteName
            myView.descTv.text = myNote.noteDes
            //delete btn click
            myView.deleteBtn.setOnClickListener {
                val dbManager = DbManager(this.context!!)
                val selectionArgs = arrayOf(myNote.noteID.toString())
                dbManager.delete("ID=?", selectionArgs)
                LoadQueryAscending("%")
            }
            //edit//update button click
            myView.editBtn.setOnClickListener {
                GoToUpdateFun(myNote)
            }
            //copy btn click
//            myView.copyBtn.setOnClickListener {
//
//                LoadAllRustam("%")
//
//            }


            return myView
        }

        override fun getItem(position: Int): Any {
            return listNotesAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listNotesAdapter.size
        }

    }

    private fun GoToUpdateFun(myNote: Note) {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("ID", myNote.noteID)
        intent.putExtra("name", myNote.noteName)
        intent.putExtra("des", myNote.noteDes)
        startActivity(intent)
    }
}
