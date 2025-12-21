package vn.clone.fahasa_backend.domain.response.category;

import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class GetCategoryPageDTO {

    private Integer id;

    private String name;

    private CategoryBranch categoryBranch;

    private List<SpecDTO> specs;

    @Builder
    @Getter
    @Setter
    public static class SpecDTO {

        private Integer id;

        private String name;

        Set<String> values;
    }
}
