package via.pro3.paymentserver.repositories;

import via.pro3.paymentserver.model.ValidCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IValidCardRepository extends JpaRepository<ValidCard, String> {
    // Spring Data JPA automatically provides:
    // - save(ValidCard) for add/update
    // - findById(String) for getSingle
    // - findAll() for getMany
    // - deleteById(String) for delete
    // - delete(ValidCard) for delete by entity
    
    Optional<ValidCard> findByCardNumber(String cardNumber);
}

