package upm.gretaapp.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "vehicles",
    foreignKeys = [ForeignKey(entity = User::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["user"])]
)
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val brand: String,
    val model: String,
    val year: Int,
    @ColumnInfo(name = "motor_type") val motorType: String,
    val kilometers: Int?,
    val photo: String,
    val user: Int
)