package jp.techacademy.tominaga.atsushi.taskapp

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

const val EXTA_TASK = "jp.techacademy.tominaga.atsushi.taskapp.TASK"

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mRealm: Realm
    private val mRealmListener = object  : RealmChangeListener<Realm> {
        override fun onChange(element: Realm){
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter : TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity,InputActivity::class.java)
            startActivity(intent)
        }

        //Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)


        //ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        //ListViewをタップした時の処理
        listView1.setOnItemClickListener{ parent, view, position, id ->
            //入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity,InputActivity::class.java)
            intent.putExtra(EXTA_TASK,task.id)
            startActivity(intent)
        }


        //ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            //タスクを削除
            val task = parent.adapter.getItem(position) as  Task

            //ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_,_ ->
                val results = mRealm.where(Task::class.java).equalTo("id",task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext,TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }


        categorySearchButton.setOnClickListener(this)
        allViewButton.setOnClickListener(this)



        reloadListView()
    }

    private fun reloadListView(){
        //Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date",Sort.DESCENDING)

        //上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        //TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        //表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()

    }



    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

    override fun onClick(v: View?) {

        val categoryString = categoryEditText.text.toString()

        when(v) {
            categorySearchButton ->{
                if (categoryString != "") {
                    //Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得

                    val taskRealmResults =
                        mRealm.where(Task::class.java).equalTo("category", categoryString).findAll()
                            .sort("date", Sort.DESCENDING)

                    //上記の結果を、TaskList としてセットする
                    mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                    //TaskのListView用のアダプタに渡す
                    listView1.adapter = mTaskAdapter

                    //表示を更新するために、アダプターにデータが変更されたことを知らせる
                    mTaskAdapter.notifyDataSetChanged()

                } else {
                    val taskRealmResults =
                        mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

                    //上記の結果を、TaskList としてセットする
                    mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                    //TaskのListView用のアダプタに渡す
                    listView1.adapter = mTaskAdapter

                    //表示を更新するために、アダプターにデータが変更されたことを知らせる
                    mTaskAdapter.notifyDataSetChanged()
                }
            }
            allViewButton ->{
                val taskRealmResults =
                    mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

                //上記の結果を、TaskList としてセットする
                mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

                //TaskのListView用のアダプタに渡す
                listView1.adapter = mTaskAdapter

                //表示を更新するために、アダプターにデータが変更されたことを知らせる
                mTaskAdapter.notifyDataSetChanged()
            }


        }


    }

}
