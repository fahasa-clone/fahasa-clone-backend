package vn.clone.fahasa_backend.domain.response.category;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CategoryTree {

    private Integer id;

    private String name;

    private String categoryIcon;

    private String slug;

    private List<CategoryTree> children;
}