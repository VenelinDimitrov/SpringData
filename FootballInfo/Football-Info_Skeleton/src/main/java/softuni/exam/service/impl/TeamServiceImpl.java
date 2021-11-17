package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.domain.dto.xml.TeamRootSeedDto;
import softuni.exam.domain.entities.Team;
import softuni.exam.repository.TeamRepository;
import softuni.exam.service.PictureService;
import softuni.exam.service.TeamService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TeamServiceImpl implements TeamService {

    private static final String TEAMS_FILE_PATH = "src/main/resources/files/xml/teams.xml";

    private final TeamRepository teamRepository;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final PictureService pictureService;

    public TeamServiceImpl(TeamRepository teamRepository, XmlParser xmlParser, ModelMapper modelMapper, ValidationUtil validationUtil, PictureService pictureService) {
        this.teamRepository = teamRepository;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.pictureService = pictureService;
    }

    @Override
    public String importTeams() throws JAXBException, FileNotFoundException {
        StringBuilder result = new StringBuilder();

        xmlParser.fromFile(TEAMS_FILE_PATH, TeamRootSeedDto.class)
                .getTeams()
                .stream()
                .filter(teamSeedDto -> {
                    boolean isValid = validationUtil.isValid(teamSeedDto)
                            && checkIfPictureExistsInDatabase(teamSeedDto.getPicture().getUrl());

                    result.append(isValid ? String.format("Successfully imported - %s",
                            teamSeedDto.getName())
                            : "Invalid Team").append(System.lineSeparator());

                    return isValid;
                }).map(teamSeedDto -> {
                    Team team = modelMapper.map(teamSeedDto, Team.class);
                    team.setPicture(pictureService.findByUrl(teamSeedDto.getPicture().getUrl()));

                    return team;
                }).forEach(teamRepository::save);

        return result.toString().trim();
    }

    private boolean checkIfPictureExistsInDatabase(String url) {
        return pictureService.entityExists(url);
    }

    @Override
    public boolean areImported() {
        return teamRepository.count() > 0;
    }

    @Override
    public String readTeamsXmlFile() throws IOException {
        return Files.readString(Path.of(TEAMS_FILE_PATH));
    }

    @Override
    public boolean checkIfEntityExistsByName(String teamName) {

        return teamRepository.existsByName(teamName);
    }

    @Override
    public Team findTeamByName(String teamName) {
        return teamRepository.findByName(teamName);
    }
}
