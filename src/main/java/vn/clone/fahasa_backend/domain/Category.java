package vn.clone.fahasa_backend.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AbstractEntity {
    @Column(name = "name")
    @NotBlank(message = "name field is required")
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @Transient
    private List<Category> children;

    public void addChild(Category category) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(category);
    }
}
