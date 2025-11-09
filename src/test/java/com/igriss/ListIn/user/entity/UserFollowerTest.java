package com.igriss.ListIn.user.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserFollowerTest {

    @Test
    void testEquals_equalObjects() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();
        UserFollower.UserFollowerId id1 = new UserFollower.UserFollowerId(followerId, followingId);
        UserFollower.UserFollowerId id2 = new UserFollower.UserFollowerId(followerId, followingId);

        assertEquals(id1, id2);
        assertEquals(id2, id1);
    }

    @Test
    void testEquals_differentFollower() {
        UUID followerId1 = UUID.randomUUID();
        UUID followerId2 = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        UserFollower.UserFollowerId id1 = new UserFollower.UserFollowerId(followerId1, followingId);
        UserFollower.UserFollowerId id2 = new UserFollower.UserFollowerId(followerId2, followingId);

        assertNotEquals(id1, id2);
    }

    @Test
    void testEquals_differentFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followingId1 = UUID.randomUUID();
        UUID followingId2 = UUID.randomUUID();

        UserFollower.UserFollowerId id1 = new UserFollower.UserFollowerId(followerId, followingId1);
        UserFollower.UserFollowerId id2 = new UserFollower.UserFollowerId(followerId, followingId2);

        assertNotEquals(id1, id2);
    }

    @Test
    void testEquals_nullAndDifferentClass() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();
        UserFollower.UserFollowerId id = new UserFollower.UserFollowerId(followerId, followingId);

        assertNotEquals(null, id);
    }

    @Test
    void testHashCode_equalObjects() {
        UUID followerId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();
        UserFollower.UserFollowerId id1 = new UserFollower.UserFollowerId(followerId, followingId);
        UserFollower.UserFollowerId id2 = new UserFollower.UserFollowerId(followerId, followingId);

        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testHashCode_differentObjects() {
        UUID id1Follower = UUID.randomUUID();
        UUID id1Following = UUID.randomUUID();
        UUID id2Follower = UUID.randomUUID();
        UUID id2Following = UUID.randomUUID();

        UserFollower.UserFollowerId id1 = new UserFollower.UserFollowerId(id1Follower, id1Following);
        UserFollower.UserFollowerId id2 = new UserFollower.UserFollowerId(id2Follower, id2Following);

        assertNotEquals(id1.hashCode(), id2.hashCode());
    }
}
