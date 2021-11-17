package softuni.exam.domain.dto.json;

import com.google.gson.annotations.Expose;

public class PictureDto {

    @Expose
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
