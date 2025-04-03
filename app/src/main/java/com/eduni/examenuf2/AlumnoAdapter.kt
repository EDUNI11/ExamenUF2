package com.eduni.examenuf2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlumnoAdapter(private val mList: List<Alumno>, private val vm: VMAlumno) : RecyclerView.Adapter<AlumnoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alumno_view, parent, false)
        return ViewHolder(view)
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val alumno = mList[position]

        holder.textViewName.text = alumno.nombre
        holder.textViewGrupo.text = alumno.grupo
        holder.textViewNota.text = alumno.nota.toString()

        holder.itemView.setOnClickListener {
            vm.setAlumno(alumno)
        }
    }
    override fun getItemCount(): Int {
        return mList.size
    }
    class ViewHolder(itemsView: View) : RecyclerView.ViewHolder(itemsView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewAlumnoName)
        val textViewGrupo: TextView = itemView.findViewById(R.id.textViewAlumnoGrupo)
        val textViewNota: TextView = itemView.findViewById(R.id.textViewAlumnoNota)
    }
}
