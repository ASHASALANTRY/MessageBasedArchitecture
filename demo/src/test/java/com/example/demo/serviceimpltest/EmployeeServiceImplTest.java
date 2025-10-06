package com.example.demo.serviceimpltest;

import com.example.demo.Repository.BasicDetailRepository;
import com.example.demo.dto.CreateEmployeeRequest;
import com.example.demo.dto.StatusResponse;
import com.example.demo.entity.BasicDetails;
import com.example.demo.dto.EmployeesRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.IntegrationException;
import com.example.demo.service.CacheHelperService;
import com.example.demo.service.QueueHelperService;
import com.example.demo.service.serviceimpl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;


@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {
    @Mock
    QueueHelperService queueHelperService;
    @Mock
    BasicDetailRepository basicDetailRepository;
    @Mock
    CacheHelperService cacheHelperService;
    @InjectMocks
    EmployeeServiceImpl employeeService;

    private static final String BASE_STATUS_URL="https://status.example/";

    @BeforeEach
    void setStatusUrl()  throws Exception{
        Field declaredField=EmployeeServiceImpl.class.getDeclaredField("statusUrl");
        declaredField.setAccessible(Boolean.TRUE);
        declaredField.set(employeeService,BASE_STATUS_URL);
    }
    private EmployeesRequest validReq(){
        EmployeesRequest employeesRequest=new EmployeesRequest();
        employeesRequest.setEmployees(List.of(new CreateEmployeeRequest()));
        return employeesRequest;
    }

    @Test
    void getEmployeeDetails_returnDetails(){
        UUID employeeId=UUID.randomUUID();
        BasicDetails basicDetails=new BasicDetails();
        when(basicDetailRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(basicDetails));
        BasicDetails result = employeeService.getEmployeeDetails(employeeId);

        assertThat(result).isSameAs(basicDetails);
        verify(basicDetailRepository).findByEmployeeId(employeeId);
    }

    @Test
    void getEmployeeDetails_whenIdNotFound(){
        UUID employeeId =UUID.randomUUID();
        when(basicDetailRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());
        assertThrows(BadRequestException.class,()->employeeService.getEmployeeDetails(employeeId));
        verify(basicDetailRepository).findByEmployeeId(employeeId);
        verifyNoInteractions(queueHelperService);

    }
    @Test
    void addEmployeeDetails_nullRequest(){
        assertThatThrownBy(()->employeeService.addEmployeeDetails(null)).isInstanceOf(BadRequestException.class).hasMessageContaining("Request body cannot be null");
        verifyNoInteractions(queueHelperService,cacheHelperService);
    }

    @Test
    void addEmployeeDetails_emptyEmployees(){
        EmployeesRequest employeesRequest=new EmployeesRequest(); //employee list is null
        assertThatThrownBy(() ->employeeService.addEmployeeDetails(employeesRequest)).isInstanceOf(BadRequestException.class).hasMessageContaining("Employees list cannot be empty");
        employeesRequest.setEmployees(List.of()); //empty employee list
        assertThatThrownBy(()->employeeService.addEmployeeDetails(employeesRequest)).isInstanceOf(BadRequestException.class).hasMessageContaining("Employees list cannot be empty");
        verifyNoInteractions(queueHelperService,cacheHelperService);
    }

    @Test
    void addEmployeeDetailsTest_validScenario(){
        EmployeesRequest employeesRequest = validReq();
        EmployeesRequest result=employeeService.addEmployeeDetails((employeesRequest));
        //captures the random key passed
        ArgumentCaptor<String> keyCapture=ArgumentCaptor.forClass(String.class);
        verify(cacheHelperService).addMessageStatusToCache(keyCapture.capture(), eq("sent for processing"));
        String key=keyCapture.getValue();
        //Queue must be called with same request and same key
        verify(queueHelperService).sendStatusMessageToQueue(same(employeesRequest),eq(key));
        //verify order: cache first then queue
        InOrder inOrder=inOrder(cacheHelperService,queueHelperService);
        inOrder.verify(cacheHelperService).addMessageStatusToCache(key,"sent for processing");
        inOrder.verify(queueHelperService).sendStatusMessageToQueue(employeesRequest,key);

        //returned objects and mutation
        assertThat(result).isSameAs(employeesRequest);
        assertThat(employeesRequest.getIsProcessed()).isFalse();
        assertThat(employeesRequest.getStatusUrl()).isEqualTo(BASE_STATUS_URL +key);

        verifyNoMoreInteractions(cacheHelperService,queueHelperService);
    }
    @Test
    void addEmployeeDetailsTest_IntegrationException(){
        EmployeesRequest employeesRequest=validReq();
        doNothing().when(cacheHelperService).addMessageStatusToCache(anyString(),anyString());

        doThrow(new IntegrationException("Queue down")).when(queueHelperService)
                .sendStatusMessageToQueue(eq(employeesRequest),anyString());

        assertThatThrownBy(()->employeeService.addEmployeeDetails(employeesRequest))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("Queue down");

        verify(cacheHelperService).addMessageStatusToCache(anyString(), eq("sent for processing"));
        verify(queueHelperService).sendStatusMessageToQueue(eq(employeesRequest), anyString());
    }

    @Test
    void addEmployeeDetailsTest_unexpectedException(){
        EmployeesRequest employeesRequest=validReq();

        doNothing().when(cacheHelperService).addMessageStatusToCache(anyString(), anyString());
        RuntimeException exception=new RuntimeException("Broken pipe");
        doThrow(exception).when(queueHelperService).sendStatusMessageToQueue(eq(employeesRequest),anyString());
        assertThatThrownBy(()->employeeService.addEmployeeDetails(employeesRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unexpected error while processing employee details")
                .hasCause(exception);
        verify(cacheHelperService).addMessageStatusToCache(anyString(), eq("sent for processing"));
        verify(queueHelperService).sendStatusMessageToQueue(eq(employeesRequest), anyString());
    }

    @Test
    void getStatusTest_validScenario(){
        String key="abc123";
        when(cacheHelperService.getDataFromCache(key)).thenReturn("sent for processing");

        StatusResponse response = employeeService.getStatus(key);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("sent for processing");
        verify(cacheHelperService).getDataFromCache(key);
    }
    @Test
    void getStatus_cacheThrowsUnexpected_wrappedInBadRequest_withCause() {
        String key = "abc123";
        RuntimeException root = new RuntimeException("boom");
        doThrow(root).when(cacheHelperService).getDataFromCache(key);

        assertThatThrownBy(() -> employeeService.getStatus(key))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unexpected error while fetching data from cache")
                .hasCause(root);

        verify(cacheHelperService).getDataFromCache(key);
        verifyNoInteractions(queueHelperService, basicDetailRepository);
    }



}
