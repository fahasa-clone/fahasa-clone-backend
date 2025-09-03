package vn.clone.fahasa_backend.domain.response.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTree {
    private int id;
    private String name;
    private List<CategoryTree> children;
}