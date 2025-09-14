package vn.clone.fahasa_backend.domain.response.category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTree {
    private Integer id;
    private String name;
    private List<CategoryTree> children;
}