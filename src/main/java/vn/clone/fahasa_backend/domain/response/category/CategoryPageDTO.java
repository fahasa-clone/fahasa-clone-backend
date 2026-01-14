package vn.clone.fahasa_backend.domain.response.category;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;

@Builder
@Getter
@Setter
public class CategoryPageDTO {

    private List<BreadcrumbDTO> categoryBreadcrumbs;

    private List<BreadcrumbDTO> parentCategories;

    private CategoryNodeDTO currentCategoryNode;

    private List<CategoryNodeDTO> childCategories;

    private PageResponse<List<BookDTO>> pageResponse;
}
