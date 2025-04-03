package com.eduni.examenuf2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduni.examenuf2.databinding.FragmentHomeBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class HomeFragment : Fragment() {
    private val firestoreDB: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val alumnoViewModel: VMAlumno by activityViewModels()
    private lateinit var viewBinding: FragmentHomeBinding
    private lateinit var alumnoListAdapter: AlumnoAdapter
    private val alumnoItems = ArrayList<Alumno>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        configureFirestoreSettings()
        setupRecyclerView()
        alumnoSelection()
        setupButtonListeners()
        loadAlumnoData()
        return viewBinding.root
    }

    private fun configureFirestoreSettings() {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {})
        }
        firestoreDB.firestoreSettings = settings
    }

    private fun setupRecyclerView() {
        viewBinding.recyclerview.layoutManager = LinearLayoutManager(context)
        alumnoListAdapter = AlumnoAdapter(alumnoItems, alumnoViewModel)
        viewBinding.recyclerview.adapter = alumnoListAdapter
    }

    private fun alumnoSelection() {
        alumnoViewModel.alumno.observe(viewLifecycleOwner) { selectedAlumno ->
            viewBinding.editTextAlumnoNombreInput.setText(selectedAlumno.nombre)

            if (selectedAlumno.grupo != "") {
                viewBinding.editTextAlumnoGrupoInput.setText(selectedAlumno.grupo.toString())
            } else {
                viewBinding.editTextAlumnoGrupoInput.text?.clear()
            }

            if (selectedAlumno.nota != 0) {
                viewBinding.editTextAlumnoNotaInput.setText(selectedAlumno.nota.toString())
            } else {
                viewBinding.editTextAlumnoNotaInput.text?.clear()
            }

            updateButtonStates(selectedAlumno.id != 0)
        }
    }

    private fun updateButtonStates(isAlumnoSelected: Boolean) {
        viewBinding.buttonDelete.isEnabled = isAlumnoSelected
        viewBinding.buttonUpdate.isEnabled = isAlumnoSelected
    }

    private fun setupButtonListeners() {
        viewBinding.buttonDelete.setOnClickListener {
            deleteSelectedAlumno()
        }
        viewBinding.buttonUpdate.setOnClickListener {
            updateSelectedAlumno()
        }
        viewBinding.buttonSave.setOnClickListener {
            saveNewAlumno()
        }
    }

    private fun deleteSelectedAlumno() {
        val selectedAlumnoId = alumnoViewModel.alumno.value?.id
        if (selectedAlumnoId != null && selectedAlumnoId != 0) {
            firestoreDB.collection("alumnos").document("I3wcnlSAUl6oM05gVEdX").update("alumnos.$selectedAlumnoId", FieldValue.delete())
                .addOnSuccessListener {
                    showToast("Alumno borrado")
                }
                .addOnFailureListener {
                    showErrorToast("Error al borrar el alumno")
                }
        } else {
            showToast("Selecciona un alumno para borrar")
        }
    }

    private fun updateSelectedAlumno() {
        val name = viewBinding.editTextAlumnoNombreInput.text.toString()
        val grupoStr = viewBinding.editTextAlumnoGrupoInput.text.toString()
        val notaStr = viewBinding.editTextAlumnoNotaInput.text.toString()
        val selectedAlumnoId = alumnoViewModel.alumno.value?.id

        if (name.isNotEmpty() && grupoStr.isNotEmpty() && notaStr.isNotEmpty() && selectedAlumnoId != null && selectedAlumnoId != 0) {
            try {
                val grupo = grupoStr.toString()
                val nota = notaStr.toInt()
                if (grupo != "" && (nota in 0..10)) {
                    val updates = mapOf(
                        "alumnos.$selectedAlumnoId.nombre" to name,
                        "alumnos.$selectedAlumnoId.grupo" to grupo,
                        "alumnos.$selectedAlumnoId.nota" to nota
                    )
                    firestoreDB.collection("alumnos").document("I3wcnlSAUl6oM05gVEdX").update(updates)
                        .addOnSuccessListener {
                            showToast("Alumno actualizado")
                        }
                        .addOnFailureListener {
                            showErrorToast("Error al actualizar")
                        }
                } else {
                    showToast("Debes rellenar el grupo y la nota no puede ser negativa ni superior a 10")
                }
            } catch (e: NumberFormatException) {
                showToast("Grupo o nota no válidos")
            }
        } else {
            showToast("Completa todos los campos")
        }
    }

    private fun saveNewAlumno() {
        val name = viewBinding.editTextAlumnoNombreInput.text.toString()
        val grupoStr = viewBinding.editTextAlumnoGrupoInput.text.toString()
        val notaStr = viewBinding.editTextAlumnoNotaInput.text.toString()

        if (name.isNotEmpty() && grupoStr.isNotEmpty() && notaStr.isNotEmpty()) {
            try {
                val grupo = grupoStr.toString()
                val nota = notaStr.toInt()
                if (grupo != "" && (nota in 0..10)) {
                    firestoreDB.runTransaction { transaction ->
                        val doc = transaction.get(firestoreDB.collection("alumnos").document("I3wcnlSAUl6oM05gVEdX"))
                        val currentCount = doc.getLong("contador") ?: 0
                        val newCount = currentCount + 1
                        val newAlumno = mapOf("nombre" to name, "grupo" to grupo, "nota" to nota)
                        val currentAlumnos = doc.get("alumnos") as? MutableMap<String, Map<String, Any>> ?: mutableMapOf()
                        currentAlumnos[newCount.toString()] = newAlumno
                        transaction.update(firestoreDB.collection("alumnos").document("I3wcnlSAUl6oM05gVEdX"), mapOf("contador" to newCount, "alumnos" to currentAlumnos))
                    }.addOnSuccessListener {
                        showToast("Alumno guardado")
                        viewBinding.editTextAlumnoGrupoInput.text?.clear()
                        viewBinding.editTextAlumnoNotaInput.text?.clear()
                    }
                        .addOnFailureListener {
                            showErrorToast("Error al guardar")
                        }
                } else {
                    showToast("Debes rellenar el grupo y la nota no puede ser negativa ni superior a 10")
                }
            } catch (e: NumberFormatException) {
                showToast("Grupo o nota no válidos")
            }
        } else {
            showToast("Completa todos los campos")
        }
    }

    private fun loadAlumnoData() {
        firestoreDB.collection("alumnos").document("I3wcnlSAUl6oM05gVEdX").get()
            .addOnSuccessListener { document ->
                updateAlumnoList(document)
            }
            .addOnFailureListener { showErrorToast("Error obteniendo los alumnos") }
    }

    private fun updateAlumnoList(document: DocumentSnapshot?) {
        alumnoItems.clear()
        if (document != null && document.exists()) {
            val data = document.data
            if (data != null && data.containsKey("alumnos")) {
                val alumnoMap = data["alumnos"] as? Map<String, Map<String, Any>>
                alumnoMap?.forEach { (id, alumnoData) ->
                    val name = alumnoData["nombre"] as? String ?: ""
                    val grupo = alumnoData["grupo"] as? String ?: ""
                    val nota = (alumnoData["nota"] as? Long)?.toInt() ?: 0
                    alumnoItems.add(Alumno(id.toInt(), name, grupo, nota))
                }
            }
        } else {
            createInitialDocument()
            return
        }
        alumnoListAdapter.notifyDataSetChanged()
        alumnoViewModel.clearAlumno()
    }

    private fun createInitialDocument() {
        val initialData = mapOf("contador" to 0, "alumnos" to mapOf<String, Map<String, Any>>())
        firestoreDB.collection("alumnos").document("I3wcnlSAUl6oM05gVEdX").set(initialData)
            .addOnSuccessListener {
                loadAlumnoData()
            }
            .addOnFailureListener {
                showErrorToast("Error al crear el documento")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        loadAlumnoData()
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}