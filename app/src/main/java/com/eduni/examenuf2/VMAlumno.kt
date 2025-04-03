package com.eduni.examenuf2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VMAlumno: ViewModel() {

    private val _alumno = MutableLiveData<Alumno>()
    val alumno: LiveData<Alumno>
        get() = _alumno

    fun setAlumno(alumno: Alumno) {
        _alumno.value = alumno
    }

    fun clearAlumno() {
        _alumno.value = Alumno(0, "", "", 0)
    }
}