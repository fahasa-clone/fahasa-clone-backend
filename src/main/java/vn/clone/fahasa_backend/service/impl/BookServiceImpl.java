package vn.clone.fahasa_backend.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.BookDetail;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;
import vn.clone.fahasa_backend.service.BookService;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public PageResponse<List<BookDTO>> fetchAllBooks(Pageable pageable, String filter) {
        Specification<Book> specification = SpecificationsBuilder.createSpecification(filter);
        Page<Book> bookPage = bookRepository.findAll(specification, pageable);
        List<BookDTO> bookDTOList = bookPage.stream()
                                            .map(book -> {
                                                BookDetail bookDetail = book.getBookDetail();
                                                return BookDTO.builder()
                                                              .id(book.getId())
                                                              .name(book.getName())
                                                              .price(book.getPrice())
                                                              .discountPercentage(book.getDiscountPercentage())
                                                              .discountAmount(book.getDiscountAmount())
                                                              .averageRating(book.getAverageRating())
                                                              .ratingCount(book.getRatingCount())
                                                              .stock(book.getStock())
                                                              .deleted(book.isDeleted())
                                                              .bookDetail(BookDTO.BookDetailDTO.builder()
                                                                                               .publicationYear(bookDetail.getPublicationYear())
                                                                                               .weight(bookDetail.getWeight())
                                                                                               .bookHeight(bookDetail.getBookHeight())
                                                                                               .bookWidth(bookDetail.getBookWidth())
                                                                                               .bookThickness(bookDetail.getBookThickness())
                                                                                               .pageCount(bookDetail.getPageCount())
                                                                                               .layout(bookDetail.getLayout())
                                                                                               .description(bookDetail.getDescription())
                                                                                               .build())
                                                              .bookImages(book.getBookImages()
                                                                              .stream()
                                                                              .map(image -> BookDTO.BookImageDTO.builder()
                                                                                                                .id(image.getId())
                                                                                                                .imagePath(image.getImagePath())
                                                                                                                .imageOrder(image.getImageOrder())
                                                                                                                .build())
                                                                              .toList())
                                                              .build();
                                            })
                                            .toList();
        return new PageResponse<>(pageable.getPageNumber() + 1, pageable.getPageSize(),
                                  bookPage.getTotalPages(), bookDTOList);
    }
}
