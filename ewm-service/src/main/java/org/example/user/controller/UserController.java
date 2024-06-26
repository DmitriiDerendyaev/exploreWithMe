package org.example.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.example.user.service.UserService;
import org.example.subscription.dto.UserWithSubscribers;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUserAdmin(@Valid @RequestBody NewUserRequest newUserRequest) {
        return userService.addUserAdmin(newUserRequest);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsersAdmin(@RequestParam(required = false) List<Long> ids,
                                       @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                       @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return userService.getUsersAdmin(ids, PageRequest.of(from / size, size));
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserAdmin(@PathVariable Long id) {
        userService.deleteUserAdmin(id);
    }

    @PostMapping("/users/{userId}/subscriptions/{authorId}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserWithSubscribers addSubscriber(@PathVariable Long userId, @PathVariable Long authorId) {
        return userService.addSubscriber(userId, authorId);
    }

    @DeleteMapping("/users/{userId}/subscriptions/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubscriber(@PathVariable Long userId, @PathVariable Long authorId) {
        userService.deleteSubscriber(userId, authorId);
    }

    @GetMapping("/users/{userId}/subscriptions")
    @ResponseStatus(HttpStatus.OK)
    public UserWithSubscribers getUserWithSubscribers(@PathVariable Long userId,
                                                      @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                      @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return userService.getUserWithSubscribers(userId, PageRequest.of(from / size, size));
    }

}
