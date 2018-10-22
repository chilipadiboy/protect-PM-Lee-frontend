package org.cs4239.team1.protectPMLeefrontendserver.controller;

import com.google.crypto.tink.subtle.Ed25519Sign;
import com.google.crypto.tink.subtle.Ed25519Verify;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.*;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.JwtEncryptionDecryptionTool;
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

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

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
    private JwtEncryptionDecryptionTool jwtEncryptionDecryptionTool;

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

            byte[] ivBytes = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(ivBytes);
            String iv = Base64.getEncoder().encodeToString(ivBytes);
            String jwt = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("session_id", iv)
                    .claim("role", user.getSelectedRole().toString())
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, jwtSecret)
                    .compact();
            byte[] encrypted = jwtEncryptionDecryptionTool.encrypt(jwt, ivBytes);

            String cookieValue = Base64.getEncoder().encodeToString(encrypted);
            Cookie newCookie = new Cookie("testCookie", cookieValue);
            newCookie.setPath("/api");
            newCookie.setHttpOnly(true);

            //TODO: set cookie to secure for production when we have https up
            //newCookie.setSecure(true);

            response.addCookie(newCookie);

            return ResponseEntity.ok(new SessionIdResponse(iv));
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
        // GET MSGHASH & SIGNATURE
            // GET HASH OF NONCE
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            int nonceInServer = 0; // TODO: this has to be an incremental nonce that the server has to keep track of...
            String nonce = Integer.toString(nonceInServer);
            byte[] nonceBytes = nonce.getBytes("UTF-8");
            byte[] msgHash =  digest.digest(nonceBytes);

            // GET SIGNATURE OF HASH OF NONCE
            Ed25519Sign signer = new Ed25519Sign(Base64.getDecoder().decode(privateKey));
            byte[] signature = signer.sign(msgHash);

            // JOIN ABOVE ARRAYS
            byte[] combined = new byte[msgHash.length + signature.length];
            System.arraycopy(msgHash, 0, combined, 0, msgHash.length);
            System.arraycopy(signature, 0, combined, msgHash.length, signature.length);

        // ENCRYPT MSG HASH & SIGNATURE
            String tagBluetoothEncryptionKey = "D2edHtPLRkUXYMCRA3NLeQ==";
            byte[] ivBytes = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(tagBluetoothEncryptionKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(combined);

            return new ServerSignatureResponse(ivBytes, encrypted);

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
                        Base64.getDecoder().decode(loginRequest.getEncryptedString()),
                        Base64.getDecoder().decode(loginRequest.getIv())
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            // GET DECRYPTED
            String tagBluetoothEncryptionKey = "D2edHtPLRkUXYMCRA3NLeQ==";
            System.out.println("IV is "+ Base64.getDecoder().decode(loginRequest.getIv()));
            IvParameterSpec tagIv = new IvParameterSpec(Base64.getDecoder().decode(loginRequest.getIv()));
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(tagBluetoothEncryptionKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, tagIv);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(loginRequest.getEncryptedString()));
            byte[] msgHash = Arrays.copyOfRange(decrypted, 0, 64);
            byte[] signature = Arrays.copyOfRange(decrypted, 64, 128);

            //TODO for zhiyuan: should be calling this but idk how to : loadedUser.getPublicKey()
            String tagPublicKey = "MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=";
            Ed25519Verify verifier = new Ed25519Verify(Base64.getDecoder().decode(tagPublicKey));
            verifier.verify(signature, msgHash);

            //VERIFY NONCE OF TAG
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            int nonceInServer = 0; ////this has to be an incremental nonce that the server has to keep track of...
            String nonce = Integer.toString(nonceInServer);
            byte[] nonceBytes = nonce.getBytes("UTF-8");
            byte[] verifyHash =  digest.digest(nonceBytes);
            nonceInServer++; //need to increment nonce here!

            if (Arrays.equals(verifyHash, msgHash)) {
                byte[] ivBytes = new byte[16];
                SecureRandom.getInstanceStrong().nextBytes(ivBytes);
                String iv = Base64.getEncoder().encodeToString(ivBytes);
                String jwt = tokenProvider.generateToken(iv, authentication);
                byte[] encrypted = jwtEncryptionDecryptionTool.encrypt(jwt, ivBytes);

                String cookieValue = Base64.getEncoder().encodeToString(encrypted);
                Cookie newCookie = new Cookie("testCookie", cookieValue);
                newCookie.setPath("/api");
                newCookie.setHttpOnly(true);

                //TODO: set cookie to secure for production when we have https up
                //newCookie.setSecure(true);

                response.addCookie(newCookie);

                return ResponseEntity.ok(new SessionIdResponse(iv));
            }
            else {
                //TODO for zhiyuan: I need to return an unauthorised response if the hash does not match meaning nonce is not verified
                // - but i cant figure out
                //how to do it for responseEntity<T>
                return ResponseEntity.ok(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
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
