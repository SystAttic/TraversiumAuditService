package traversium.audit.grpc

import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService
import traversium.audit.metrics.AuditMetricsServiceGrpc
import traversium.audit.metrics.*
import traversium.audit.service.AuditMetricsService
import java.time.OffsetDateTime

@GrpcService
class AuditMetricsGrpcService(
    private val auditMetricsService: AuditMetricsService
) : AuditMetricsServiceGrpc.AuditMetricsServiceImplBase() {

    override fun getTotalUsersCreated(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalUsersCreated(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getActiveUsers(
        request: ActiveUsersRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getActiveUsers(request.tenantId, request.days)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getNewUsersInPeriod(
        request: DateRangeRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val startDate = OffsetDateTime.parse(request.startDate)
            val endDate = OffsetDateTime.parse(request.endDate)
            val count = auditMetricsService.getNewUsersInPeriod(request.tenantId, startDate, endDate)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTotalTripsCreated(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalTripsCreated(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTripsCreatedInPeriod(
        request: DateRangeRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val startDate = OffsetDateTime.parse(request.startDate)
            val endDate = OffsetDateTime.parse(request.endDate)
            val count = auditMetricsService.getTripsCreatedInPeriod(request.tenantId, startDate, endDate)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTotalMediaUploaded(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalMediaUploaded(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getMediaUploadedInPeriod(
        request: DateRangeRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val startDate = OffsetDateTime.parse(request.startDate)
            val endDate = OffsetDateTime.parse(request.endDate)
            val count = auditMetricsService.getMediaUploadedInPeriod(request.tenantId, startDate, endDate)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTotalStorageBytes(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalStorageBytes(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTotalSocialInteractions(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalSocialInteractions(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTotalLikes(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalLikes(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTotalComments(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalComments(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getSocialInteractionsInPeriod(
        request: DateRangeRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val startDate = OffsetDateTime.parse(request.startDate)
            val endDate = OffsetDateTime.parse(request.endDate)
            val count = auditMetricsService.getSocialInteractionsInPeriod(request.tenantId, startDate, endDate)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getTotalApiCalls(
        request: MetricsRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val count = auditMetricsService.getTotalApiCalls(request.tenantId)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }

    override fun getApiCallsInPeriod(
        request: DateRangeRequest,
        responseObserver: StreamObserver<CountResponse?>
    ) {
        try {
            val startDate = OffsetDateTime.parse(request.startDate)
            val endDate = OffsetDateTime.parse(request.endDate)
            val count = auditMetricsService.getApiCallsInPeriod(request.tenantId, startDate, endDate)
            val response = CountResponse.newBuilder()
                .setCount(count)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }
}

