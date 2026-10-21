package nl.klrnbk.stuffie.datasource.repositories

import nl.klrnbk.stuffie.datasource.models.StuffieModel
import nl.klrnbk.stuffie.datasource.models.StuffieStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StuffieRepository : JpaRepository<StuffieModel, String> {
    fun findByOwnerSubOrderByCreatedAtDesc(ownerSub: String): List<StuffieModel>

    fun findByOwnerSubAndStatusOrderByCreatedAtDesc(
        ownerSub: String,
        status: StuffieStatus,
    ): List<StuffieModel>

    fun findByIdAndOwnerSub(
        id: String,
        ownerSub: String,
    ): List<StuffieModel>

    fun findByOwnerSub(ownerSub: String): List<StuffieModel>

    fun deleteByOwnerSub(ownerSub: String)

    @Query("select distinct s.ownerSub from StuffieModel s")
    fun findDistinctOwnerSubs(): List<String>
}
