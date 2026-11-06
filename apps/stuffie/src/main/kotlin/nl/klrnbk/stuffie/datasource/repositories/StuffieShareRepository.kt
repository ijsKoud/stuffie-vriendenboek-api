package nl.klrnbk.stuffie.datasource.repositories

import nl.klrnbk.stuffie.datasource.models.StuffieShareModel
import org.springframework.data.jpa.repository.JpaRepository

interface StuffieShareRepository : JpaRepository<StuffieShareModel, String> {
    fun findByToken(token: String): List<StuffieShareModel>

    fun findByStuffieId(stuffieId: String): List<StuffieShareModel>

    fun findByStuffieIdIn(stuffieIds: Collection<String>): List<StuffieShareModel>

    fun deleteByStuffieIdIn(stuffieIds: Collection<String>)
}
