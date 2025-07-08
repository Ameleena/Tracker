package com.habittracker.data.db

import com.habittracker.domain.model.HabitLog as DomainHabitLog
import com.habittracker.domain.repository.HabitLogRepository as DomainHabitLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitLogRepository(private val habitLogDao: HabitLogDao) : DomainHabitLogRepository {
    override fun getLogsForHabit(habitId: Int): Flow<List<DomainHabitLog>> =
        habitLogDao.getLogsForHabit(habitId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertLog(log: DomainHabitLog) =
        habitLogDao.insertLog(log.toData())

    override suspend fun updateLog(log: DomainHabitLog) =
        habitLogDao.updateLog(log.toData())

    override suspend fun deleteLog(log: DomainHabitLog) =
        habitLogDao.deleteLog(log.toData())

    override suspend fun getLogByDate(habitId: Int, date: String): DomainHabitLog? =
        habitLogDao.getLogByDate(habitId, date)?.toDomain()

    override suspend fun getLogByDateAndTime(habitId: Int, date: String, reminderTime: String): DomainHabitLog? =
        habitLogDao.getLogByDateAndTime(habitId, date, reminderTime)?.toDomain()
}

fun HabitLog.toDomain() = DomainHabitLog(
    id = id,
    habitId = habitId,
    date = date,
    isCompleted = isCompleted,
    reminderTime = reminderTime
)

fun DomainHabitLog.toData() = HabitLog(
    id = id,
    habitId = habitId,
    date = date,
    isCompleted = isCompleted,
    reminderTime = reminderTime
) 