package org.giavacms.paypal.producer;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.giavacms.paypal.model.PaypalConfiguration;
import org.giavacms.paypal.repository.PaypalConfigurationRepository;

@Named
@SessionScoped
public class PaypallProducer implements Serializable
{
   public PaypallProducer()
   {
   }

   private static final long serialVersionUID = 1L;
   @Inject
   PaypalConfigurationRepository paypalConfigurationRepository;

   public PaypalConfiguration getPaypalConfiguration()
   {
      return paypalConfigurationRepository.load();
   }

}