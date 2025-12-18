package vn.clone.fahasa_backend.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.*;
import vn.clone.fahasa_backend.domain.response.CartItemDTO;
import vn.clone.fahasa_backend.domain.response.ShippingAddressResponseDTO;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;

@Repository
@RequiredArgsConstructor
public class ShippingAddressRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    public List<ShippingAddressResponseDTO> findAllAddressesByAccountId(Integer accountId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query for data
        CriteriaQuery<ShippingAddressResponseDTO> query = cb.createQuery(ShippingAddressResponseDTO.class);
        Root<ShippingAddress> root = query.from(ShippingAddress.class);

        Join<ShippingAddress, Ward> ward = root.join("ward", JoinType.LEFT);
        Join<Ward, District> district = ward.join("district", JoinType.LEFT);
        Join<District, Province> province = district.join("province", JoinType.LEFT);

        query.select(cb.construct(ShippingAddressResponseDTO.class,
                                  root.get("id"),
                                  ward.get("id"),
                                  root.get("receiverName"),
                                  root.get("receiverPhone"),
                                  province.get("name"),
                                  district.get("name"),
                                  ward.get("name"),
                                  root.get("detailAddress"),
                                  root.get("isDefault")
        ));

        // Apply specification predicate get shipping address by account id
        Specification<ShippingAddress> specification = SpecificationsBuilder.hasAccountId(accountId);
        Predicate predicate = specification.toPredicate(root, query, cb);
        query.where(predicate);

        // Execute query
        return entityManager.createQuery(query).getResultList();
    }
}
