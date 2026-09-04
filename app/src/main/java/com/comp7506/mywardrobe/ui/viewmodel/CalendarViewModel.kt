package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.db.OutfitRecordEntity
import com.comp7506.mywardrobe.data.db.OutfitWithItems
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.util.endIsoDate
import com.comp7506.mywardrobe.util.startIsoDate
import com.comp7506.mywardrobe.util.toIsoDateString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(private val repository: WardrobeRepository) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val currentMonth: StateFlow<YearMonth> = month.stateIn(viewModelScope, SharingStarted.Eagerly, YearMonth.now())
    val currentSelectedDate: StateFlow<LocalDate> = selectedDate.stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now())

    val outfits: StateFlow<List<OutfitWithItems>> =
        repository.observeOutfits()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recordsInMonth: StateFlow<List<OutfitRecordEntity>> =
        month.flatMapLatest { ym -> repository.observeOutfitRecordsBetween(ym.startIsoDate(), ym.endIsoDate()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedRecord: StateFlow<OutfitRecordEntity?> =
        combine(recordsInMonth, selectedDate) { records, date ->
            val key = date.toIsoDateString()
            records.firstOrNull { it.date == key }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
        clampSelectedDateToCurrentMonth()
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
        clampSelectedDateToCurrentMonth()
    }

    fun setYearMonth(ym: YearMonth) {
        month.value = ym
        clampSelectedDateToCurrentMonth()
    }

    private fun clampSelectedDateToCurrentMonth() {
        val ym = month.value
        val maxDay = ym.lengthOfMonth()
        val d = selectedDate.value
        selectedDate.value = if (YearMonth.from(d) != ym) {
            val day = d.dayOfMonth.coerceIn(1, maxDay)
            ym.atDay(day)
        } else if (d.dayOfMonth > maxDay) {
            ym.atEndOfMonth()
        } else {
            d
        }
    }

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun recordOutfitForSelectedDate(outfitId: Long) {
        val date = selectedDate.value.toIsoDateString()
        viewModelScope.launch {
            repository.setOutfitRecord(date = date, outfitId = outfitId)
        }
    }

    fun deleteRecordForSelectedDate() {
        val date = selectedDate.value.toIsoDateString()
        viewModelScope.launch {
            repository.deleteOutfitRecord(date)
        }
    }

    fun recordPhotoOutfit(imageUri: String) {
        viewModelScope.launch {
            val newOutfitId = repository.addOutfitFromPhoto(imageUri)
            val date = selectedDate.value.toIsoDateString()
            repository.setOutfitRecord(date = date, outfitId = newOutfitId)
        }
    }
}

