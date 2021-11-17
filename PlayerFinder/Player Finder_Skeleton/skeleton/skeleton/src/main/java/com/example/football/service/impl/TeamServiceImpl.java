package com.example.football.service.impl;

import com.example.football.models.dto.json.TeamSeedDto;
import com.example.football.models.entity.Team;
import com.example.football.repository.TeamRepository;
import com.example.football.service.TeamService;
import com.example.football.service.TownService;
import com.example.football.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class TeamServiceImpl implements TeamService {

    private static final String TEAMS_FILE_PATH = "src/main/resources/files/json/teams.json";

    private final TeamRepository teamRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final TownService townService;

    public TeamServiceImpl(TeamRepository teamRepository, ModelMapper modelMapper, Gson gson, ValidationUtil validationUtil, TownService townService) {
        this.teamRepository = teamRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.townService = townService;
    }

    @Override
    public boolean areImported() {
        return teamRepository.count() > 0;
    }

    @Override
    public String readTeamsFileContent() throws IOException {
        return Files.readString(Path.of(TEAMS_FILE_PATH));
    }

    @Override
    public String importTeams() throws IOException {
        StringBuilder result = new StringBuilder();

        Arrays.stream(gson.fromJson(readTeamsFileContent(), TeamSeedDto[].class))
                .filter(teamSeedDto -> {
                    boolean isValid = validationUtil.isValid(teamSeedDto)
                            && townExistsInDataBase(teamSeedDto.getTownName())
                            && !teamAlreadyExists(teamSeedDto.getName());

                    result.append(isValid ? String.format("Successfully imported Team %s - %d"
                            ,teamSeedDto.getName(), teamSeedDto.getFanBase())
                            : "Invalid Team").append(System.lineSeparator());

                    return isValid;
                }).map(teamSeedDto -> {
                    Team team = modelMapper.map(teamSeedDto, Team.class);
                    team.setTown(townService.findTownByName(teamSeedDto.getTownName()));

                    return team;
                }).forEach(teamRepository::save);

        return result.toString().trim();
    }

    @Override
    public boolean teamAlreadyExists(String name) {
        return teamRepository.existsByName(name);
    }

    @Override
    public Team findTeamByName(String teamName) {
        return teamRepository.findByName(teamName).orElse(null);
    }

    private boolean townExistsInDataBase(String townName) {
        return townService.checkIfEntityExists(townName);
    }
}
