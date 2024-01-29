package main.controller;

import main.model.*;
import main.repository.TownRepository;
import main.repository.UserRepository;
import main.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth")
public class AuthRestController {
    @Autowired
    private TownRepository townRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationRequest registrationRequest) {
        String email = registrationRequest.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return new ResponseEntity<>("Error: email is already in use!", HttpStatus.FORBIDDEN);
        }
        if (!validateEmail(email)) {
            return new ResponseEntity<>("Error: email is Bad!", HttpStatus.FORBIDDEN);
        }
        String firstname = registrationRequest.getFirstname();
        if (firstname.isBlank()) {
            return new ResponseEntity<>("Error: firstname is Blank!", HttpStatus.FORBIDDEN);
        }
        String lastname = registrationRequest.getLastname();
        Long townId = (long) registrationRequest.getTownId();
        if (!townRepository.existsById(townId)) {
            return new ResponseEntity<>("Error: town_id is Bad!", HttpStatus.FORBIDDEN);
        }
        String password = registrationRequest.getPassword();
        if (!validatePassword(password)) {
            return new ResponseEntity<>("Error: password is Bad!", HttpStatus.FORBIDDEN);
        }
        User user = new User(null, email, firstname, lastname, encoder.encode(password),
                townRepository.getById(townId), Role.USER, "", 0L);
        userRepository.save(user);
        return login(new LoginRequest(email, password));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail().toLowerCase();
            String password = loginRequest.getPassword();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Error: email not exist"));
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            Role role = user.getRole();
            Long userId = user.getId();
            String accessToken = jwtUtils.createAccessToken(email, role);
            String refreshToken = jwtUtils.createRefreshToken(userId);
            Map<Object, Object> response = new HashMap<>();
            response.put("user_id", userId);
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRefreshRequest request) {
        Long userId = request.getUserId();
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>("Error: user not exist", HttpStatus.FORBIDDEN);
        }
        String email = optionalUser.get().getEmail();
        String refreshToken = request.getRefreshToken();
        if (!jwtUtils.validateRefreshToken(refreshToken, userId)) {
            return new ResponseEntity<>("Error: refresh_token is Bad", HttpStatus.FORBIDDEN);
        }
        String accessToken = jwtUtils.createAccessToken(email, optionalUser.get().getRole());
        refreshToken = jwtUtils.createRefreshToken(userId);
        Map<Object, Object> response = new HashMap<>();
        response.put("user_id", userId);
        response.put("access_token", accessToken);
        response.put("refresh_token", refreshToken);
        return ResponseEntity.ok(response);
    }

    private boolean validateEmail(String email) {
        String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePassword(String password) {
        String EMAIL_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
