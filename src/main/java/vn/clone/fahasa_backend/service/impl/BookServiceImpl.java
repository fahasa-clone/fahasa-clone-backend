package vn.clone.fahasa_backend.service.impl;

import java.time.Instant;
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
import vn.clone.fahasa_backend.domain.*;
import vn.clone.fahasa_backend.domain.request.CreateBookRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookImagesRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookRequest;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.BookRepositoryCustom;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;
import vn.clone.fahasa_backend.service.*;
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

    private final PublisherService publisherService;

    private final AuthorService authorService;

    private final FahasaProperties fahasaProperties;

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> fetchAllBooks(Pageable pageable, String filter) {
        Specification<Book> specification = SpecificationsBuilder.createSpecification(filter);
        specification = specification.and((root, query, cb) -> cb.equal(root.get("deleted"), false));
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
            // Create Book
            Book book = Book.builder()
                            .name(request.getName())
                            .price(request.getPrice())
                            .discountPercentage(request.getDiscountPercentage())
                            .discountAmount(request.getDiscountAmount())
                            .stock(request.getStock())
                            .averageRating(0)
                            .ratingCount(0)
                            .deleted(false)
                            .bookDetail(bookDetail)
                            .build();
            bookDetail.setBook(book);

            // Get Category
            Category category = categoryService.getCategoryById(request.getCategoryId());
            book.setCategory(category);

            // Get Publisher
            Publisher publisher = publisherService.getPublisherById(request.getPublisherId());
            bookDetail.setPublisher(publisher);

            // Get Author list
            List<Author> authors = new ArrayList<>();
            for (int authorId : request.getAuthorIds()) {
                Author author = authorService.getAuthorById(authorId);
                authors.add(author);
            }
            book.setAuthors(authors);

            // Create BookImages from uploaded URLs
            List<BookImage> bookImages = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                BookImage bookImage = BookImage.builder()
                                               .imagePath(imageUrls.get(i))
                                               .imageOrder(i + 1)
                                               .book(book)
                                               .build();
                bookImages.add(bookImage);
            }
            book.setBookImages(bookImages);

            // Save to the database
            Book savedBook = bookRepository.save(book);

            log.info("Book created: {}", savedBook);

            // Convert to DTO
            return convertToFullDTO(savedBook);

        } catch (Exception ex) {
            // Rollback: Delete newly uploaded images from Cloudinary
            if (imageUrls != null) {
                rollbackUploadedImages(imageUrls);
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

        // Update Author list
        // Clear old authors (this will update the join table)
        book.getAuthors()
            .clear();

        // Fetch new authors from database
        List<Author> newAuthors = request.getAuthorIds()
                                         .stream()
                                         .map(authorService::getAuthorById)
                                         .toList();

        // Add new authors
        book.getAuthors()
            .addAll(newAuthors);

        // Update Book
        book.setName(request.getName());
        book.setPrice(request.getPrice());
        book.setDiscountPercentage(request.getDiscountPercentage());
        book.setDiscountAmount(request.getDiscountAmount());
        book.setStock(request.getStock());

        // Update BookDetail
        BookDetail bookDetail = book.getBookDetail();
        bookDetail.setPublicationYear(request.getPublicationYear());
        bookDetail.setWeight(request.getWeight());
        bookDetail.setBookHeight(request.getBookHeight());
        bookDetail.setBookWidth(request.getBookWidth());
        bookDetail.setBookThickness(request.getBookThickness());
        bookDetail.setPageCount(request.getPageCount());
        bookDetail.setLayout(BookLayout.valueOf(request.getLayout()));
        bookDetail.setDescription(request.getDescription());

        // Get Publisher
        Publisher publisher = publisherService.getPublisherById(request.getPublisherId());
        bookDetail.setPublisher(publisher);

        Book updatedBook = bookRepository.save(book);
        return convertToFullDTO(updatedBook);
    }

    @Override
    @Transactional
    public FullBookDTO updateBookImages(int bookId, UpdateBookImagesRequest request) {
        Book book = findBookOrThrow(bookId);
        String bookSlug = VietnameseConverter.convertNameToSlug(book.getName());

        List<String> uploadedImageUrls = new ArrayList<>();
        List<String> deletedPublicIds = new ArrayList<>();

        try {
            // 1. Delete images
            if (request.getImagesToDelete() != null && !request.getImagesToDelete().isEmpty()) {
                deleteImages(book, request.getImagesToDelete(), deletedPublicIds);
            }

            // 2. Upload new images
            if (request.getImagesToUpload() != null && !request.getImagesToUpload().isEmpty()) {
                uploadNewImages(book, request.getImagesToUpload(), bookSlug, uploadedImageUrls);
            }

            // 3. Update image orders
            if (request.getImagesToUpdateOrder() != null && !request.getImagesToUpdateOrder().isEmpty()) {
                updateImageOrders(book, request.getImagesToUpdateOrder());
            }

            // Save changes
            Book updatedBook = bookRepository.save(book);
            log.info("Book images updated successfully for book ID: {}", bookId);

            return convertToFullDTO(updatedBook);

        } catch (Exception ex) {
            log.error("Error updating book images for book ID: {}", bookId, ex);

            // Rollback: Delete newly uploaded images from Cloudinary
            rollbackUploadedImages(uploadedImageUrls);

            // Note: We cannot roll back deleted images from Cloudinary
            // Consider implementing a soft delete strategy for production

            // throw new RuntimeException("Failed to update book images", ex);
            throw ex;
        }
    }

    /**
     * Delete images from Cloudinary and database
     */
    private void deleteImages(Book book,
                              List<UpdateBookImagesRequest.ImageToDelete> imagesToDelete,
                              List<String> deletedPublicIds) {
        for (UpdateBookImagesRequest.ImageToDelete imageToDelete : imagesToDelete) {
            // Find the image in the book's image list
            BookImage bookImage = book.getBookImages()
                                      .stream()
                                      .filter(img -> img.getId() == imageToDelete.getId())
                                      .findFirst()
                                      .orElseThrow(() -> new EntityNotFoundException(
                                              "Image not found with ID: " + imageToDelete.getId()));

            // Verify URL matches (security check)
            if (!bookImage.getImagePath().equals(imageToDelete.getImageUrl())) {
                throw new IllegalArgumentException("Image URL mismatch for ID: " + imageToDelete.getId());
            }

            // Delete from Cloudinary
            String publicId = cloudinaryService.extractPublicIdFromUrl(imageToDelete.getImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
                deletedPublicIds.add(publicId);
            }

            // Remove from the book's image list
            book.getBookImages()
                .remove(bookImage);
        }
    }

    /**
     * Upload new images to Cloudinary and add to database
     */
    private void uploadNewImages(Book book,
                                 List<UpdateBookImagesRequest.ImageToUpload> imagesToUpload,
                                 String bookSlug,
                                 List<String> uploadedImageUrls) {
        String folder = fahasaProperties.getCloudinary()
                                        .getProductFolder();

        for (UpdateBookImagesRequest.ImageToUpload imageToUpload : imagesToUpload) {
            MultipartFile file = imageToUpload.getFile();

            if (file == null || file.isEmpty()) {
                continue;
            }

            // Upload to Cloudinary
            String publicId = cloudinaryService.generatePublicId(bookSlug, imageToUpload.getImageOrder());
            String imageUrl = cloudinaryService.uploadImage(file, folder, publicId);
            uploadedImageUrls.add(imageUrl);

            // Create a BookImage entity
            BookImage newBookImage = BookImage.builder()
                                              .imagePath(imageUrl)
                                              .imageOrder(imageToUpload.getImageOrder())
                                              .book(book)
                                              .build();
            book.getBookImages()
                .add(newBookImage);

            log.info("Uploaded new image for book ID: {}, order: {}",
                     book.getId(), imageToUpload.getImageOrder());
        }
    }

    /**
     * Update image orders in database
     */
    private void updateImageOrders(Book book,
                                   List<UpdateBookImagesRequest.ImageToUpdateOrder> imagesToUpdateOrder) {
        for (UpdateBookImagesRequest.ImageToUpdateOrder orderUpdate : imagesToUpdateOrder) {
            BookImage bookImage = book.getBookImages()
                                      .stream()
                                      .filter(img -> img.getId() == orderUpdate.getId())
                                      .findFirst()
                                      .orElseThrow(() -> new EntityNotFoundException(
                                              "Image not found with ID: " + orderUpdate.getId()));

            bookImage.setImageOrder(orderUpdate.getImageOrder());
            log.info("Updated image order for ID: {} to {}",
                     orderUpdate.getId(), orderUpdate.getImageOrder());
        }
    }

    /**
     * Rollback uploaded images from Cloudinary in case of error
     */
    private void rollbackUploadedImages(List<String> uploadedImageUrls) {
        for (String imageUrl : uploadedImageUrls) {
            try {
                String publicId = cloudinaryService.extractPublicIdFromUrl(imageUrl);
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                    log.info("Rolled back uploaded image: {}", publicId);
                }
            } catch (Exception e) {
                log.warn("Failed to rollback uploaded image: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        Book book = findBookOrThrow(id);
        book.setDeleted(true);
        book.setDeletedAt(Instant.now());
        bookRepository.save(book);
    }

    private Book findBookOrThrow(int id) {
        return bookRepository.findByIdAndDeletedFalse(id)
                             .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
    }

    private FullBookDTO convertToFullDTO(Book book) {
        BookDetail bookDetail = book.getBookDetail();
        Publisher publisher = bookDetail.getPublisher();

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
                                                               .publisher(FullBookDTO.BookDetailDTO
                                                                                  .PublisherDTO.builder()
                                                                                               .id(publisher.getId())
                                                                                               .name(publisher.getName())
                                                                                               .build())
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
                          .authors(book.getAuthors()
                                       .stream()
                                       .map(author -> FullBookDTO.AuthorDTO.builder()
                                                                           .id(author.getId())
                                                                           .name(author.getName())
                                                                           .build())
                                       .toList())
                          .build();
    }
}
