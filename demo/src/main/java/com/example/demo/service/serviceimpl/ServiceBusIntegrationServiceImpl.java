package com.example.demo.service.serviceimpl;

import com.azure.messaging.servicebus.*;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.Request;
import com.example.demo.dto.Response;
import com.example.demo.service.ServiceBusIntegrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ServiceBusIntegrationServiceImpl implements ServiceBusIntegrationService {
    private final RestTemplate restTemplate = new RestTemplate();
//    private final ServiceBusReceiverClient receiverClient;
    private final ServiceBusClientBuilder serviceBusClientBuilder;
    private static final Logger log = LoggerFactory.getLogger(ServiceBusIntegrationServiceImpl.class);

    @Override
    public ResponseEntity<ApiResponse> externalApiCall(String city) throws JsonProcessingException {
        String url="https://bb810f3847f4e92b9b311a2bbaf3a9.4a.environment.api.powerplatform.com:443/powerautomate/automations/direct/workflows/3c228386e29344e2aed4930661bd916d/triggers/manual/paths/invoke/?api-version=1&sp=/triggers/manual/run&sv=1.0&sig=4mPrLoZcyz_43Q0Z95sdLg3S3CctGeaWAPoxs4dW468";
        // headers: api-key + JSON
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key",System.getenv("API_KEY"));                 // same as curl
        headers.setContentType(MediaType.APPLICATION_JSON);

        // body: { "city": "Ams" }
        Request request=new Request();
        request.setCity(city);
        // Convert object to JSON string
        ObjectMapper mapper=new ObjectMapper();
        String json = mapper.writeValueAsString(request);
        HttpEntity<Request> entity = new HttpEntity<>(request, headers);
        ApiResponse apiResponse=new ApiResponse();
        log.info("calling external api with request: " + json);
        try {
            ResponseEntity<String> res =
                    restTemplate.exchange(System.getenv("API_URL"), HttpMethod.POST, entity, String.class);
            String responseBody=Objects.nonNull(res.getBody()) ?res.getBody() :"";
            log.info("Completed api call successfully with response"+ responseBody);
            if(Objects.nonNull(res.getBody())){
                ServiceBusMessage msg = new ServiceBusMessage(responseBody.getBytes(StandardCharsets.UTF_8))
                        .setContentType("application/json")                  // if JSON
                        .setSubject("ThirdPartyResponse")
                        .setMessageId(UUID.randomUUID().toString());
                log.info("Sending a message with message body:"+ responseBody);
                try (ServiceBusSenderClient sender = serviceBusClientBuilder
                        .sender()
                        .queueName("samplequeue")
                        .buildClient()) {
                sender.sendMessage(msg);}

            }
            apiResponse.setResponse(mapper.readValue(res.getBody(), Response.class));

            return ResponseEntity.status(res.getStatusCode()).body(apiResponse);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }
/*    public List<String> peek(int max) {
        List<ServiceBusReceivedMessage> messages =  receiverClient.peekMessages(max).stream().toList();
        return messages.stream()
                .map(m -> {
                    String body = m.getBody().toString(); // assumes UTF-8 text body
                    Integer status = (Integer) m.getApplicationProperties().get("httpStatus");
                    return "status=" + status + ", body=" + body;
                })
                .collect(Collectors.toList());
    }*/



}
