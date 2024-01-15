package upm.gretaapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity data class represents a single row in the database.
 */
@Entity
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val brand: String,
    val model: String,
    val age: Int,
    val motorType: String,
    val kilometers: Int?
)