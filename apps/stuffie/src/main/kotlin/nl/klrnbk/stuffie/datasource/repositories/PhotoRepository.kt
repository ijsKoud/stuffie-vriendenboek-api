package nl.klrnbk.stuffie.datasource.repositories

import nl.klrnbk.stuffie.datasource.models.PhotoModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PhotoRepository : JpaRepository<PhotoModel, String> {
    fun findByIdAndOwnerSub(
        id: String,
        ownerSub: String,
    ): List<PhotoModel>

    fun deleteByOwnerSub(ownerSub: String)

    @Query("select distinct p.ownerSub from PhotoModel p")
    fun findDistinctOwnerSubs(): List<String>
}
