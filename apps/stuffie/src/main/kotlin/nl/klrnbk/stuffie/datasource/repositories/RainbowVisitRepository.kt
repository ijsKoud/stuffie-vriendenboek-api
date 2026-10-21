package nl.klrnbk.stuffie.datasource.repositories

import nl.klrnbk.stuffie.datasource.models.RainbowVisitModel
import org.springframework.data.jpa.repository.JpaRepository

interface RainbowVisitRepository : JpaRepository<RainbowVisitModel, String> {
    fun findByStuffieIdIn(stuffieIds: Collection<String>): List<RainbowVisitModel>

    fun findByStuffieIdAndReturnedAtIsNull(stuffieId: String): List<RainbowVisitModel>

    fun deleteByStuffieIdIn(stuffieIds: Collection<String>)
}
