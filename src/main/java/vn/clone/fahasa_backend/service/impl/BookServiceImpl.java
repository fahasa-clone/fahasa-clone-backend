package vn.clone.fahasa_backend.service.impl;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.BookDetail;
import vn.clone.fahasa_backend.domain.BookImage;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.BookRepositoryCustom;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;
import vn.clone.fahasa_backend.service.BookService;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookRepositoryCustom bookRepositoryCustom;

    @Override
    public Page<BookDTO> fetchAllBooks(Pageable pageable, String filter) {
        Specification<Book> specification = SpecificationsBuilder.createSpecification(filter);
        // return bookRepository.findAll(specification, pageable)
        //                      .map(this::convertToDTO);
        return bookRepositoryCustom.findAllBooksWithFirstImage(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FullBookDTO getBookById(int id) {
        Book book = bookRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
        return convertToFullDTO(book);
    }

    private BookDTO convertToDTO(Book book) {
        String imagePath = null;
        List<BookImage> images = book.getBookImages();
        for (BookImage image : images) {
            if (image.getImageOrder() == 1) {
                imagePath = image.getImagePath();
                break;
            }
        }

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
                      .imagePath(imagePath)
                      .build();
    }

    private FullBookDTO convertToFullDTO(Book book) {
        BookDetail bookDetail = book.getBookDetail();
        return FullBookDTO.builder()
                          .id(book.getId())
                          .name(book.getName())
                          .price(book.getPrice())
                          .discountPercentage(book.getDiscountPercentage())
                          .discountAmount(book.getDiscountAmount())
                          .averageRating(book.getAverageRating())
                          .ratingCount(book.getRatingCount())
                          .stock(book.getStock())
                          .deleted(book.isDeleted())
                          .bookDetail(FullBookDTO.BookDetailDTO.builder()
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
                                          .map(image -> FullBookDTO.BookImageDTO.builder()
                                                                                .id(image.getId())
                                                                                .imagePath(image.getImagePath())
                                                                                .imageOrder(image.getImageOrder())
                                                                                .build())
                                          .toList())
                          .build();
    }
}
