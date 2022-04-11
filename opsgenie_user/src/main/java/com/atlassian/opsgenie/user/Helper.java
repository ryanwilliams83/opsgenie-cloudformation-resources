package com.atlassian.opsgenie.user;

import com.atlassian.opsgenie.user.client.OpsgenieClient;
import com.atlassian.opsgenie.user.client.OpsgenieClientException;
import org.apache.commons.lang3.StringUtils;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author Furkan Yildiz
 * @created 7.04.2022
 */
public class Helper {
    public static void Delay(TypeConfigurationModel typeConfiguration) throws InterruptedException {
        if (typeConfiguration != null) {
            ContractTestSettings contractTestSettings = typeConfiguration.getContractTestSettings();
            if (contractTestSettings != null) {
                Boolean delayEnabled = contractTestSettings.getDelayEnabled();
                if (delayEnabled != null && delayEnabled.equals(Boolean.TRUE)) {
                    Integer delaySeconds = contractTestSettings.getDelaySeconds();
                    if (delaySeconds != null) TimeUnit.SECONDS.sleep(delaySeconds);
                }
            }
        }
    }

    public static OpsgenieClient CreateOGClient(TypeConfigurationModel typeConfiguration) throws OpsgenieClientException {
        String opsgenieApiEndpoint = null;
        String opsgenieApiKey = null;

        if (typeConfiguration != null && typeConfiguration.getOpsgenieCredentials() != null) {
            opsgenieApiEndpoint = typeConfiguration.getOpsgenieCredentials()
                                                   .getOpsgenieApiEndpoint();
            opsgenieApiKey = typeConfiguration.getOpsgenieCredentials()
                                              .getOpsgenieApiKey();
        }

        if (StringUtils.isEmpty(opsgenieApiEndpoint) || StringUtils.isEmpty(opsgenieApiKey)) {
            throw new OpsgenieClientException("OpsgenieApiEndpoint and OpsgenieApiKey must be provided in TypeConfiguration.", 401);
        }

        return new OpsgenieClient(opsgenieApiEndpoint, opsgenieApiKey);
    }

    public static ProgressEvent<ResourceModel, CallbackContext> GetInternalFailureResponse(String message) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .errorCode(HandlerErrorCode.InternalFailure)
                            .message(message)
                            .status(OperationStatus.FAILED)
                            .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> GetServiceFailureResponse(int responseCode, String message) {
        HandlerErrorCode errorCode = HandlerErrorCode.GeneralServiceException;
        if (responseCode == 400) {
            errorCode = HandlerErrorCode.InvalidRequest;
        } else if (responseCode == 401) {
            errorCode = HandlerErrorCode.InvalidCredentials;
        } else if (responseCode == 403) {
            errorCode = HandlerErrorCode.AccessDenied;
        } else if (responseCode == 404) {
            errorCode = HandlerErrorCode.NotFound;
        } else if (responseCode == 409) {
            errorCode = HandlerErrorCode.AlreadyExists;
        } else if (responseCode == 429) {
            errorCode = HandlerErrorCode.Throttling;
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .errorCode(errorCode)
                            .message(message)
                            .status(OperationStatus.FAILED)
                            .build();
    }

}
