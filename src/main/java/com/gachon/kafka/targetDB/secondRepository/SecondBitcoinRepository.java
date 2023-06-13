package com.gachon.kafka.targetDB.secondRepository;


//import com.gachon.kafka.targetDB.secondModel.SecondUser;
import com.gachon.kafka.srcDB.model.Bitcoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecondBitcoinRepository extends JpaRepository<Bitcoin, Integer> {
}
