package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.JwtAuthenticationResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.LoginRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ServerSignatureRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ServerSignatureResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.SignUpRequest;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.JwtTokenProvider;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserAuthentication;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.crypto.tink.subtle.Ed25519Sign;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthentication userAuthentication;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${app.privateKey}")
    private String privateKey;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    @PostMapping("/signin")
    @Deprecated
    //TODO: Remove this method in release
    public ResponseEntity<?> authenticateUserOne(@Valid @RequestBody ServerSignatureRequest request, HttpServletResponse response) {
        try {
            User user = userAuthentication.authenticate(request.getNric(),
                    request.getPassword(),
                    Role.create(request.getRole()));

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            int sessionId = SecureRandom.getInstance("SHA1PRNG").nextInt(Integer.MAX_VALUE);

            String jwt = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("session_id", sessionId)
                    .claim("role", user.getSelectedRole().toString())
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, jwtSecret)
                    .compact();

            Cookie newCookie = new Cookie("testCookie", jwt);
            newCookie.setPath("/api");
            newCookie.setHttpOnly(true);
            response.addCookie(newCookie);

            //TODO: set cookie to secure for production when we have https up
            //newCookie.setSecure(true);

            response.addCookie(newCookie);

            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
        } catch (GeneralSecurityException gse) {
            throw new BadCredentialsException("Bad credentials.");
        }
    }

    @PostMapping("/firstAuthorization")
    public ServerSignatureResponse getServerSignature(@Valid @RequestBody ServerSignatureRequest serverSignatureRequest) {
        try {
            userAuthentication.authenticate(serverSignatureRequest.getNric(),
                    serverSignatureRequest.getPassword(),
                    Role.create(serverSignatureRequest.getRole()));
        } catch (GeneralSecurityException gse) {
            throw new BadCredentialsException("Bad credentials.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            String msg = "0";
            byte[] msgHash =  digest.digest(msg.getBytes("UTF-8"));

            Ed25519Sign signer = new Ed25519Sign(Base64.getDecoder().decode(privateKey));
            byte[] signature = signer.sign(msgHash);

            return new ServerSignatureResponse(msgHash, signature);
        } catch (Exception e) {
            throw new AssertionError("Errors should not happen.");
        }
    }

    @PostMapping("/secondAuthorization")
    public ResponseEntity<?> authenticateUserTwo(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UserAuthenticationToken(
                        loginRequest.getNric(),
                        loginRequest.getPassword(),
                        Role.create(loginRequest.getRole()),
                        Base64.getDecoder().decode(loginRequest.getSignature()),
                        Base64.getDecoder().decode(loginRequest.getData())
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            int sessionId = SecureRandom.getInstance("SHA1PRNG").nextInt(Integer.MAX_VALUE);
            String jwt = tokenProvider.generateToken(sessionId, authentication);

            Cookie newCookie = new Cookie("testCookie", jwt);
            newCookie.setPath("/api");
            newCookie.setHttpOnly(true);
            response.addCookie(newCookie);

            //TODO: set cookie to secure for production when we have https up
            //newCookie.setSecure(true);

            response.addCookie(newCookie);

            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
        } catch (NoSuchAlgorithmException nsae) {
            throw new AssertionError("Algorithm should exist.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByNric(signUpRequest.getNric())) {
            return new ResponseEntity<>(new ApiResponse(false, "Nric is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(
                signUpRequest.getNric(),
                signUpRequest.getName(),
                signUpRequest.getEmail(),
                signUpRequest.getPhone(),
                signUpRequest.getAddress(),
                signUpRequest.getAge(),
                Gender.valueOf(signUpRequest.getGender().toUpperCase()),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getPublicKey(),
                new HashSet<>(signUpRequest.getRoles().stream()
                        .map(Role::create)
                        .collect(Collectors.toList()))
        );

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{nric}")
                .buildAndExpand(result.getNric()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}
