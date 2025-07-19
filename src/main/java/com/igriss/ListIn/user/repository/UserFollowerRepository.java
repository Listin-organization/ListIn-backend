package com.igriss.ListIn.user.repository;

import com.igriss.ListIn.user.dto.FollowsDTO;
import com.igriss.ListIn.user.entity.UserFollower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserFollowerRepository extends JpaRepository<UserFollower, UserFollower.UserFollowerId> {

    @Query(value = """
            SELECT
                u.user_id AS userId,
                u.nick_name AS nickName,
                u.profile_image_path AS profileImagePath,
                u.following AS following,
                u.followers AS followers
            FROM user_followers uf
            JOIN users u ON uf.follower_id = u.user_id
            WHERE uf.following_id = :userId
            ORDER BY u.nick_name
            """,
            countQuery = """
                SELECT COUNT(*)
                FROM user_followers uf
                WHERE uf.following_id = :userId
            """,
            nativeQuery = true)
    Page<FollowsDTO> findAllFollowers(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
            SELECT
                u.user_id AS userId,
                u.nick_name AS nickName,
                u.profile_image_path AS profileImagePath,
                u.following AS following,
                u.followers AS followers
            FROM user_followers uf
            JOIN users u ON uf.following_id = u.user_id
            WHERE uf.follower_id = :userId
            ORDER BY u.nick_name
            """,
            countQuery = """
                SELECT COUNT(*)
                FROM user_followers uf
                WHERE uf.follower_id = :userId
            """,
            nativeQuery = true)
    Page<FollowsDTO> findAllFollowings(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
        SELECT u.user_id
        FROM user_followers uf
        JOIN users u ON uf.following_id = u.user_id
        WHERE uf.follower_id = :userId
        ORDER BY u.nick_name
        """,
            nativeQuery = true)
    List<UUID> findFollowings(@Param("userId") UUID userId);

    Boolean existsByFollower_UserIdAndFollowing_UserId(UUID followerUserId, UUID followingUserId);
}
