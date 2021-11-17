package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.domain.dto.json.PlayersSeedDto;
import softuni.exam.domain.entities.Player;
import softuni.exam.repository.PlayerRepository;
import softuni.exam.service.PictureService;
import softuni.exam.service.PlayerService;
import softuni.exam.service.TeamService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final String PLAYERS_FILE_PATH = "src/main/resources/files/json/players.json";

    private final PlayerRepository playerRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final PictureService pictureService;
    private final TeamService teamService;

    public PlayerServiceImpl(PlayerRepository playerRepository, Gson gson, ModelMapper modelMapper, ValidationUtil validationUtil, PictureService pictureService, TeamService teamService) {
        this.playerRepository = playerRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.pictureService = pictureService;
        this.teamService = teamService;
    }

    @Override
    public String importPlayers() throws IOException {
        StringBuilder result = new StringBuilder();

        Arrays.stream(gson.fromJson(readPlayersJsonFile(), PlayersSeedDto[].class))
                .filter(playersSeedDto -> {
                    boolean isValid = validationUtil.isValid(playersSeedDto)
                            && checkIfPlayerPictureExists(playersSeedDto.getPicture().getUrl())
                            && checkIfTeamExists(playersSeedDto.getTeam().getName())
                            && checkIfTeamPictureExists(playersSeedDto.getTeam().getPicture().getUrl());

                    result.append(isValid ? String.format("Successfully imported player: %s %s",
                            playersSeedDto.getFirstName(), playersSeedDto.getLastName())
                            : "Invalid Player").append(System.lineSeparator());

                    return isValid;
                }).map(playersSeedDto -> {
                    Player player = modelMapper.map(playersSeedDto, Player.class);
                    player.setPicture(pictureService.findByUrl(playersSeedDto.getPicture().getUrl()));
                    player.setTeam(teamService.findTeamByName(playersSeedDto.getTeam().getName()));

                    return player;
                }).forEach(playerRepository::save);


        return result.toString().trim();
    }

    private boolean checkIfTeamPictureExists(String url) {
        return pictureService.entityExists(url);
    }

    private boolean checkIfTeamExists(String name) {
        return teamService.checkIfEntityExistsByName(name);
    }

    private boolean checkIfPlayerPictureExists(String url) {
        return pictureService.entityExists(url);
    }

    @Override
    public boolean areImported() {
        return playerRepository.count() > 0;
    }

    @Override
    public String readPlayersJsonFile() throws IOException {
        return Files.readString(Path.of(PLAYERS_FILE_PATH));
    }

    @Override
    public String exportPlayersWhereSalaryBiggerThan() {
        StringBuilder result = new StringBuilder();

        playerRepository.findAllBySalaryGreaterThanOrderBySalaryDesc(BigDecimal.valueOf(100000))
                .forEach(player -> {
                    result.append(String.format("Player name: %s %s \n" +
                            "Number: %d\n" +
                            "Salary: %.2f\n" +
                            "Team: %s\n",
                            player.getFirstName(), player.getLastName()
                    ,player.getNumber(), player.getSalary(), player.getTeam().getName())).append(System.lineSeparator());
                });

        return result.toString().trim();
    }

    @Override
    public String exportPlayersInATeam() {
        StringBuilder result = new StringBuilder();

        result.append("Team: North Hub").append(System.lineSeparator());

        teamService.findTeamByName("North Hub")
                .getPlayers().forEach(player -> {
                    result.append(String.format("Player name: %s %s - %s\n" +
                            "Number: %d\n",
                            player.getFirstName(), player.getLastName(), player.getPosition(), player.getNumber()));
                });

        return result.toString().trim();
    }
}
