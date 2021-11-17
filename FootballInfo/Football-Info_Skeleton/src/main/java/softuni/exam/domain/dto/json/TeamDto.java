package softuni.exam.domain.dto.json;

import com.google.gson.annotations.Expose;

public class TeamDto {

    @Expose
    private String name;
    @Expose
    private PictureDto picture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PictureDto getPicture() {
        return picture;
    }

    public void setPicture(PictureDto picture) {
        this.picture = picture;
    }
}
