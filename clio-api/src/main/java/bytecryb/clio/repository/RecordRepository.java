package bytecryb.clio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bytecryb.clio.model.Record;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    
}
