package com.example.football.service.impl;

import com.example.football.models.dto.xml.StatRootSeedDto;
import com.example.football.models.entity.Stat;
import com.example.football.repository.StatRepository;
import com.example.football.service.StatService;
import com.example.football.util.ValidationUtil;
import com.example.football.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StatServiceImpl implements StatService {

    private static final String STATS_FILE_PATH = "src/main/resources/files/xml/stats.xml";

    private final StatRepository statRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;

    public StatServiceImpl(StatRepository statRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser) {
        this.statRepository = statRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
    }

    @Override
    public boolean areImported() {
        return statRepository.count() > 0;
    }

    @Override
    public String readStatsFileContent() throws IOException {
        return Files.readString(Path.of(STATS_FILE_PATH));
    }

    @Override
    public String importStats() throws JAXBException, FileNotFoundException {
        StringBuilder result = new StringBuilder();

        xmlParser.fromFile(STATS_FILE_PATH, StatRootSeedDto.class)
                .getStats()
                .stream()
                .filter(statSeedDto -> {
                    boolean isValid = validationUtil.isValid(statSeedDto)
                            && !checkIfStatAlreadyExistsInDataBase(statSeedDto.getPassing(),
                            statSeedDto.getShooting(), statSeedDto.getEndurance());

                    result.append(isValid ? String.format("Successfully imported Stat %.2f - %.2f - %.2f",
                            statSeedDto.getShooting(), statSeedDto.getPassing(), statSeedDto.getEndurance())
                            : "Invalid Stat").append(System.lineSeparator());

                    return isValid;
                }).map(statSeedDto -> modelMapper.map(statSeedDto, Stat.class))
                .forEach(statRepository::save);

        return result.toString().trim();
    }

    @Override
    public Stat findStatById(Long statId) {
        return statRepository.findById(statId).orElse(null);
    }

    private boolean checkIfStatAlreadyExistsInDataBase(Double passing, Double shooting, Double endurance) {
        return statRepository.existsByPassing(passing) && statRepository.existsByShooting(shooting)
                && statRepository.existsByEndurance(endurance);
    }
}
