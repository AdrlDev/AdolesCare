package dev.adriele.adolescare.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PdfSearchViewModel : ViewModel() {
    val searchQuery = MutableLiveData<String>()
    val searchMatches = MutableLiveData<List<Int>>() // optional: page indexes
}
