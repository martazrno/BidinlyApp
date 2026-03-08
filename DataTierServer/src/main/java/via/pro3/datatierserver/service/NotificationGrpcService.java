package via.pro3.datatierserver.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import via.pro3.datatierserver.grpc.DataTierProto;
import via.pro3.datatierserver.grpc.NotificationServiceGrpc;
import via.pro3.datatierserver.model.Notification;
import via.pro3.datatierserver.repositories.INotificationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {

    @Autowired
    private INotificationRepository notificationRepository;

    @Override
    public void createNotification(DataTierProto.CreateNotificationRequest request,
                                  StreamObserver<DataTierProto.NotificationResponse> responseObserver) {
        try {

            Notification newNotification = new Notification();
            newNotification.setBidId(request.getBidId());
            newNotification.setPropertyId(request.getPropertyId());
            newNotification.setMessage(request.getMessage());
            newNotification.setIsRead(false);
            newNotification.setCreatedAt(Instant.now());

            if (!request.hasUserId()) {
                responseObserver.onError(
                        io.grpc.Status.INVALID_ARGUMENT
                                .withDescription("UserId is required for notification")
                                .asRuntimeException()
                );
                return;
            }

            newNotification.setUserId(request.getUserId());

            if (request.hasStatus()) {
                newNotification.setStatus(request.getStatus());
            }

            if (request.hasPropertyTitle() && !request.getPropertyTitle().isEmpty()) {
                newNotification.setPropertyTitle(request.getPropertyTitle());
            }

            Notification savedNotification = notificationRepository.save(newNotification);

            DataTierProto.NotificationResponse response = convertToNotificationResponse(savedNotification);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error creating notification: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getNotifications(DataTierProto.GetNotificationsRequest request,
                                 StreamObserver<DataTierProto.GetNotificationsResponse> responseObserver) {
        try {
            List<Notification> notifications;

            if (request.hasUserId()) {
                if (request.hasIsRead()) {
                    notifications = notificationRepository
                            .findByUserIdAndIsRead(request.getUserId(), request.getIsRead());
                } else {
                    notifications = notificationRepository
                            .findByUserId(request.getUserId());
                }
            } else {
                notifications = notificationRepository.findAll();
            }

            List<DataTierProto.NotificationResponse> notificationResponses = notifications.stream()
                    .map(this::convertToNotificationResponse)
                    .collect(Collectors.toList());

            DataTierProto.GetNotificationsResponse response = DataTierProto.GetNotificationsResponse.newBuilder()
                    .addAllNotifications(notificationResponses)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error retrieving notifications: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getNotification(DataTierProto.GetNotificationRequest request,
                                StreamObserver<DataTierProto.NotificationResponse> responseObserver) {
        try {
            Optional<Notification> notificationOpt =
                    notificationRepository.findById(request.getId());

            if (notificationOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Notification with id " + request.getId() + " not found")
                        .asRuntimeException());
                return;
            }

            Notification notification = notificationOpt.get();
            DataTierProto.NotificationResponse response = convertToNotificationResponse(notification);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error retrieving notification: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void markNotificationAsRead(DataTierProto.MarkNotificationAsReadRequest request,
                                       StreamObserver<DataTierProto.NotificationResponse> responseObserver) {
        try {
            Optional<Notification> notificationOpt =
                    notificationRepository.findById(request.getId());

            if (notificationOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Notification with id " + request.getId() + " not found")
                        .asRuntimeException());
                return;
            }

            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            Notification updatedNotification = notificationRepository.save(notification);

            DataTierProto.NotificationResponse response = convertToNotificationResponse(updatedNotification);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error marking notification as read: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private DataTierProto.NotificationResponse convertToNotificationResponse(Notification notification) {
        DataTierProto.NotificationResponse.Builder builder = DataTierProto.NotificationResponse.newBuilder()
                .setId(notification.getId() != null ? notification.getId() : 0)
                .setBidId(notification.getBidId() != null ? notification.getBidId() : 0)
                .setPropertyId(notification.getPropertyId() != null ? notification.getPropertyId() : 0)
                .setMessage(notification.getMessage() != null ? notification.getMessage() : "")
                .setIsRead(notification.getIsRead() != null ? notification.getIsRead() : false);

        if (notification.getUserId() != null) {
            builder.setUserId(notification.getUserId());
        }

        if (notification.getStatus() != null && !notification.getStatus().isEmpty()) {
            builder.setStatus(notification.getStatus());
        }

        if (notification.getCreatedAt() != null) {
            builder.setCreatedAtSeconds(notification.getCreatedAt().getEpochSecond());
        }

        if (notification.getPropertyTitle() != null && !notification.getPropertyTitle().isEmpty()) {
            builder.setPropertyTitle(notification.getPropertyTitle());
        }

        return builder.build();
    }
}

