package com.mobile.persson.agrohorta

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.*
import com.mobile.persson.agrohorta.adapters.ContentAdapter
import com.mobile.persson.agrohorta.database.models.PlantModel
import com.mobile.persson.agrohorta.database.models.PlantModelRealm
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.ArrayList


class Main2Activity : AppCompatActivity() {

    var plants: MutableList<PlantModelRealm> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        getNews()
    }

    fun getLists(): ArrayList<PlantModelRealm> {
        val plants = ArrayList<PlantModelRealm>()
        var plant = PlantModelRealm()

        plant.plantName = "planta 1 teste"
        plants.add(plant)

        plant = PlantModelRealm()
        plant.plantName = "planta 2 teste"
        plants.add(plant)

        plant = PlantModelRealm()
        plant.plantName = "planta 3 teste"
        plants.add(plant)

        plant = PlantModelRealm()
        plant.plantName = "planta 4 teste"
        plants.add(plant)

        plant = PlantModelRealm()
        plant.plantName = "planta 5 teste"
        plants.add(plant)

        return plants
    }

    private var mDatabaseRef: DatabaseReference? = FirebaseDatabase.getInstance().getReference();


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
        mDatabaseRef?.child(getString(R.string.node_database))?.child("en")?.child(getString(R.string.node_plant_list))
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
                        //TODO tratar erros
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


