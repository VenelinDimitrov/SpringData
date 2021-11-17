package softuni.exam.domain.dto.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "teams")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeamRootSeedDto {

    @XmlElement(name = "team")
    private List<TeamSeedDto> teams;

    public List<TeamSeedDto> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamSeedDto> teams) {
        this.teams = teams;
    }
}
