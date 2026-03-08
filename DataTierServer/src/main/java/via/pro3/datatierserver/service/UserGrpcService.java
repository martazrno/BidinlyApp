package via.pro3.datatierserver.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import via.pro3.datatierserver.grpc.DataTierProto;
import via.pro3.datatierserver.grpc.UserServiceGrpc;
import via.pro3.datatierserver.model.User;
import via.pro3.datatierserver.repositories.IUserRepository;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import via.pro3.datatierserver.security.AESUtil;
import via.pro3.datatierserver.security.PasswordHasher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.Security;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private IUserRepository userRepository;

    @Override
    public void createUser(DataTierProto.CreateUserRequest request, StreamObserver<DataTierProto.UserResponse> responseObserver) {
        try {
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                        .withDescription("Username is required")
                        .asRuntimeException());
                return;
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                        .withDescription("Password is required")
                        .asRuntimeException());
                return;
            }

            Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
            if (existingUser.isPresent()) {
                responseObserver.onError(io.grpc.Status.ALREADY_EXISTS
                        .withDescription("Username already exists: " + request.getUsername())
                        .asRuntimeException());
                return;
            }

            User newUser = new User();
            newUser.setUsername(request.getUsername());
            String hashedPassword = PasswordHasher.hash(request.getPassword());
            newUser.setPassword(hashedPassword);
            newUser.setRoleId(request.getRoleId());
            newUser.setIsActive(true);

            if (request.hasEmail() && !request.getEmail().trim().isEmpty()) {
                newUser.setEmail(request.getEmail());
            }

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            System.out.println("Public key for user: " + publicKeyBase64);
            System.out.println("Private key for user: " + privateKeyBase64);


           //Generate Self-Signed Certificate
            Security.addProvider(new BouncyCastleProvider());

            X500Name dnName = new X500Name("CN=" + request.getUsername());
            BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

            Date startDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 1);
            Date endDate = calendar.getTime();

            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
                    .build(privateKey);

            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    dnName,
                    serialNumber,
                    startDate,
                    endDate,
                    dnName,
                    publicKey
            );

            X509Certificate certificate = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certBuilder.build(contentSigner));

            String certificateBase64 =
                    Base64.getEncoder().encodeToString(certificate.getEncoded());

            System.out.println("Generated cert for user:");
            System.out.println(certificateBase64);

            //store keys & certificate in user entity
            newUser.setPublicKey(publicKeyBase64);
            String encryptedPrivateKey = AESUtil.encrypt(privateKeyBase64, hashedPassword);
            newUser.setPrivateKey(encryptedPrivateKey);
            newUser.setCertificate(certificateBase64);

            User savedUser = userRepository.save(newUser);

            DataTierProto.UserResponse response = convertToUserResponse(savedUser);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error creating user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUser(DataTierProto.GetUserRequest request, StreamObserver<DataTierProto.UserResponse> responseObserver) {
        try {
            Optional<User> userOpt = userRepository.getSingle(request.getId());
            
            if (userOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("User with id " + request.getId() + " not found")
                    .asRuntimeException());
                return;
            }
            
            User user = userOpt.get();
            DataTierProto.UserResponse response = convertToUserResponse(user);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error retrieving user: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void getUsers(DataTierProto.GetUsersRequest request, StreamObserver<DataTierProto.GetUsersResponse> responseObserver) {
        try {
            List<User> users;
            
            if (request.hasRoleId()) {
                users = userRepository.findAll().stream()
                    .filter(user -> user.getRoleId() != null && user.getRoleId().equals(request.getRoleId()))
                    .collect(Collectors.toList());
            } else {
                users = userRepository.findAll();
            }
            
            List<DataTierProto.UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            DataTierProto.GetUsersResponse response = DataTierProto.GetUsersResponse.newBuilder()
                .addAllUsers(userResponses)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error retrieving users: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void updateUser(DataTierProto.UpdateUserRequest request, StreamObserver<DataTierProto.UserResponse> responseObserver) {
        try {
            Optional<User> userOpt = userRepository.getSingle(request.getId());
            
            if (userOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("User with id " + request.getId() + " not found")
                    .asRuntimeException());
                return;
            }
            
            User user = userOpt.get();
            
            if (request.hasUsername()) {
                Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(request.getId())) {
                    responseObserver.onError(io.grpc.Status.ALREADY_EXISTS
                        .withDescription("Username already exists: " + request.getUsername())
                        .asRuntimeException());
                    return;
                }
                user.setUsername(request.getUsername());
            }

            if (request.hasPassword()) {
                String hashedPassword = PasswordHasher.hash(request.getPassword());
                user.setPassword(hashedPassword);
            }
            
            if (request.hasRoleId()) {
                user.setRoleId(request.getRoleId());
            }
            
            if (request.hasIsActive()) {
                user.setIsActive(request.getIsActive());
            }
            
            if (request.hasEmail()) {
                user.setEmail(request.getEmail().trim().isEmpty() ? null : request.getEmail());
            }
            
            User updatedUser = userRepository.save(user);
            
            DataTierProto.UserResponse response = convertToUserResponse(updatedUser);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error updating user: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void deleteUser(DataTierProto.DeleteUserRequest request, StreamObserver<DataTierProto.DeleteUserResponse> responseObserver) {
        try {
            Optional<User> userOpt = userRepository.getSingle(request.getId());
            
            if (userOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("User with id " + request.getId() + " not found")
                    .asRuntimeException());
                return;
            }
            
            userRepository.deleteById(request.getId());
            
            DataTierProto.DeleteUserResponse response = DataTierProto.DeleteUserResponse.newBuilder()
                .setSuccess(true)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error deleting user: " + e.getMessage())
                .asRuntimeException());
        }
    }

    private DataTierProto.UserResponse convertToUserResponse(User user) {
        DataTierProto.UserResponse.Builder builder = DataTierProto.UserResponse.newBuilder()
            .setId(user.getId() != null ? user.getId() : 0)
            .setUsername(user.getUsername() != null ? user.getUsername() : "")
            .setRoleId(user.getRoleId() != null ? user.getRoleId() : 0)
            .setIsActive(user.getIsActive() != null ? user.getIsActive() : true);
        
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            builder.setEmail(user.getEmail());
        }
        
        return builder.build();
    }
}

