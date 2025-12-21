package vn.clone.fahasa_backend.domain.response.category;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CategoryBranch {

    private Integer id;

    private String name;

    private String slug;

    private boolean isTerminationPoint;
    
    private boolean isParentOfTerminationPoint;

    private List<CategoryBranch> children;
}