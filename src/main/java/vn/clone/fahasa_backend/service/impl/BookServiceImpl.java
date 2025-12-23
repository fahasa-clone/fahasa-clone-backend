package vn.clone.fahasa_backend.service.impl;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
import vn.clone.fahasa_backend.domain.request.BookSpecDTO;
import vn.clone.fahasa_backend.domain.request.CreateBookRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookImagesRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookRequest;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.BookRepositoryCustom;
import vn.clone.fahasa_backend.repository.specification.BookSpecCombinationSpecification;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;
import vn.clone.fahasa_backend.service.*;
import vn.clone.fahasa_backend.util.VietnameseConverter;
import vn.clone.fahasa_backend.util.constant.BookLayout;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final FahasaProperties fahasaProperties;

    private final BookRepository bookRepository;

    private final BookRepositoryCustom bookRepositoryCustom;

    private final CloudinaryService cloudinaryService;

    private final CategoryService categoryService;

    private final PublisherService publisherService;

    private final AuthorService authorService;

    private final SpecService specService;

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> fetchAllBooks(Pageable pageable, String filter,
                                       List<Integer> specIds, List<String> values) {
        // Create the filter specification
        Specification<Book> specification = SpecificationsBuilder.createSpecification(filter);

        if (specIds != null && values != null && specIds.size() == values.size()) {
            // Create combinations
            List<BookSpecCombinationSpecification.BookSpecCombination> combinations = new ArrayList<>();
            for (int i = 0; i < specIds.size(); i++) {
                combinations.add(new BookSpecCombinationSpecification.BookSpecCombination(specIds.get(i), values.get(i)));
            }

            // Create the BookSpec specification
            Specification<Book> bookSpecCombinationSpecification = new BookSpecCombinationSpecification(combinations);

            // Combine filter and BookSpec specifications
            specification = specification.and(bookSpecCombinationSpecification);
        }

        return bookRepositoryCustom.findAllBooksWithFirstImage(specification, pageable);
    }

    @Override
    public List<BookDTO> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return bookRepository.searchByNameFullText(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> fetchNewestArrivalBooks(int page, int size) {
        Pageable pageable = Pageable.ofSize(size)
                                    .withPage(page - 1);
        return bookRepositoryCustom.findNewestBooksWithFirstImage(null, pageable);
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

            // === Upload photos to Cloudinary ===
            MultipartFile coverImage = request.getCoverImage();
            List<MultipartFile> images = request.getImages();
            images.add(0, coverImage);
            imageUrls = cloudinaryService.uploadImages(images, fahasaProperties.getCloudinary()
                                                                               .getProductFolder(), bookSlug);

            // === Create Book ===
            Book book = Book.builder()
                            .name(request.getName())
                            .price(request.getPrice())
                            .discountPercentage(request.getDiscountPercentage())
                            .discountAmount(request.getDiscountAmount())
                            .stock(request.getStock())
                            .averageRating(0)
                            .ratingCount(0)
                            .deleted(false)
                            .category(categoryService.getCategoryById(request.getCategoryId()))
                            .publicationYear(request.getPublicationYear())
                            .weight(request.getWeight())
                            .bookHeight(request.getBookHeight())
                            .bookWidth(request.getBookWidth())
                            .bookThickness(request.getBookThickness())
                            .pageCount(request.getPageCount())
                            .layout(BookLayout.valueOf(request.getLayout()))
                            .description(request.getDescription())
                            .publisher(publisherService.getPublisherById(request.getPublisherId()))
                            .build();

            // === Create an Author list ===
            List<Author> authors = new ArrayList<>();
            for (int authorId : request.getAuthorIds()) {
                Author author = authorService.getAuthorById(authorId);
                authors.add(author);
            }
            book.setAuthors(authors);

            // === Create BookImages from uploaded URLs ===
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

            // === Create BookSpec list ===
            List<BookSpec> bookSpecs = request.getBookSpecs()
                                              .stream()
                                              .map(bookSpecDTO -> BookSpec.builder()
                                                                          .book(book)
                                                                          .spec(specService.getSpecById(bookSpecDTO.getSpecId()))
                                                                          .value(bookSpecDTO.getValue())
                                                                          .build())
                                              .toList();
            book.setBookSpecs(bookSpecs);

            // === Save to the database ===
            Book savedBook = bookRepository.save(book);

            log.info("Book created: {}", savedBook);

            // === Convert to DTO ===
            return convertToFullDTO(savedBook);

        } catch (Exception ex) {
            // === Rollback: Delete newly uploaded images from Cloudinary ===
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

        // === Update Author list ===
        if (book.getAuthors() == null) {
            book.setAuthors(new ArrayList<>());
        }
        List<Author> authors = book.getAuthors();

        // Create a set of requested Author IDs for a quick lookup
        Set<Integer> requestedAuthorIds = new HashSet<>(request.getAuthorIds());

        // Remove authors that are not in the request
        authors.removeIf(author -> !requestedAuthorIds.contains(author.getId()));

        // Add new authors that aren't already in the book
        Set<Integer> existingAuthorIds = authors.stream()
                                                .map(Author::getId)
                                                .collect(Collectors.toSet());

        for (Integer authorId : request.getAuthorIds()) {
            if (!existingAuthorIds.contains(authorId)) {
                authors.add(authorService.getAuthorById(authorId));
            }
        }

        // === Update Book ===
        book.setName(request.getName());
        book.setPrice(request.getPrice());
        book.setDiscountPercentage(request.getDiscountPercentage());
        book.setDiscountAmount(request.getDiscountAmount());
        book.setStock(request.getStock());
        book.setCategory(categoryService.getCategoryById(request.getCategoryId()));

        // === Update BookDetail ===
        book.setPublicationYear(request.getPublicationYear());
        book.setWeight(request.getWeight());
        book.setBookHeight(request.getBookHeight());
        book.setBookWidth(request.getBookWidth());
        book.setBookThickness(request.getBookThickness());
        book.setPageCount(request.getPageCount());
        book.setLayout(BookLayout.valueOf(request.getLayout()));
        book.setDescription(request.getDescription());
        book.setPublisher(publisherService.getPublisherById(request.getPublisherId()));

        // === Update BookSpec list ===
        if (book.getBookSpecs() == null) {
            book.setBookSpecs(new ArrayList<>());
        }
        List<BookSpec> bookSpecs = book.getBookSpecs();

        // Create a map of existing specs by Spec ID for O(1) lookup
        Map<Integer, BookSpec> specMap = bookSpecs.stream()
                                                  .collect(Collectors.toMap(
                                                          bs -> bs.getSpec().getId(),
                                                          bs -> bs
                                                  ));

        // Create a set of requested Spec IDs
        Set<Integer> requestedSpecIds = request.getBookSpecs()
                                               .stream()
                                               .map(BookSpecDTO::getSpecId)
                                               .collect(Collectors.toSet());

        // Remove specs not in request
        bookSpecs.removeIf(bs -> !requestedSpecIds.contains(bs.getSpec().getId()));

        // Update existing or add new
        for (BookSpecDTO bookSpecDTO : request.getBookSpecs()) {
            BookSpec existingSpec = specMap.get(bookSpecDTO.getSpecId());

            if (existingSpec != null) {
                existingSpec.setValue(bookSpecDTO.getValue());
            } else {
                BookSpec newSpec = BookSpec.builder()
                                           .book(book)
                                           .spec(specService.getSpecById(bookSpecDTO.getSpecId()))
                                           .value(bookSpecDTO.getValue())
                                           .build();
                bookSpecs.add(newSpec);
            }
        }

        // === Save to the database ===
        Book updatedBook = bookRepository.save(book);

        // === Convert to DTO ===
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

            // === Save to the database ===
            Book updatedBook = bookRepository.save(book);
            log.info("Book images updated successfully for book ID: {}", bookId);

            // === Convert to DTO
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

    @Override
    public Book findBookOrThrow(int id) {
        return bookRepository.findById(id)
                             .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
    }

    private FullBookDTO convertToFullDTO(Book book) {
        Publisher publisher = book.getPublisher();

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
                          .bookDetail(FullBookDTO.BookDetailDTO.builder()
                                                               .publicationYear(book.getPublicationYear())
                                                               .weight(book.getWeight())
                                                               .bookHeight(book.getBookHeight())
                                                               .bookWidth(book.getBookWidth())
                                                               .bookThickness(book.getBookThickness())
                                                               .pageCount(book.getPageCount())
                                                               .layout(book.getLayout())
                                                               .description(book.getDescription())
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
                          .bookSpecs(book.getBookSpecs()
                                         .stream()
                                         .map(bookSpec -> {
                                             Spec spec = bookSpec.getSpec();
                                             return FullBookDTO.BookSpecDTO.builder()
                                                                           .specId(spec.getId())
                                                                           .specName(spec.getName())
                                                                           .value(bookSpec.getValue())
                                                                           .build();
                                         })
                                         .toList())
                          .build();
    }
}
