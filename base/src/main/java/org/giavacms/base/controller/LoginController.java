/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.base.controller;

import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.giavacms.base.common.util.JSFLocalUtils;
import org.giavacms.base.common.util.PasswordUtils;
import org.giavacms.base.model.OperazioniLog;
import org.giavacms.base.model.UserAuth;
import org.giavacms.base.repository.UserRepository;
import org.giavacms.common.annotation.BackPage;
import org.giavacms.common.annotation.EditPage;
import org.giavacms.common.annotation.ListPage;
import org.giavacms.common.annotation.ViewPage;
import org.giavacms.common.controller.AbstractLazyController;
import org.giavacms.common.util.JSFUtils;

@Named
@SessionScoped
public class LoginController extends AbstractLazyController<UserAuth>
{

   private static final long serialVersionUID = 1L;

   // --------------------------------------------------------
   @BackPage
   public static String BACK = "/private/administration.xhtml";
   @ViewPage
   public static String VIEW = "/private/user/view.xhtml";
   @ListPage
   public static String LIST = "/private/user/list.xhtml";
   @EditPage
   public static String NEW_OR_EDIT = "/private/user/edit.xhtml";
   public static String CAMBIO_PASSWORD = "/private/user/change-password.xhtml";

   public static String LOGOUT = "/logout.jsp";

   // --------------------------------------------------------

   private UserAuth userAuth;

   @Inject
   UserRepository userRepository;

   @Inject
   LogOperationsController logOperationsController;

   private String username;
   private String password;

   public LoginController()
   {

   }

   public String login()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      HttpServletRequest request = (HttpServletRequest)
               context.getExternalContext().getRequest();
      try
      {
         request.login(this.username, this.password);
      }
      catch (ServletException e)
      {

         context.addMessage(null, new FacesMessage("Login failed."));
         return null;
      }
      return BACK;
   }

   public boolean isAdmin()
   {
      return JSFUtils.isUserInRole("admin");
   }

   public boolean isUser()
   {
      return !JSFUtils.isUserInRole("admin");
   }

   public boolean isInRole(String role)
   {
      return isAdmin() || JSFUtils.isUserInRole(role);
   }

   public String reset()
   {
      return BACK;
   }

   @PreDestroy
   public void destroy()
   {
      try
      {
         if (JSFLocalUtils.getPrincipal() != null)
         {
            logger.info("destroy: @PreDestroy");
            logOperationsController.save(OperazioniLog.LOGOUT,
                     JSFUtils.getUserName(), "logout operation");
            HttpSession session = JSFLocalUtils.getHttpSession();
            if (session != null)
            {
               session.invalidate();
            }
         }
         else
         {
            logger.info("destroy: session already ended");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public String logout()
   {
      logOperationsController.save(OperazioniLog.LOGOUT,
               JSFUtils.getUserName(), "operazione logout");
      ExternalContext extCtx = FacesContext.getCurrentInstance()
               .getExternalContext();
      try
      {
         extCtx.redirect(extCtx.encodeActionURL(JSFUtils.getContextPath()
                  + LOGOUT));
      }
      catch (Exception e)
      {

      }
      return null;
   }

   public String goToChangePassword()
   {
      this.userAuth = userRepository.findByUsername(JSFUtils.getUserName());
      return CAMBIO_PASSWORD + REDIRECT_PARAM;
   }

   public String changePassword()
   {
      if (getUserAuth().getOldPassword() == null || getUserAuth().getOldPassword().trim().isEmpty())
      {
         FacesMessage message = new FacesMessage();
         message.setDetail("La password corrente e' vuota!");
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         message.setSummary("Errore password corrente");
         FacesContext.getCurrentInstance().addMessage("pwd:opwd", message);
         return null;
      }
      String oldPassword = PasswordUtils.createPassword(getUserAuth().getOldPassword());
      if (!oldPassword.equals(getUserAuth().getPassword()))
      {
         FacesMessage message = new FacesMessage();
         message.setDetail("La password corrente non e' corretta!");
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         message.setSummary("Errore password corrente");
         FacesContext.getCurrentInstance().addMessage("pwd:opwd", message);
         return null;
      }
      if (getUserAuth().getNewPassword() == null
               || getUserAuth().getNewPassword().length() == 0)
      {
         FacesMessage message = new FacesMessage();
         message.setDetail("La nuova password non e' stata inserita in entrambi i campi di testo!");
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         message.setSummary("Errore nuova password");
         FacesContext.getCurrentInstance().addMessage("pwd:npwd", message);
         return null;
      }
      if (getUserAuth().getConfirmPassword() == null
               || getUserAuth().getConfirmPassword().length() == 0)
      {
         FacesMessage message = new FacesMessage();
         message.setDetail("La nuova password non e' stata inserita in entrambi i campi di testo!");
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         message.setSummary("Errore nuova password");
         FacesContext.getCurrentInstance().addMessage("pwd:cpwd", message);
         return null;
      }
      if (!userAuth.getNewPassword().equals(userAuth.getConfirmPassword()))
      {
         FacesMessage message = new FacesMessage();
         message.setDetail("Sono stati inseriti valori diversi nei due campi di testo relativi alla nuova password!");
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         message.setSummary("Errore nuova password");
         FacesContext.getCurrentInstance().addMessage("pwd:cpwd", message);
         return null;
      }
      String newPassword = PasswordUtils.createPassword(getUserAuth().getNewPassword());
      getUserAuth().setPassword(newPassword);
      userRepository.update(getUserAuth());
      logOperationsController.save(OperazioniLog.MODIFY,
               JSFUtils.getUserName(),
               "cambio pwd utente: " + this.userAuth.getUsername());
      ExternalContext extCtx = FacesContext.getCurrentInstance()
               .getExternalContext();
      try
      {
         extCtx.redirect(extCtx.encodeActionURL(LOGOUT));
      }
      catch (Exception e)
      {
         FacesMessage message = new FacesMessage(
                  "Errore generico, riprovare piu' tardi!");
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         message.setSummary("Errore generico");
         FacesContext.getCurrentInstance().addMessage("pwd:cpwd", message);
         return null;
      }
      return null;
   }

   public void checkRoles(ComponentSystemEvent event)
   {
      if (getUserAuth() == null && JSFUtils.getUserName() != null
               && !JSFUtils.getUserName().isEmpty())
      {
         userAuth = userRepository.findByUsername(JSFUtils.getUserName());
         setUserAuth(userAuth);
         logOperationsController.save("LOGIN", JSFUtils.getUserName(),
                  "LOGIN");
      }
      String acl = "" + event.getComponent().getAttributes().get("roles");
      for (String a : acl.split(","))
      {
         if ("ANY".equalsIgnoreCase(a))
         {
            if (JSFUtils.getUserName() != null
                     && JSFUtils.getUserName().length() > 0)
            {
               return;
            }
         }
         if (isInRole(a.trim()))
         {
            return;
         }
      }
      try
      {
         logger.info(acl + " - non consentito!");
         FacesContext context = FacesContext.getCurrentInstance();
         ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context
                  .getApplication().getNavigationHandler();
         handler.performNavigation("administration");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         // Se siamo qui il redirect è fallito.
         // A questo punto, piuttosto che lasciare andare l'utente dove
         // non deve.. runtime exception!
         throw new RuntimeException("Accesso non consentito");
      }
   }

   public UserAuth getUserAuth()
   {
      if (userAuth == null && JSFUtils.getUserName() != null
               && !JSFUtils.getUserName().isEmpty())
      {
         userAuth = userRepository.findByUsername(JSFUtils.getUserName());
         logOperationsController.save("LOGIN", JSFUtils.getUserName(),
                  "LOGIN");
         setUserAuth(userAuth);
      }
      return userAuth;
   }

   public void setUserAuth(UserAuth userAuth)
   {
      this.userAuth = userAuth;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

}
