package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.LoginRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ServerSignatureRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ServerSignatureResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.SessionIdResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.SignUpRequest;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.AESEncryptionDecryptionTool;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.security.Hasher;
import org.cs4239.team1.protectPMLeefrontendserver.security.JwtTokenProvider;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserAuthentication;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Autowired
    private AESEncryptionDecryptionTool aesEncryptionDecryptionTool;

    @Value("${app.privateKey}")
    private String privateKey;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @PostMapping("/signin")
    @Deprecated
    //TODO: Remove this method in release
    public ResponseEntity<?> authenticateUserOne(@Valid @RequestBody ServerSignatureRequest request, HttpServletResponse response) {
        logger.info("Accessing AuthController#authenticateUserOne", request);
        try {
            User user = userAuthentication.authenticate(request.getNric(),
                    request.getPassword(),
                    Role.create(request.getRole()));

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            byte[] ivBytes = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(ivBytes);
            String iv = Base64.getEncoder().encodeToString(ivBytes);
            String jwt = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("session_id", iv)
                    .claim("role", user.getSelectedRole().toString())
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, jwtSecret)
                    .compact();
            byte[] encrypted = aesEncryptionDecryptionTool.encrypt(jwt, jwtSecret, ivBytes, "AES/CBC/PKCS5PADDING");

            String cookieValue = Base64.getEncoder().encodeToString(encrypted);
            Cookie newCookie = new Cookie("sessionCookie", cookieValue);
            newCookie.setPath("/api");
            newCookie.setHttpOnly(true);

            //TODO: set cookie to secure for production when we have https up
            //newCookie.setSecure(true);

            response.addCookie(newCookie);
            response.setHeader("Set-Cookie", response.getHeader("Set-Cookie") + "; SameSite=strict");

            return ResponseEntity.ok(new SessionIdResponse(iv));
        } catch (GeneralSecurityException gse) {
            throw new BadCredentialsException("Bad credentials.");
        }
    }

    @PostMapping("/firstAuthorization")
    public ResponseEntity<?> getServerSignature(@Valid @RequestBody ServerSignatureRequest serverSignatureRequest) {
        logger.info("Accessing AuthController#getServerSignature", serverSignatureRequest);
        try {
            userAuthentication.authenticate(serverSignatureRequest.getNric(),
                    serverSignatureRequest.getPassword(),
                    Role.create(serverSignatureRequest.getRole()));
        } catch (GeneralSecurityException gse) {
            throw new BadCredentialsException("Bad credentials.");
        }

        try {
            User patient = userRepository.findByNric(serverSignatureRequest.getNric())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "nric", serverSignatureRequest.getNric()));
            byte[] pubKey = Base64.getDecoder().decode(patient.getPublicKey());

            int nonce = patient.getNonce();
            if (nonce > 30) {
                return new ResponseEntity<>(new ApiResponse(false, "Number of nonces requested for the day exceeded."), HttpStatus.BAD_REQUEST);
            }

            byte[] msgHash = Hasher.hash(nonce);
            byte[] ivBytes = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(ivBytes);
            byte[] encrypted = aesEncryptionDecryptionTool.encrypt(msgHash, patient.getSymmetricKey(), ivBytes, "AES/CBC/NOPADDING");
            byte[] loginCode = Integer.toString(0).getBytes();

            byte[] combined = new byte[loginCode.length + pubKey.length + ivBytes.length + encrypted.length];
            System.arraycopy(loginCode, 0, combined, 0, loginCode.length);
            System.arraycopy(pubKey, 0, combined, loginCode.length, pubKey.length);
            System.arraycopy(ivBytes, 0, combined, loginCode.length+pubKey.length, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, loginCode.length+pubKey.length+ivBytes.length, encrypted.length);
            Ed25519Sign signer = new Ed25519Sign(Base64.getDecoder().decode(privateKey));
            byte[] signature = signer.sign(combined);

            return ResponseEntity.ok(new ServerSignatureResponse(ivBytes, combined, signature));

        } catch (Exception e) {
            throw new AssertionError("Errors should not happen.");
        }
    }

    @PostMapping("/secondAuthorization")
    public ResponseEntity<?> authenticateUserTwo(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        logger.info("ccessing AuthController#authenticateUserTwo", loginRequest);
        try {
            User patient = userRepository.findByNric(loginRequest.getNric())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "nric", loginRequest.getNric()));
            byte[] decrypted = aesEncryptionDecryptionTool
                    .decrypt(Base64.getDecoder().decode(loginRequest.getEncryptedString()), patient.getSymmetricKey(), loginRequest.getIv(), "AES/CBC/NOPADDING");
            byte[] msgHash = Arrays.copyOfRange(decrypted, 0, 64);
            byte[] signature = Arrays.copyOfRange(decrypted, 64, 128);

            Authentication authentication = authenticationManager.authenticate(
                    new UserAuthenticationToken(
                            loginRequest.getNric(),
                            loginRequest.getPassword(),
                            Role.create(loginRequest.getRole()),
                            msgHash,
                            signature,
                            Base64.getDecoder().decode(loginRequest.getIv())
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            patient.setNonce(patient.getNonce() + 1);
            patient.setNumOfNonceUsed(patient.getNumOfNonceUsed() + 1);
            userRepository.save(patient);
            byte[] ivBytes = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(ivBytes);
            String iv = Base64.getEncoder().encodeToString(ivBytes);
            String jwt = tokenProvider.generateToken(iv, authentication);
            byte[] encrypted = aesEncryptionDecryptionTool.encrypt(jwt, jwtSecret, ivBytes, "AES/CBC/PKCS5PADDING");

            String cookieValue = Base64.getEncoder().encodeToString(encrypted);
            Cookie newCookie = new Cookie("sessionCookie", cookieValue);
            newCookie.setPath("/api");
            newCookie.setHttpOnly(true);

            //TODO: set cookie to secure for production when we have https up
            //newCookie.setSecure(true);

            response.addCookie(newCookie);
            response.setHeader("Set-Cookie", response.getHeader("Set-Cookie") + "; SameSite=strict");

            return ResponseEntity.ok(new SessionIdResponse(iv));
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest, @CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing AuthController#registerUser", signUpRequest);
        if (userRepository.existsByNric(signUpRequest.getNric())) {
            return new ResponseEntity<>(new ApiResponse(false, "Nric is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        if (currentUser.getSelectedRole().equals(Role.ROLE_EXTERNAL_PARTNER)
                && signUpRequest.getRoles().contains("administrator")) {
            return new ResponseEntity<>(new ApiResponse(false, "External partners are not allowed to create administrator accounts!"),
                    HttpStatus.BAD_REQUEST);
        }

        User user = new User(
                signUpRequest.getNric(),
                signUpRequest.getName(),
                signUpRequest.getEmail(),
                signUpRequest.getPhone(),
                signUpRequest.getAddress(),
                signUpRequest.getPostalCode(),
                signUpRequest.getAge(),
                Gender.valueOf(signUpRequest.getGender().toUpperCase()),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getPublicKey(),
                "",
                0,
                0,
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

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    private void resetDailyNonceLimit() {
        userRepository.findAll().forEach(user ->  {
            user.setNumOfNonceUsed(0);
            userRepository.save(user);
        });
    }
}
