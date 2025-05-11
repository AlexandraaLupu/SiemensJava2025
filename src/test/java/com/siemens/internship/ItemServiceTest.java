package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@EnableAsync
class ItemServiceTest {

    @Autowired
    private ItemRepository itemRepository;

    private ItemService itemService;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
        itemService = new ItemService(itemRepository);
    }

    private Item createTestItem(String name, String description, String status, String email) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setStatus(status);
        item.setEmail(email);
        return itemRepository.save(item);
    }

    @Test
    void testSaveItem() {
        Item item = new Item();
        item.setName("a");
        item.setDescription("a");
        item.setStatus("NEW");
        item.setEmail("a@example.com");

        Item saved = itemService.save(item);
        assertNotNull(saved.getId());
        assertEquals("a", saved.getName());
        assertEquals("a@example.com", saved.getEmail());
    }

    @Test
    void testFindAll() {
        createTestItem("a", "a", "NEW", "a@example.com");
        createTestItem("b", "b", "NEW", "b@example.com");

        List<Item> items = itemService.findAll();
        assertEquals(2, items.size());
    }

    @Test
    void testFindById() {
        Item item = createTestItem("a", "a", "NEW", "a@example.com");
        Optional<Item> found = itemService.findById(item.getId());

        assertTrue(found.isPresent());
        assertEquals("a", found.get().getName());
        assertEquals("a@example.com", found.get().getEmail());
    }

    @Test
    void testUpdateItem() {
        Item item = createTestItem("a", "a", "NEW", "a@example.com");

        Item updated = new Item();
        updated.setName("b");
        updated.setDescription("b");
        updated.setStatus("PROCESSED");
        updated.setEmail("b@example.com");

        Optional<Item> result = itemService.updateItem(item.getId(), updated);

        assertTrue(result.isPresent());
        assertEquals("b", result.get().getName());
        assertEquals("b@example.com", result.get().getEmail());
        assertEquals("PROCESSED", result.get().getStatus());
    }

    @Test
    void testUpdateNonExistentItem() {
        Item updated = new Item();
        updated.setName("a");
        updated.setDescription("a");
        updated.setStatus("a");
        updated.setEmail("a@example.com");

        Optional<Item> result = itemService.updateItem(999L, updated);
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteById() {
        Item item = createTestItem("a", "a", "NEW", "a@example.com");
        boolean deleted = itemService.deleteById(item.getId());

        assertTrue(deleted);
        assertFalse(itemRepository.existsById(item.getId()));
    }

    @Test
    void testDeleteNonExistentId() {
        boolean deleted = itemService.deleteById(12345L);
        assertFalse(deleted);
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        createTestItem("Item A", "Desc A", "NEW", "a@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        createTestItem("Item B", "Desc B", "NEW", "b@example.com");
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();

        // wait for async processing to complete
        List<Item> result = future.get();

        assertNotNull(result);
        assertEquals(16, result.size());

        for (Item item : result) {
            assertEquals("PROCESSED", item.getStatus());
            Item dbItem = itemRepository.findById(item.getId()).orElseThrow();
            assertEquals("PROCESSED", dbItem.getStatus());
        }
    }
}
