package vn.clone.fahasa_backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    @NotBlank(message = "name field is required")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant created_at;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updated_at;

    @Transient
    private List<Category> children;

    public void addChild(Category category) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(category);
    }
}