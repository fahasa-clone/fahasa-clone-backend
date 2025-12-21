package vn.clone.fahasa_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.Spec;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);

    @Query("SELECT cs.spec FROM CategorySpec cs WHERE cs.category.id IN :categoryIds")
    List<Spec> findSpecsByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);
}
