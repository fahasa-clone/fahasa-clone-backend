package vn.clone.fahasa_backend.domain.response.category;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CategoryDTO {

    private Integer id;

    private String name;

    private String slug;

    private String description;

    private String categoryIcon;

    private ParentCategoryDTO parentCategory;

    private List<CategorySpecDTO> categorySpecs;

    @Builder
    @Getter
    @Setter
    public static class ParentCategoryDTO {

        private Integer id;

        private String name;
    }

    @Builder
    @Getter
    @Setter
    public static class CategorySpecDTO {

        private Integer specId;

        private String specName;

        private boolean isFiltered;
    }
}
