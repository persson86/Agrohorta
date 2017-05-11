package com.mobile.persson.agrohorta

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.androidhuman.rxfirebase2.database.RxFirebaseDatabase
import com.androidhuman.rxfirebase2.database.data
import com.androidhuman.rxfirebase2.database.dataChanges
import com.google.firebase.database.*
import com.mobile.persson.agrohorta.adapters.ContentAdapter
import com.mobile.persson.agrohorta.database.models.PlantModel
import com.mobile.persson.agrohorta.database.models.PlantModelRealm
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.ArrayList
import com.google.firebase.database.DataSnapshot
import java.util.function.Consumer


class Main2Activity : AppCompatActivity() {

    val mDatabaseRef: DatabaseReference? = FirebaseDatabase.getInstance().getReference()
    var plants: MutableList<PlantModelRealm> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        //getNews()
        test()
    }

    fun test(){
        val ref: DatabaseReference = FirebaseDatabase.getInstance().reference

        ref.child(getString(R.string.node_database))
                .child("language_en")
                .child(getString(R.string.node_plant_list))
                .dataChanges()
                .subscribe({
                    if (it.exists()) {
                        Log.i("teste", it.value.toString())
                    } else {
                        // Data does not exists
                    }
                }) {
                    // Handle error
                }


        /*ref.child(getString(R.string.node_database))?.child("language_en")?.child(getString(R.string.node_plant_list))?.data()
                ?.subscribe({
                    Toast.makeText(applicationContext, "teste", Toast.LENGTH_SHORT).show()
                }) {
                    // NoSuchElementException is thrown when there are no data exist
                }*/
    }

    fun getNews(): Observable<List<PlantModelRealm>> {
        return Observable.create {
            subscriber ->
            val news = mutableListOf<PlantModelRealm>()
            getPlantList()
            subscriber.onNext(news)
            subscriber.onComplete()
        }
    }

    fun getPlantList() {
        mDatabaseRef?.child(getString(R.string.node_database))?.child("language_en")?.child(getString(R.string.node_plant_list))
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (data in dataSnapshot.children) {
                            val receivePlant = data.getValue(PlantModel::class.java)
                            val plant = PlantModelRealm()
                            plant.plantName = receivePlant.plantName
                            plant.plantImage = receivePlant.plantImage
                            plants.add(plant)
                        }

                        recyclerView.layoutManager = GridLayoutManager(applicationContext, 3)
                        recyclerView.hasFixedSize()
                        recyclerView.adapter = ContentAdapter(applicationContext, plants)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(applicationContext, "erro", Toast.LENGTH_SHORT).show()
                    }
                })
    }

}


/*fun observe(query: Query): Observable<DataSnapshot> {
    return Observable.fromAsync<DataSnapshot>({
        val listener = query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                it.onError(RuntimeException(databaseError.message))
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                it.onNext(dataSnapshot)
            }
        })
        it.setCancellation { query.removeEventListener(listener) }
    }, AsyncEmitter.BackpressureMode.BUFFER)
}*/


