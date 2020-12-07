package mx.tecnm.tepic.ladm_u4_ejercicio4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var baseRemota =  FirebaseFirestore.getInstance()
    var datos = ArrayList<String>()
    var listaID = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mostrarEnListView()
        btnRegistrar.setOnClickListener {
            insertar()
        }

    }

    private fun mostrarEnListView() {
        baseRemota.collection("datos")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException!=null){
                    mensaje("Error no se pudo recuperar data desde NUBE")
                    return@addSnapshotListener
                }
                datos.clear()
                listaID.clear()
                var cadena =""
                for(registro in querySnapshot!!){
                    cadena = "Descripcion: ${registro.getString("descripcion")}\nLugar: ${registro.getString("lugar")}\nFecha ${registro.getString("fecha")}"
                    datos.add(cadena)
                    listaID.add(registro.id)
                }
                var adaptador = ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,datos)
                lstvDatos.adapter=adaptador
                lstvDatos.setOnItemClickListener { parent, view, position, id ->
                    mostrarAlertEliminarActualizar(position)
                }

            }
    }

    private fun insertar() {
        val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
        val netDate = Date(caldvFecha.date)
        val date = sdf.format(netDate)
        var datos = hashMapOf(
            "descripcion" to edtDecripcion.text.toString(),
            "lugar" to edtLugar.text.toString(),
            "fecha" to date
        )

        baseRemota.collection("datos")
            .add(datos as Any)
            .addOnSuccessListener {
                Toast.makeText(this,"Se inserto datos",Toast.LENGTH_LONG).show()
                edtDecripcion.setText("")
                edtLugar.setText("")
            }
            .addOnFailureListener {
                mensaje("No se pudo insertar \n" +
                        "${it.message}")
            }
    }
    private fun mostrarAlertEliminarActualizar(posicion:Int) {
        var idLista = listaID.get(posicion)
        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage("¿Que desea hacer con \n ${datos.get(posicion)}?")
            .setPositiveButton("Eliminar"){d,i-> eliminar(idLista)}
            .setNeutralButton("CANCELAR")  {d,i->}
            .show()
    }
    private fun eliminar(idLista:String) {
        baseRemota.collection("datos")
            .document(idLista)
            .delete()
            .addOnFailureListener {
                mensaje("no se pudo eliminar")
            }
            .addOnSuccessListener {
                Toast.makeText(this, "Se elimino correctamente", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mensaje(s: String) {
        AlertDialog.Builder(this)
            .setTitle("Atencion")
            .setMessage(s)
            .setPositiveButton("OK"){d,i->}
            .show()
    }
}