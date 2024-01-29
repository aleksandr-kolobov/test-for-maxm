package main.controller;

import main.model.Town;
import main.repository.TownRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/towns")
public class TownRestController {
    @Autowired
    private TownRepository townRepository;

    @GetMapping
//    @PreAuthorize("hasRole('USER')")
    public List<Town> getAll() {
        return new ArrayList<>(townRepository.findAll());
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public Town getTown(@PathVariable Long id) {
        return townRepository.findById(id).orElse(null);
    }

//    @PostMapping
//    public Town addTown(@RequestBody Town town) {
//        String str = town.getName();
//        town.setName(str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase());
//        return townRepository.save(town);
//    }
//
//    @DeleteMapping("/{id}")
//    public void deleteById(@PathVariable Integer id) {
//        townRepository.deleteById(id);
//    }
}
