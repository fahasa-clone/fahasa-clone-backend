package vn.clone.fahasa_backend.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.clone.fahasa_backend.config.FahasaProperties;
import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.BookDetail;
import vn.clone.fahasa_backend.domain.BookImage;
import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.request.CreateBookRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookRequest;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.BookRepositoryCustom;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;
import vn.clone.fahasa_backend.service.BookService;
import vn.clone.fahasa_backend.service.CategoryService;
import vn.clone.fahasa_backend.service.CloudinaryService;
import vn.clone.fahasa_backend.util.VietnameseConverter;
import vn.clone.fahasa_backend.util.constant.BookLayout;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookRepositoryCustom bookRepositoryCustom;

    private final CloudinaryService cloudinaryService;

    private final CategoryService categoryService;

    private final FahasaProperties fahasaProperties;

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> fetchAllBooks(Pageable pageable, String filter) {
        Specification<Book> specification = SpecificationsBuilder.createSpecification(filter);
        return bookRepositoryCustom.findAllBooksWithFirstImage(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FullBookDTO getBookById(int id) {
        Book book = findBookOrThrow(id);
        return convertToFullDTO(book);
    }

    @Override
    @Transactional
    public FullBookDTO createBook(CreateBookRequest request) {
        List<String> imageUrls = null;
        try {
            String bookSlug = VietnameseConverter.convertNameToSlug(request.getName());

            // Upload photos to Cloudinary
            MultipartFile coverImage = request.getCoverImage();
            List<MultipartFile> images = request.getImages();
            images.add(0, coverImage);
            imageUrls = cloudinaryService.uploadImages(images, fahasaProperties.getCloudinary()
                                                                               .getProductFolder(), bookSlug);

            // Create BookDetail
            BookDetail bookDetail = BookDetail.builder()
                                              .publicationYear(request.getPublicationYear())
                                              .weight(request.getWeight())
                                              .bookHeight(request.getBookHeight())
                                              .bookWidth(request.getBookWidth())
                                              .bookThickness(request.getBookThickness())
                                              .pageCount(request.getPageCount())
                                              .layout(BookLayout.valueOf(request.getLayout()))
                                              .description(request.getDescription())
                                              .build();

            // Calculate discountAmount
            int discountPercentage = request.getDiscountPercentage() != null ? request.getDiscountPercentage() : 0;
            int discountAmount = (int) (request.getPrice() * discountPercentage / 100.0);

            // Create Book
            Book book = Book.builder()
                            .name(request.getName())
                            .price(request.getPrice())
                            .discountPercentage(discountPercentage)
                            .discountAmount(discountAmount)
                            .stock(request.getStock())
                            .averageRating(0)
                            .ratingCount(0)
                            .deleted(false)
                            // .bookDetail(bookDetail)
                            .build();
            // bookDetail.setBook(book);

            // Get Category
            Category category = categoryService.getCategoryById(request.getCategoryId());
            book.setCategory(category);

            // Create BookImages from uploaded URLs
            List<BookImage> bookImages = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                BookImage bookImage = BookImage.builder()
                                               .imagePath(imageUrls.get(i))
                                               .imageOrder(i)
                                               .book(book)
                                               .build();
                bookImages.add(bookImage);
            }
            book.setBookImages(bookImages);

            // Save to the database
            Book savedBook = bookRepository.save(book);

            log.info("Book created: {}", savedBook);

            // Temporary code
            bookDetail.setId(savedBook.getId());
            savedBook.setBookDetail(bookDetail);
            savedBook = bookRepository.save(savedBook);

            // Convert to DTO
            return convertToFullDTO(savedBook);
        } catch (Exception ex) {
            if (imageUrls != null) {
                String publicId;
                for (String imageUrl : imageUrls) {
                    publicId = cloudinaryService.extractPublicIdFromUrl(imageUrl);
                    cloudinaryService.deleteImage(publicId);
                }
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public FullBookDTO updateBook(int id, UpdateBookRequest request) {
        Book book = findBookOrThrow(id);

        // Get Category
        Category category = categoryService.getCategoryById(request.getCategoryId());
        book.setCategory(category);

        // Update Book
        book.setName(request.getName());
        book.setPrice(request.getPrice());
        book.setDiscountPercentage(request.getDiscountPercentage());
        book.setDiscountAmount(request.getDiscountAmount());
        book.setStock(request.getStock());

        // Update BookDetail
        BookDetail bookDetail = book.getBookDetail();
        if (bookDetail == null) {
            bookDetail = BookDetail.builder()
                                   .id(id)
                                   .build();
            book.setBookDetail(bookDetail);
        }

        bookDetail.setPublicationYear(request.getPublicationYear());
        bookDetail.setWeight(request.getWeight());
        bookDetail.setBookHeight(request.getBookHeight());
        bookDetail.setBookWidth(request.getBookWidth());
        bookDetail.setBookThickness(request.getBookThickness());
        bookDetail.setPageCount(request.getPageCount());
        bookDetail.setLayout(BookLayout.valueOf(request.getLayout()));
        bookDetail.setDescription(request.getDescription());

        Book updatedBook = bookRepository.save(book);
        return convertToFullDTO(updatedBook);
    }

    // @Override
    @Transactional
    public FullBookDTO updateBookImages(int id, List<MultipartFile> newImages) {
        Book book = findBookOrThrow(id);

        // Xóa tất cả hình ảnh cũ khỏi Cloudinary
        List<BookImage> oldImages = book.getBookImages();
        for (BookImage oldImage : oldImages) {
            try {
                String publicId = cloudinaryService.extractPublicIdFromUrl(oldImage.getImagePath());
                cloudinaryService.deleteImage(publicId);
            } catch (Exception e) {
                log.warn("Không thể xóa ảnh từ Cloudinary: {}", e.getMessage());
            }
        }

        // Xóa tất cả hình ảnh cũ khỏi database
        book.getBookImages()
            .clear();

        // Upload ảnh mới
        if (newImages != null && !newImages.isEmpty()) {
            List<String> newImageUrls = cloudinaryService.uploadImages(newImages, "fahasa/books", null);

            List<BookImage> bookImages = new ArrayList<>();
            for (int i = 0; i < newImageUrls.size(); i++) {
                BookImage bookImage = BookImage.builder()
                                               .imagePath(newImageUrls.get(i))
                                               .imageOrder(i)
                                               .book(book)
                                               .build();
                bookImages.add(bookImage);
            }
            book.setBookImages(bookImages);
        }

        Book updatedBook = bookRepository.save(book);
        return convertToFullDTO(updatedBook);
    }

    // @Override
    @Transactional
    public void deleteBookImage(int bookId, Long imageId) {
        Book book = findBookOrThrow(bookId);

        BookImage bookImage = book.getBookImages()
                                  .stream()
                                  .filter(img -> img.getId() == imageId)
                                  .findFirst()
                                  .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại"));

        // Xóa từ Cloudinary
        try {
            String publicId = cloudinaryService.extractPublicIdFromUrl(bookImage.getImagePath());
            cloudinaryService.deleteImage(publicId);
        } catch (Exception e) {
            log.warn("Không thể xóa ảnh từ Cloudinary: {}", e.getMessage());
        }

        // Xóa từ database
        book.getBookImages().remove(bookImage);

        // Cập nhật thứ tự ảnh
        for (int i = 0; i < book.getBookImages().size(); i++) {
            book.getBookImages().get(i).setImageOrder(i);
        }

        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        Book book = findBookOrThrow(id);
        book.setDeleted(true);
    }

    private Book findBookOrThrow(int id) {
        return bookRepository.findByIdAndDeletedFalse(id)
                             .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
    }

    private FullBookDTO convertToFullDTO(Book book) {
        BookDetail bookDetail = book.getBookDetail();
        return FullBookDTO.builder()
                          .id(book.getId())
                          .name(book.getName())
                          .categoryId(book.getCategory()
                                          .getId())
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
                                          .sorted(Comparator.comparingInt(FullBookDTO.BookImageDTO::getImageOrder))
                                          .toList())
                          .build();
    }
}
