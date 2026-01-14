package vn.clone.fahasa_backend.domain.response.category;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CategoryNodeDTO {

    private Integer id;

    private String name;

    private String path;

    private List<CategoryNodeDTO> children;

    private Boolean isActive;

    private Boolean isParentOfActiveNode;
}
