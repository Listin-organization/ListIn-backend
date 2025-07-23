package com.igriss.ListIn.publication.controller;

import com.igriss.ListIn.publication.dto.PublicationRequestDTO;
import com.igriss.ListIn.publication.dto.PublicationResponseDTO;
import com.igriss.ListIn.publication.dto.UpdatePublicationRequestDTO;
import com.igriss.ListIn.publication.dto.page.PageResponse;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationAttributeValue;
import com.igriss.ListIn.publication.repository.PublicationAttributeValueRepository;
import com.igriss.ListIn.publication.repository.PublicationRepository;
import com.igriss.ListIn.publication.service.PublicationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
@Slf4j
public class PublicationController {

    private final PublicationService publicationService;
    private final PublicationRepository publicationRepository;

    private final PublicationAttributeValueRepository repo;

    @Operation(summary = "${publication-controller.save.summary}", description = "${publication-controller.save.description}")
    @PostMapping
    public ResponseEntity<UUID> savePublication(@RequestBody PublicationRequestDTO request, Authentication connectedUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publicationService.savePublication(request, connectedUser));
    }

    @Operation(summary = "${publication-controller.user-publications.summary}", description = "${publication-controller.user-publications.description}")
    @GetMapping("/user-publications")
    public ResponseEntity<PageResponse<PublicationResponseDTO>> getPublicationsOfUser(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                                      @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                                      Authentication connectedUser) {
        return ResponseEntity.ok(publicationService.findAllByUser(page, size, connectedUser));
    }

    @Operation(summary = "${publication-controller.get-by-id.summary}", description = "${publication-controller.get-by-id.description}")
    @GetMapping("/{publicationId}") // todo -> to be modified, this is for test used only !
    public ResponseEntity<Publication> getPublicationById(@PathVariable UUID publicationId) {
        return ResponseEntity.ok(publicationRepository.findById(publicationId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/PAV/{publicationId}") // todo -> to be modified, this is for test used only !
    public ResponseEntity<List<PublicationAttributeValue>> getPAV(@PathVariable UUID publicationId) {
        return ResponseEntity.ok(repo.findByPublication_Id(publicationId));
    }

    @Operation(summary = "${publication-controller.update.summary}", description = "${publication-controller.update.description}")
    @PatchMapping("/update/{publicationId}")
    public ResponseEntity<PublicationResponseDTO> updatePublication(@PathVariable UUID publicationId, @RequestBody UpdatePublicationRequestDTO updatePublication, Authentication authentication) {
        return ResponseEntity.ok(publicationService.updateUserPublication(publicationId, updatePublication, authentication));
    }

    @Operation(summary = "${publication-controller.find-by-user.summary}", description = "${publication-controller.find-by-user.description}")
    @GetMapping("/user/{userId}")
    public PageResponse<PublicationResponseDTO> findByUser(@PathVariable UUID userId,
                                                           @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                           @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                           Authentication connectedUser) {
        return publicationService.findAllByUserId(userId, page, size, connectedUser);
    }

    @Operation(summary = "${publication-controller.videos.summary}", description = "${publication-controller.videos.description}")
    @GetMapping({"/videos", "/videos/{pCategory}"})
    public PageResponse<PublicationResponseDTO> getVideos(@PathVariable(required = false) UUID pCategory,
                                                          @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                          @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                          Authentication connectedUser
    ) {
        return publicationService.findPublicationsContainingVideo(page, size, connectedUser, pCategory);
    }

    @PatchMapping("/like/{publicationId}")
    public UUID likePublication(@PathVariable UUID publicationId, Authentication connectedUser) {
        return publicationService.likePublication(publicationId, connectedUser);
    }

    @PatchMapping("/unlike/{publicationId}")
    public UUID unLikePublication(@PathVariable UUID publicationId, Authentication connectedUser) {
        return publicationService.unLikePublication(publicationId, connectedUser);
    }

    @GetMapping("/like")
    public PageResponse<PublicationResponseDTO> getLikedPublications(@RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "5") Integer size,
                                                                     Authentication connectedUser) {
        return publicationService.findAllLikedPublications(page, size, connectedUser);
    }

    @DeleteMapping("/delete/{publicationId}")
    public ResponseEntity<Object> deletePublication(@PathVariable UUID publicationId, Authentication authentication) {
        publicationService.deletePublication(publicationId, authentication);
        return ResponseEntity.ok().body("Publication deleted successfully");
    }

    @PostMapping("/view/{publicationId}")
    public ResponseEntity<UUID> viewPublication(@PathVariable UUID publicationId, Authentication connectedUser) {
        return ResponseEntity.ok(publicationService.viewPublication(publicationId, connectedUser));
    }

    @GetMapping("/following")
    public ResponseEntity<PageResponse<PublicationResponseDTO>> getFollowingsPublications(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                                          @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                                          Authentication connectedUser) {
        return ResponseEntity.ok(publicationService.getFollowingsPublications(page, size, connectedUser));
    }

    @GetMapping("/with-videos")
    public ResponseEntity<PageResponse<PublicationResponseDTO>> getUserPostsContainingVideos(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                                             @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                                             @RequestParam(name = "userId", required = false) String userId,
                                                                                             Authentication connectedUser) {
        return ResponseEntity.ok(publicationService.getVideoPublications(page, size, userId, connectedUser));
    }

    @GetMapping("/without-videos")
    public ResponseEntity<PageResponse<PublicationResponseDTO>> getUserPostsContainingPhotos(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                                             @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                                             @RequestParam(name = "userId", required = false) String userId,
                                                                                             Authentication connectedUser
    ) {
        return ResponseEntity.ok(publicationService.getPhotoPublications(page, size, userId, connectedUser));
    }
}
