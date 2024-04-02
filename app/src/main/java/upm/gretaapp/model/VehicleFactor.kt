package upm.gretaapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "vehicles")
data class VehicleFactor(
    @PrimaryKey
    val id: Long,
    val factor: Double = 1.0,
    @ColumnInfo(name = "needs_consumption")
    val needsConsumption: Boolean = true,
)