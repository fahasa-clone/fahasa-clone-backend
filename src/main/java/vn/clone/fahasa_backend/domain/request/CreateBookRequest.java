package vn.clone.fahasa_backend.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import vn.clone.fahasa_backend.annotation.ValidImage;
import vn.clone.fahasa_backend.annotation.ValidImages;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateBookRequest extends UpdateBookRequest {

    @NotNull(message = "Cover image is required")
    @ValidImage(message = "Cover image must be a valid image file (jpg, jpeg, png, gif, webp, bmp)")
    private MultipartFile coverImage;

    @ValidImages(maxCount = 2)
    private List<MultipartFile> images;
}
