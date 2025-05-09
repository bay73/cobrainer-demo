package cobrainer.demo.service

import cobrainer.demo.model.Skill
import cobrainer.demo.model.SkillId
import cobrainer.demo.model.SkillType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SkillsService {

    fun getSkills(): List<Skill> {
        return listOf(
            Skill(SkillId("eb184430-ab14-434e-a459-faf7ad919429"), "Kotlin", "", SkillType.HARD_SKILL),
            Skill(SkillId("e81e53c9-ac27-4e16-96f3-2bd8e2014643"), "Java", "", SkillType.HARD_SKILL),
            Skill(SkillId("c3a30a95-0cab-43f0-aad6-84d683548cd4"), "Collaboration", "", SkillType.SOFT_SKILL),
            Skill(SkillId("2b93af90-aca8-49e4-965c-344caaf82b07"), "Teamwork", "", SkillType.SOFT_SKILL),
            Skill(SkillId("0878870b-ca22-4e21-a450-6bf31263397c"), "English", "", SkillType.LANGUAGE_SKILL),
            Skill(SkillId("1c8d72ad-38d2-445c-a9c0-0efaf357c97e"), "German", "", SkillType.LANGUAGE_SKILL),
            Skill(SkillId("271189b3-437b-486b-93c7-01f8746097d0"), "Cloud Computing", "", SkillType.HARD_SKILL) ,
        )
    }
}