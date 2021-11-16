package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import net.bytebuddy.utility.RandomString;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SecurityServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private Sensor sensor;

    private SecurityService securityService;

    @Mock
    private StatusListener statusListeners;

    @Captor
    ArgumentCaptor<AlarmStatus> captor;

    private Set<Sensor> getSensors(boolean active, int count) {
        String randomString = UUID.randomUUID().toString();

        Set<Sensor> sensors = new HashSet<>();
        for (int i = 0; i <= count; i++) {
            sensors.add(new Sensor(randomString, SensorType.DOOR));
        }
        sensors.forEach(it -> it.setActive(active));
        return sensors;
    }

    @BeforeEach
    void init(){
        securityService = new SecurityService(securityRepository, imageService);
        sensor = new Sensor(new RandomString().nextString(), SensorType.DOOR);
    }

    @Test
    public void shouldCallSecurityRepositoryWhenChangeArmingStatus(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        Mockito.verify(securityRepository, Mockito.times(1)).setArmingStatus(ArmingStatus.DISARMED);
    }

    @Test
   public void shouldCallCatDetectedWhenProcessImageCall(){
        BufferedImage bufferedImage = new BufferedImage(100,120,TYPE_INT_RGB);
        securityService.processImage(bufferedImage);
        Mockito.verify(imageService, Mockito.times(1)).imageContainsCat(bufferedImage, 50.0f);
   }

   @Test
   public void should_System_Into_Pending_Status_When_Sensor_Active_And_Alarm_Is_Armed(){
       Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
       Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
       securityService.changeSensorActivationStatus(sensor,true);
       Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
   }

   @Test
    public void should_System_Into_Status_Alarm_When_Sensor_Active_And_Alarm_Is_Armed_And_System_already_Pending(){
       Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
       Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
       securityService.changeSensorActivationStatus(sensor,true);
       Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void should_System_No_Alarm_State_When_Pending_Alarm_And_System_Inactive(){
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor,false);
        Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void changeAlarmState_sensorDeactivateWhileInactive_noChangeToAlarmState() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        sensor.setActive(Boolean.FALSE);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    public void should_System_Alarm_State_when_Sensor_Is_Active_And_System_Pending_State(){
        Set<Sensor> sensors = Set.of(sensor, new Sensor(UUID.randomUUID().toString(), SensorType.WINDOW));
        Mockito.when(securityRepository.getSensors()).thenReturn(sensors);
        Mockito.when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.ALARM);
    }

    @Test
    public void should_system_in_alarm_status_when_image_system_detect_cat_and_system_is_armed_home(){
        Mockito.when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        Mockito.when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void should_system_in_no_alarm_status_when_image_system_no_detect_cat_as_long_captor_not_active(){
        sensor.setActive(false);
        Set<Sensor> sensors = Set.of(sensor, new Sensor(UUID.randomUUID().toString(), SensorType.WINDOW));
        Mockito.when(securityRepository.getSensors()).thenReturn(sensors);
        Mockito.when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(mock(BufferedImage.class));
        Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void should_status_not_alarm_when_system_is_disarmed(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void resetAllSensors_IfTheSystemIsArmed() {
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        assertTrue(securityService.getSensors().stream().allMatch(sensor -> Boolean.FALSE.equals(sensor.getActive())));
    }

    @Test
    void setAlarmStatusToAlarm_systemArmedHomeAndCatDetected() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.ALARM);
    }



    @Test
    public void addAndRemoveSensor() {
        Sensor sensor = new Sensor("test", SensorType.DOOR);
        securityService.addSensor(sensor);
        assertNotNull(securityService.getSensors());
        securityService.removeSensor(sensor);
    }
}
