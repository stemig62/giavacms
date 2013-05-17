package org.giavacms.richcontent.controller.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.giavacms.base.controller.AbstractPageRequestController;
import org.giavacms.base.controller.request.PageRequestController;
import org.giavacms.base.pojo.I18nRequestParams;
import org.giavacms.common.annotation.OwnRepository;
import org.giavacms.common.model.Search;
import org.giavacms.common.util.BeanUtils;
import org.giavacms.richcontent.model.RichContent;
import org.giavacms.richcontent.model.type.RichContentType;
import org.giavacms.richcontent.repository.RichContentRepository;
import org.giavacms.richcontent.repository.RichContentTypeRepository;

@Named
@RequestScoped
public class RichContentRequestController extends
         AbstractPageRequestController<RichContent> implements Serializable
{

   private static final long serialVersionUID = 1L;

   public static final String PARAM_CONTENT = "q";
   public static final String PARAM_TYPE = "type";
   // public static final String ID_PARAM = "id";
   public static final String CURRENT_PAGE_PARAM = "currentpage";
   public static final String[] PARAM_NAMES = new String[] { PARAM_CONTENT,
            PARAM_TYPE,
            // ID_PARAM,
            CURRENT_PAGE_PARAM };

   @Inject
   @OwnRepository(RichContentRepository.class)
   RichContentRepository richContentRepository;

   @Inject
   RichContentTypeRepository richContentTypeRepository;

   @Inject
   PageRequestController pageRequestController;

   private String filter;

   private RichContent last;

   public RichContentRequestController()
   {
      super();
   }

   @Override
   public void initParameters()
   {
      super.initParameters();
      this.testI18N();
   }

   @Override
   public List<RichContent> loadPage(int startRow, int pageSize)
   {
      Search<RichContent> r = new Search<RichContent>(RichContent.class);
      r.getObj().setTitle(getParams().get(PARAM_CONTENT));
      r.getObj().getRichContentType().setName(getParams().get(PARAM_TYPE));
      if (getFilter() != null && !getFilter().isEmpty())
      {
         r.getObj().getRichContentType().setName(getFilter());
      }
      return richContentRepository.getList(r, startRow, pageSize);
   }

   public List<RichContent> getLatest(int pageSize)
   {
      logger.info("getLatest:" + pageSize);
      Search<RichContent> r = new Search<RichContent>(RichContent.class);
      return richContentRepository.getList(r, 0, pageSize);
   }

   public List<RichContent> getPageOfSizeWithCategory(int size, String category)
   {
      setFilter(category);
      setPageSize(size);
      return getPage();
   }

   @Override
   public int totalSize()
   {
      Search<RichContent> r = new Search<RichContent>(RichContent.class);
      r.getObj().getRichContentType().setName(getParams().get(PARAM_TYPE));
      r.getObj().setTitle(getParams().get(PARAM_CONTENT));
      if (getFilter() != null && !getFilter().isEmpty())
      {
         r.getObj().getRichContentType().setName(getFilter());
      }
      return richContentRepository.getListSize(r);
   }

   @Override
   public String[] getParamNames()
   {
      return PARAM_NAMES;
   }

   @Override
   public String getIdParam()
   {
      throw new RuntimeException("Should not be called. getIdValue override implement");
   }

   public List<String> getTipiRichContent()
   {
      Search<RichContentType> r = new Search<RichContentType>(RichContentType.class);
      List<RichContentType> rntl = richContentTypeRepository.getList(r, 0, 0);
      List<String> l = new ArrayList<String>();
      for (RichContentType rnt : rntl)
      {
         l.add(rnt.getName());
      }
      return l;
   }

   public String getRichContentTypeOptionsHTML()
   {
      StringBuffer sb = new StringBuffer();
      Search<RichContentType> r = new Search<RichContentType>(RichContentType.class);
      List<RichContentType> rntl = richContentTypeRepository.getList(r, 0, 0);
      for (RichContentType richContentType : rntl)
      {
         sb.append("<option value=\"")
                  .append(richContentType.getName())
                  .append("\"")
                  .append(richContentType.getName().equals(
                           getParams().get(PARAM_TYPE)) ? " selected=\"selected\""
                           : "").append(">").append(richContentType.getName())
                  .append("</option>");
      }
      return sb.toString();
   }

   @Override
   public String getCurrentPageParam()
   {
      return CURRENT_PAGE_PARAM;
   }

   public boolean isScheda()
   {
      return getElement() != null && getElement().getId() != null;
   }

   public String getFilter()
   {
      return filter;
   }

   public void setFilter(String filter)
   {
      this.filter = filter;
   }

   public RichContent getLast(String category)
   {
      if (last == null)
      {
         RichContent last = richContentRepository.getLast(category);
         this.last = last;
      }
      return last;
   }

   @Override
   protected Object getIdValue()
   {
      return pageRequestController.getElement().getId();
   }

   protected void testI18N()
   {

      String testName = "test";
      I18nRequestParams i18nRequestParams = super.getI18nRequestParams();

      int currentLang = ((PageRequestController) BeanUtils
               .getBean(PageRequestController.class)).getElement().getLang();
      String testValue = i18nRequestParams.get(currentLang, testName);
      if (testValue != null && testValue.trim().length() > 0)
      {
         for (int i = 0; i < i18nRequestParams.getLanguages().length; i++)
         {
            if (i == currentLang)
            {
               continue;
            }
            i18nRequestParams
                     .put(i,
                              testName,
                              (i18nRequestParams.getLanguages()[i] == null || !i18nRequestParams
                                       .getLanguages()[i].isEnabled()) ? "UNSUPPORTED"
                                       : (testValue + "_" + i18nRequestParams
                                                .getLanguages()[i].getId()));
         }
      }
   }
}
