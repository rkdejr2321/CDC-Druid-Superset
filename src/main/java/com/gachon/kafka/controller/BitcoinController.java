package com.gachon.kafka.controller;

import com.gachon.kafka.srcDB.model.Bitcoin;
import com.gachon.kafka.srcDB.repository.BitcoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coin")
@RequiredArgsConstructor
public class BitcoinController {

    private final BitcoinRepository bitcoinRepository;

    @GetMapping("/{id}")
    public Bitcoin findById(@PathVariable int id){
        return bitcoinRepository.findById(id).orElse(null);
    }

    @PostMapping("")
    public Bitcoin saveUser(@RequestBody Bitcoin bitcoin){
        return bitcoinRepository.save(bitcoin);
    }

    @PutMapping("/{id}")
    public Bitcoin updateUser(@PathVariable int id, @RequestBody Bitcoin bitcoinDto) {
        Bitcoin bitcoin = bitcoinRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 id가 업습니다."));
        bitcoin.update(bitcoinDto.getId());
        return bitcoinRepository.save(bitcoin);}

    @DeleteMapping("/{id}")
    public int deleteUser(@PathVariable int id){
        bitcoinRepository.deleteById(id);
        return id;
    }

}
