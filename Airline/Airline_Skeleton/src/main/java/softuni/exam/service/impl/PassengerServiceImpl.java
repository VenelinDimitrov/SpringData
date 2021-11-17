package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.json.PassengerSeedDto;
import softuni.exam.models.entity.Passenger;
import softuni.exam.repository.PassengerRepository;
import softuni.exam.service.PassengerService;
import softuni.exam.service.TownService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class PassengerServiceImpl implements PassengerService {

    private static final String PASSENGERS_FILE_PATH = "src/main/resources/files/json/passengers.json";

    private final PassengerRepository passengerRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;
    private final TownService townService;

    public PassengerServiceImpl(PassengerRepository passengerRepository, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson, TownService townService) {
        this.passengerRepository = passengerRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
        this.townService = townService;
    }

    @Override
    public boolean areImported() {
        return passengerRepository.count() > 0;
    }

    @Override
    public String readPassengersFileContent() throws IOException {
        return Files.readString(Path.of(PASSENGERS_FILE_PATH));
    }

    @Override
    public String importPassengers() throws IOException {
        StringBuilder result = new StringBuilder();

        Arrays.stream(gson.fromJson(readPassengersFileContent(), PassengerSeedDto[].class))
                .filter(passengerSeedDto -> {
                    boolean isValid = validationUtil.isValid(passengerSeedDto);

                    result.append(isValid ? String.format("Successfully imported Passenger %s - %s",
                            passengerSeedDto.getLastName(), passengerSeedDto.getEmail())
                            : "Invalid passenger").append(System.lineSeparator());

                    return isValid;
                }).map(passengerSeedDto -> {
                    Passenger passenger = modelMapper.map(passengerSeedDto, Passenger.class);
                    passenger.setTown(townService.getTownByName(passengerSeedDto.getTown()));

                    return passenger;
                })
                .forEach(passengerRepository::save);

        return result.toString().trim();
    }

    @Override
    public Passenger getPassengerByEmail(String email) {
        return passengerRepository.findByEmail(email);
    }

    @Override
    public String getPassengersOrderByTicketsCountDescendingThenByEmail() {
        StringBuilder result = new StringBuilder();

        passengerRepository.findAllPassengersByTicketCountDescThenByEmail()
                .forEach(passenger -> {
                    result.append(String.format("Passenger %s  %s\n" +
                            "\tEmail - %s\n" +
                            "Phone - %s\n" +
                            "\tNumber of tickets - %d\n",
                            passenger.getFirstName(), passenger.getLastName()
                    , passenger.getEmail(), passenger.getPhoneNumber(), passenger.getTickets().size()));
                });

        return result.toString().trim();
    }
}
