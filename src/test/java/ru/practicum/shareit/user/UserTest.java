package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    public void testEquals() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        User user3 = new User();
        user3.setId(2L);

        Assertions.assertEquals(user1, user2);
        Assertions.assertNotEquals(user1, user3);
    }

    @Test
    public void testHashCode() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        User user3 = new User();
        user3.setId(2L);

        Assertions.assertEquals(user1.hashCode(), user2.hashCode());
        Assertions.assertNotEquals(user1.hashCode(), user3.hashCode());
    }
}