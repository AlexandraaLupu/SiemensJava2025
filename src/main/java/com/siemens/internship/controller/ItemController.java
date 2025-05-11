package com.siemens.internship.controller;

import com.siemens.internship.service.ItemService;
import com.siemens.internship.model.Item;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) { // removed @Autowired field injection
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) { // replaced item with ? because the body can contain an Item or String
        if (result.hasErrors()) {
            return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST); // if result has errors -> bad request
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED); // no errors -> created
    }


    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // replaced NO_CONTENT with NOT_FOUND
        // if there is no item with that id -> no item found
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) { // if validation fails
            return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
        }
        Optional<Item> updateItem = itemService.updateItem(id, item); // created an update function in Service

        if (updateItem.isPresent()) {
            return ResponseEntity.ok(updateItem.get());
        } else {
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        boolean deleted = itemService.deleteById(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // the item was deleted
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // the item was not found
    }


    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() { // the function is async
        return itemService.processItemsAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Collections.emptyList()));
    }
}
