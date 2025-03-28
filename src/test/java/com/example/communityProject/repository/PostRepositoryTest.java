package com.example.communityProject.repository;

import com.example.communityProject.entity.Post;
import com.example.communityProject.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Post testPost1;
    private Post testPost2;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test1@example.com")
                .password("User@123")
                .nickname("tester1")
                .build());

        testPost1 = postRepository.save(Post.builder()
                .title("Post 1")
                .content("Content of Post 1")
                .user(testUser)
                .likes(0L)
                .build());

        testPost2 = postRepository.save(Post.builder()
                .title("Post 2")
                .content("Content of Post 2")
                .user(testUser)
                .likes(0L)
                .build());
    }

    @Test
    @DisplayName("모든 게시글 조회하기")
    void findAll() {
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(2);
        assertThat(posts).containsExactlyInAnyOrder(testPost1, testPost2);
    }

    @Test
    @DisplayName("특정 사용자 ID로 게시글 조회하기")
    void findByUserId() {
        List<Post> posts = postRepository.findByUserId(testUser.getId());
        assertThat(posts).hasSize(2);
        assertThat(posts).containsExactlyInAnyOrder(testPost1, testPost2);
    }

    @Test
    @DisplayName("좋아요 수 증가하기")
    void incrementLikes() {
        postRepository.incrementLikes(testPost1.getId());
        entityManager.flush(); // DB에 쿼리 반영
        entityManager.clear();
        Post updatedPost = postRepository.findById(testPost1.getId()).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getLikes()).isEqualTo(1); // Assuming initial likes count is 0
    }

    @Test
    @DisplayName("좋아요 수 감소하기")
    void decrementLikes() {
        postRepository.incrementLikes(testPost1.getId());
        entityManager.flush(); // DB에 쿼리 반영
        entityManager.clear();
        postRepository.decrementLikes(testPost1.getId());
        entityManager.flush(); // DB에 쿼리 반영
        entityManager.clear();
        Post updatedPost = postRepository.findById(testPost1.getId()).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getLikes()).isEqualTo(0); // Should return to 0 after decrement
    }

    @Test
    @DisplayName("사용자 ID로 게시글 ID 조회하기")
    void findIdsByUser_Id() {
        List<Long> postIds = postRepository.findIdsByUserId(testUser.getId());
        assertThat(postIds).hasSize(2);
        assertThat(postIds).containsExactlyInAnyOrder(testPost1.getId(), testPost2.getId());
    }

    @Test
    @DisplayName("사용자의 게시글 삭제하기")
    void deleteByUser_Id() {
        postRepository.deleteByUserId(testUser.getId());
        List<Post> posts = postRepository.findAll();
        assertThat(posts).isEmpty();
    }
}
