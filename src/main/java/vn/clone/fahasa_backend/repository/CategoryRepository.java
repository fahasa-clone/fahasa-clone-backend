package vn.clone.fahasa_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.clone.fahasa_backend.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    public boolean existsByName(String name);
}
