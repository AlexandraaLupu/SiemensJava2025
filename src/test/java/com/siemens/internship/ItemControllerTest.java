package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAsync
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Item createItem(String name, String desc, String status, String email) {
        Item item = new Item(null, name, desc, status, email);
        return itemRepository.save(item);
    }

    @BeforeEach
    void clearDb() {
        itemRepository.deleteAll();
    }

    @Test
    void testGetAllItems() throws Exception {
        createItem("Item1", "D", "NEW", "a@example.com");
        createItem("Item2", "D", "NEW", "b@example.com");

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
    }

    @Test
    void testCreateItem_Valid() throws Exception {
        Item item = new Item(null, "a", "a", "a", "test@example.com");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("a"));
    }

    @Test
    void testCreateItem_InvalidEmail() throws Exception {
        Item item = new Item(null, "a", "a", "a", "invalid-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input"));
    }

    @Test
    void testGetItemById_Found() throws Exception {
        Item saved = createItem("a", "a", "a", "test@example.com");

        mockMvc.perform(get("/api/items/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("a"));
    }

    @Test
    void testGetItemById_NotFound() throws Exception {
        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateItem_Valid() throws Exception {
        Item saved = createItem("a", "a", "a", "a@example.com");
        Item updated = new Item(null, "b", "b", "b", "b@example.com");

        mockMvc.perform(put("/api/items/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("b"))
                .andExpect(jsonPath("$.status").value("b"));
    }

    @Test
    void testUpdateItem_InvalidEmail() throws Exception {
        Item saved = createItem("a", "a", "a", "a@example.com");
        saved.setEmail("invalid-email");

        mockMvc.perform(put("/api/items/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input"));
    }

    @Test
    void testUpdateItem_NotFound() throws Exception {
        Item updated = new Item(null, "X", "Y", "Z", "x@example.com");

        mockMvc.perform(put("/api/items/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item not found"));
    }

    @Test
    void testDeleteItem_Found() throws Exception {
        Item saved = createItem("a", "a", "a", "a@example.com");

        mockMvc.perform(delete("/api/items/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteItem_NotFound() throws Exception {
        mockMvc.perform(delete("/api/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        createItem("A", "Desc A", "NEW", "a@example.com");
        createItem("B", "Desc B", "NEW", "b@example.com");

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk());

        Thread.sleep(3000); // wait for @Async processing to finish

        List<Item> items = itemRepository.findAll();
        for (Item item : items) {
            assertEquals("PROCESSED", item.getStatus());
        }
    }
}
