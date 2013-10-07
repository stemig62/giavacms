package org.giavacms.paypal.controller.conversation;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.giavacms.common.util.JSFUtils;
import org.giavacms.paypal.model.ShoppingArticle;
import org.giavacms.paypal.model.ShoppingCart;
import org.giavacms.paypal.producer.PaypallProducer;
import org.giavacms.paypal.repository.ShoppingCartRepository;
import org.giavacms.paypal.service.ShippingAmountService;
import org.giavacms.paypal.util.PaypalUtils;

@Named
@ConversationScoped
public class ShoppingCartConversationController implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Inject
   Conversation conversation;
   private String lastPage;
   private ShoppingCart element;

   @Inject
   PaypallProducer paypallProducer;

   @Inject
   ShoppingCartRepository shoppingCartRepository;

   @Inject
   ShippingAmountService shippingAmountService;

   public ShoppingCartConversationController()
   {
   }

   public String addProduct(String vat,
            String price,
            String idProduct,
            String description, String quantity)
   {
      beginConversation();
      getElement().addArticle(new ShoppingArticle(idProduct, description, price, quantity, vat));
      setLastPage(FacesContext.getCurrentInstance().getViewRoot().getId());
      return paypallProducer.getPaypalConfiguration().getShoppingCartUrl();
   }

   public void gotoPaypal()
   {
      String shippingAmount = shippingAmountService.calculate(getElement());
      getElement().setShipping(shippingAmount);
      shoppingCartRepository.persist(getElement());
      try
      {
         PaypalUtils.init(paypallProducer.getPaypalConfiguration(), getElement());
         shoppingCartRepository.update(getElement());
         if (getElement().isCreated())
            JSFUtils.redirect(getElement().getApprovalUrl());
         else
         {
            System.out.println("ERROR!!!");
            return;
         }
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void gotoLastPage()
   {
      try
      {
         JSFUtils.redirect(getLastPage());
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void reset()
   {
      this.element = new ShoppingCart(paypallProducer.getPaypalConfiguration().getCurrency());
   }

   private void beginConversation()
   {
      if (conversation.isTransient())
      {
         conversation.begin();
         conversation.setTimeout(30 * 60);
         this.element = new ShoppingCart(paypallProducer.getPaypalConfiguration().getCurrency());
         return;
      }

      throw new IllegalStateException();
   }

   private void endConversation()
   {
      if (!conversation.isTransient())
      {
         conversation.end();
         return;
      }

      throw new IllegalStateException();
   }

   public void finish()
   {
      endConversation();
   }

   public ShoppingCart getElement()
   {
      if (this.element == null)
         this.element = new ShoppingCart();
      return element;
   }

   public String getLastPage()
   {
      return lastPage;
   }

   public void setLastPage(String lastPage)
   {
      this.lastPage = lastPage;
   }

}
