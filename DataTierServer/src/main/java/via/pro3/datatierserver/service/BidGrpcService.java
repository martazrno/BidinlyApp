package via.pro3.datatierserver.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import via.pro3.datatierserver.grpc.BidServiceGrpc;
import via.pro3.datatierserver.grpc.DataTierProto;
import via.pro3.datatierserver.model.Bid;
import via.pro3.datatierserver.model.User;
import via.pro3.datatierserver.repositories.IBidRepository;
import via.pro3.datatierserver.repositories.IUserRepository;
import via.pro3.datatierserver.security.AESUtil;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class BidGrpcService extends BidServiceGrpc.BidServiceImplBase {

    @Autowired
    private IBidRepository bidRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public void createBid(DataTierProto.CreateBidRequest request, StreamObserver<DataTierProto.BidResponse> responseObserver) {
        try {
            //build bid
            Bid newBid = new Bid();
            newBid.setBuyerId(request.getBuyerId());
            newBid.setPropertyId(request.getPropertyId());
            newBid.setAmount(BigDecimal.valueOf(request.getAmount()));
            newBid.setExpiryDate(Instant.ofEpochSecond(request.getExpiryDateSeconds()));
            newBid.setStatus("Pending");
            newBid.setDeal(request.hasDeal() ? request.getDeal() : "");

            //Fetch buyer for signing
            Optional<User> userOpt = userRepository.getSingle(request.getBuyerId());
            if (userOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Buyer not found")
                        .asRuntimeException());
                return;
            }
            User buyer = userOpt.get();

            //decrypt private key
            String encryptedPrivateKey = buyer.getPrivateKey();
            String passwordHash = buyer.getPassword();
            String privateKeyBase64 = AESUtil.decrypt(encryptedPrivateKey, passwordHash);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // build message to sign
            String message = request.getBuyerId() + "|" +
                    request.getPropertyId() + "|" +
                    request.getAmount() + "|" +
                    request.getExpiryDateSeconds() + "|" +
                    (request.hasDeal() ? request.getDeal() : "");

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            //sign the msg
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(messageBytes);
            byte[] signatureBytes = signature.sign();
            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

            //save signature in bid
            newBid.setSignature(signatureBase64);

            //save bid
            Bid savedBid = bidRepository.save(newBid);

            DataTierProto.BidResponse response = convertToBidResponse(savedBid);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error creating bid: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getBids(DataTierProto.GetBidsRequest request, StreamObserver<DataTierProto.GetBidsResponse> responseObserver) {
        try {
            List<Bid> bids = bidRepository.getMany();
            
            updateExpiredBids(bids);
            
            if (request.hasPropertyId() || request.hasBuyerId() || request.hasStatus()) {
                bids = bids.stream()
                    .filter(bid -> {
                        if (request.hasPropertyId() && (bid.getPropertyId() == null || !bid.getPropertyId().equals(request.getPropertyId()))) {
                            return false;
                        }
                        if (request.hasBuyerId() && (bid.getBuyerId() == null || !bid.getBuyerId().equals(request.getBuyerId()))) {
                            return false;
                        }
                        if (request.hasStatus() && (bid.getStatus() == null || !bid.getStatus().equals(request.getStatus()))) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            }
            
            List<DataTierProto.BidResponse> bidResponses = bids.stream()
                .map(this::convertToBidResponse)
                .collect(Collectors.toList());
            
            DataTierProto.GetBidsResponse response = DataTierProto.GetBidsResponse.newBuilder()
                .addAllBids(bidResponses)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error retrieving bids: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void getBid(DataTierProto.GetBidRequest request, StreamObserver<DataTierProto.BidResponse> responseObserver) {
        try {
            Optional<Bid> bidOpt = bidRepository.getSingle(request.getId());
            
            if (bidOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Bid with id " + request.getId() + " not found")
                    .asRuntimeException());
                return;
            }
            
            Bid bid = bidOpt.get();
            
            updateExpiredBid(bid);
            
            DataTierProto.BidResponse response = convertToBidResponse(bid);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error retrieving bid: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void updateBid(DataTierProto.UpdateBidRequest request, StreamObserver<DataTierProto.BidResponse> responseObserver) {
        try {
            Optional<Bid> bidOpt = bidRepository.getSingle(request.getId());
            
            if (bidOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Bid with id " + request.getId() + " not found")
                    .asRuntimeException());
                return;
            }
            
            Bid bid = bidOpt.get();
            
            if (request.hasAmount()) {
                bid.setAmount(BigDecimal.valueOf(request.getAmount()));
            }
            
            if (request.hasExpiryDateSeconds()) {
                bid.setExpiryDate(Instant.ofEpochSecond(request.getExpiryDateSeconds()));
            }
            
            if (request.hasStatus()) {
                bid.setStatus(request.getStatus());
            }

            if (request.hasDeal()) {
                bid.setDeal(request.getDeal());
            }
            
            Bid updatedBid = bidRepository.save(bid);
            
            DataTierProto.BidResponse response = convertToBidResponse(updatedBid);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error updating bid: " + e.getMessage())
                .asRuntimeException());
        }
    }

    @Override
    public void deleteBid(DataTierProto.DeleteBidRequest request, StreamObserver<DataTierProto.DeleteBidResponse> responseObserver) {
        try {
            Optional<Bid> bidOpt = bidRepository.getSingle(request.getId());
            
            if (bidOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Bid with id " + request.getId() + " not found")
                    .asRuntimeException());
                return;
            }
            
            bidRepository.deleteById(request.getId());
            
            DataTierProto.DeleteBidResponse response = DataTierProto.DeleteBidResponse.newBuilder()
                .setSuccess(true)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Error deleting bid: " + e.getMessage())
                .asRuntimeException());
        }
    }

    private void updateExpiredBids(List<Bid> bids) {
        for (Bid bid : bids) {
            updateExpiredBid(bid);
        }
    }

    private boolean updateExpiredBid(Bid bid) {
        Instant now = Instant.now();
        
        if (bid.getExpiryDate() != null 
            && bid.getExpiryDate().isBefore(now) 
            && "Pending".equals(bid.getStatus())) {
            
            bid.setStatus("Expired");
            bidRepository.save(bid);
            return true;
        }
        
        return false;
    }

    private DataTierProto.BidResponse convertToBidResponse(Bid bid) {

        long expiryDateSeconds = bid.getExpiryDate() != null
                ? bid.getExpiryDate().getEpochSecond()
                : 0;

        boolean signatureValid = false;

        try {
            // fetch buyer public key
            Optional<User> userOpt = userRepository.getSingle(bid.getBuyerId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                String publicKeyBase64 = user.getPublicKey();
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);

                // recreate message
                String message = bid.getBuyerId() + "|" +
                        bid.getPropertyId() + "|" +
                        bid.getAmount().doubleValue() + "|" +
                        expiryDateSeconds + "|" +
                        (bid.getDeal() != null ? bid.getDeal() : "");

                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

                //decode signature
                if (bid.getSignature() != null && !bid.getSignature().isEmpty()) {
                    byte[] signatureBytes = Base64.getDecoder().decode(bid.getSignature());

                    //verify
                    Signature sig = Signature.getInstance("SHA256withRSA");
                    sig.initVerify(publicKey);
                    sig.update(messageBytes);
                    signatureValid = sig.verify(signatureBytes);
                }
            }

        } catch (Exception e) {
            signatureValid = false;
        }

        //build response
        return DataTierProto.BidResponse.newBuilder()
                .setId(bid.getId() != null ? bid.getId() : 0)
                .setBuyerId(bid.getBuyerId() != null ? bid.getBuyerId() : 0)
                .setPropertyId(bid.getPropertyId() != null ? bid.getPropertyId() : 0)
                .setAmount(bid.getAmount() != null ? bid.getAmount().doubleValue() : 0.0)
                .setExpiryDateSeconds(expiryDateSeconds)
                .setStatus(bid.getStatus() != null ? bid.getStatus() : "Pending")
                .setDeal(bid.getDeal() != null ? bid.getDeal() : "")
                .setSignature(bid.getSignature() != null ? bid.getSignature() : "")
                .setSignatureValid(signatureValid) // ⭐ NEW FIELD
                .build();
    }

    @Override
    public void setBidStatus(DataTierProto.SetBidStatusRequest request,
                             StreamObserver<DataTierProto.BidResponse> responseObserver) {
        try {
            Optional<Bid> bidOpt = bidRepository.getSingle(request.getId());

            if (bidOpt.isEmpty()) {
                responseObserver.onError(
                        io.grpc.Status.NOT_FOUND
                                .withDescription("Bid with id " + request.getId() + " not found")
                                .asRuntimeException()
                );
                return;
            }

            Bid acceptedBid = bidOpt.get();

            acceptedBid.setStatus(request.getStatus());
            bidRepository.save(acceptedBid);

            if ("Accepted".equals(request.getStatus())) {
                List<Bid> allBids = bidRepository.getMany();

                for (Bid bid : allBids) {
                    if (!bid.getId().equals(acceptedBid.getId()) &&
                            bid.getPropertyId().equals(acceptedBid.getPropertyId()) &&
                            !"Rejected".equals(bid.getStatus())) {

                        bid.setStatus("Rejected");
                        bidRepository.save(bid);
                    }
                }
            }

            DataTierProto.BidResponse response = convertToBidResponse(acceptedBid);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Error updating bid status: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}

