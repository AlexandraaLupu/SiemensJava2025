package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository; // constructor in the place of @Autowired so that we don't have field injection
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public boolean deleteById(Long id){
        if (!itemRepository.existsById(id)) { // if the item ID doesn't exist
            return false;
        }
        itemRepository.deleteById(id);
        return true;
    }

    public Optional<Item> updateItem(Long id, Item updatedItem) { // created update function so that controller calls the method from service
        return itemRepository.findById(id).map(existingItem -> {
            updatedItem.setId(id);
            return itemRepository.save(updatedItem);
        });
    }



    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Item> items = itemRepository.findAll();               // get from DB all the items only once

        List<CompletableFuture<Item>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                            try { Thread.sleep(1000); }                    // simulate work
                            catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new IllegalStateException(
                                        "Interrupted while processing item " + item.getId(), ie);
                            }
                            item.setStatus("PROCESSED"); // got rid of the processedCount and processedItems list as we don't need them(we return the list)
                            return itemRepository.save(item);
                        }, executor)
                        .handle((saved, ex) -> {                          // error handling
                            if (ex != null) {
                                log.error("Failed to process item {}", item.getId(), ex);
                                return null;
                            }
                            return saved;
                        }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])) // combining futures in a list that we return
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList());
    }


}

