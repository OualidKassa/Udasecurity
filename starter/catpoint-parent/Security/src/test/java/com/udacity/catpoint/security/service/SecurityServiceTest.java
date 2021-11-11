package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.SecurityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    private SecurityService securityService;

    private Set<StatusListener> statusListeners;

    @BeforeEach
    void init(){
        securityService = new SecurityService(securityRepository, imageService);
        statusListeners = new HashSet<>();
    }

    @Test
    public void ShouldCallSecurityRepositoryWhenChangeArmingStatus(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        Mockito.verify(securityRepository, Mockito.times(1)).setArmingStatus(ArmingStatus.DISARMED);
    }

    @Test
    public void test(){
        int a = 5;
        Assertions.assertEquals(5, a);
    }
}
