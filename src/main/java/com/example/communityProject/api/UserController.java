package com.example.communityProject.api;

import com.example.communityProject.dto.UserDto;
import com.example.communityProject.entity.Image;
import com.example.communityProject.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    // 사용자 프로필 목록 조회
    @GetMapping("/api/users")
    public ResponseEntity<List<UserDto>> getUserList() {
        List<UserDto> userList = userService.getUserList();
        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }

    // 사용자 프로필 조회
    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto dto = userService.getUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    // 사용자 프로필 등록
    @PostMapping("/api/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto dto) {
        UserDto createdDto = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.OK).body(createdDto);
    }

    // 사용자 프로필 수정
    @PatchMapping("/api/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
        UserDto updatedDto = userService.updateUser(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedDto);
    }

    // 사용자 삭제
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable Long id){
        UserDto deletedDto = userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<UserDto> loginUser(@RequestBody UserDto userDto) {
        UserDto authenticatedUser = userService.authenticateUser(userDto.getEmail(), userDto.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body(authenticatedUser);
    }


    // 프로필 이미지 업로드
    @PostMapping("/api/images/user/{userId}")
    public ResponseEntity<UserDto> uploadProfileImage(@PathVariable Long userId, @RequestParam("file") MultipartFile file)throws IOException {
        try {
            String imageUrl = userService.saveImageToLocalFile(file, userId);
            log.info(imageUrl);
            UserDto updatedUser = userService.updateProfileImage(userId, imageUrl);
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // 프로필 이미지 조회
    @GetMapping("/api/images/user/{id}")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        String imagePath = userService.getProfileImagePath(id);
        if (imagePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(imagePath);

        try {
            byte[] imageData = Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
