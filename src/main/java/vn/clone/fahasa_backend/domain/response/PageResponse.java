package vn.clone.fahasa_backend.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class PageResponse<T> {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private T items;
}
