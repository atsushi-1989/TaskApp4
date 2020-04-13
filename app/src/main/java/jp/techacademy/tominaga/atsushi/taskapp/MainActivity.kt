package jp.techacademy.tominaga.atsushi.taskapp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
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
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        //Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)


        //ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        //ListViewをタップした時の処理
        listView1.setOnItemClickListener{ parent, view, position, id ->
            //入力・編集する画面に遷移させる
        }


        //ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            //タスクを削除
            true
        }

        //アプリ起動時に表示テスト用のタスクを作成する
        addTaskForTest()

        reloadListView()
    }

    private fun reloadListView(){
        //Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("data",Sort.DESCENDING)

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

    private fun addTaskForTest(){
        val task = Task()
        task.title = "作業"
        task.contents = "プログラムを書いてPUSHする"
        task.date = Date()
        task.id = 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()

    }

}
