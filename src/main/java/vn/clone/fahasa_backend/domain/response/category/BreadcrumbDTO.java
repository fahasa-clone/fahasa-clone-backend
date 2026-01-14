package vn.clone.fahasa_backend.domain.response.category;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BreadcrumbDTO {

    private Integer id;

    private String name;

    private String path;
}
