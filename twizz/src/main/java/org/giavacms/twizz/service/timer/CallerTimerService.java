package org.giavacms.twizz.service.timer;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.giavacms.twizz.model.pojo.CallToMake;
import org.twiliofaces.cdi.doers.Caller;

import com.twilio.sdk.TwilioRestException;

@Stateless
@LocalBean
public class CallerTimerService
{

   @Inject
   Caller twilioCaller;

   @Resource
   TimerService timerService;

   public void createTimer(CallToMake callToMake)
   {
      System.out.println(callToMake);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(callToMake.getWhen());
      timerService.createCalendarTimer(
               new ScheduleExpression()
                        .hour(calendar.get(Calendar.HOUR_OF_DAY))
                        .minute(calendar.get(Calendar.MINUTE))
                        .second(calendar.get(Calendar.SECOND)),
               new TimerConfig(callToMake, true));
   }

   @Timeout
   public void timeout(Timer timer)
   {
      CallToMake callToMake = (CallToMake) timer.getInfo();
      System.out.println(getClass().getName() + ": " + new Date() + " " + callToMake);
      try
      {
         String callId = twilioCaller

                  .setTo(callToMake.getTo())
                  .url(callToMake.getUrl()).call();
         System.out.println("call id: " + callId);
      }
      catch (TwilioRestException e)
      {
         e.printStackTrace();
      }

   }

}
