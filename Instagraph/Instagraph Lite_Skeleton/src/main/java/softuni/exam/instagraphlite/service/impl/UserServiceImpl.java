package softuni.exam.instagraphlite.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.instagraphlite.models.dto.json.UserSeedDto;
import softuni.exam.instagraphlite.models.entity.User;
import softuni.exam.instagraphlite.repository.UserRepository;
import softuni.exam.instagraphlite.service.PictureService;
import softuni.exam.instagraphlite.service.UserService;
import softuni.exam.instagraphlite.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final String USERS_PATH_FILE = "src/main/resources/files/users.json";

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final PictureService pictureService;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, Gson gson, ValidationUtil validationUtil, PictureService pictureService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.pictureService = pictureService;
    }

    @Override
    public boolean areImported() {
        return userRepository.count() > 0;
    }

    @Override
    public String readFromFileContent() throws IOException {
        return Files.readString(Path.of(USERS_PATH_FILE));
    }

    @Override
    public String importUsers() throws IOException {
        StringBuilder result = new StringBuilder();

        Arrays.stream(gson.fromJson(readFromFileContent(), UserSeedDto[].class))
                .filter(userSeedDto -> {
                    boolean isValid = validationUtil.isValid(userSeedDto)
                            && !userExists(userSeedDto.getUsername())
                            && pictureService.entityExists(userSeedDto.getProfilePicture());

                    result.append(isValid ? String.format("Successfully imported User: %s",
                            userSeedDto.getUsername())
                            : "Invalid user").append(System.lineSeparator());

                    return isValid;
                }).map(userSeedDto -> {
                    User user = modelMapper.map(userSeedDto, User.class);
                    user.setProfilePicture(pictureService.findByPath(userSeedDto.getProfilePicture()));

                    return user;
                }).forEach(userRepository::save);

        return result.toString().trim();
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public String exportUsersWithTheirPosts() {
        StringBuilder result = new StringBuilder();

        userRepository.findAllByPostsCountDescThenById()
                .forEach(user -> {
                    result.append(String.format("User: %s\n" +
                            "Post count: %d\n", user.getUsername(), user.getPosts().size())).append(System.lineSeparator());

                    user.getPosts().forEach(post -> {
                        result.append(String.format("==Post Details:\n" +
                                "----Caption: %s\n" +
                                "----Picture Size: %.2f\n", post.getCaption(), post.getPicture().getSize()))
                                .append(System.lineSeparator());
                    });
                });

        return result.toString().trim();
    }
}
