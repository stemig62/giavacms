/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.base.model.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public enum ResourceType
{

   FONT("font", Arrays.asList("otf", "eot", "svg", "ttf",
            "woff")),
   IMAGE("img", Arrays.asList("gif", "jpg", "jpeg", "png", "ico")),
   DOCUMENT("docs", Arrays.asList("zip", "gz", "tar", "bz2", "pdf", "doc", "docx", "xls", "xlsx", "p7m", "txt")),
   STYLESHEET("css", Arrays.asList("css")),
   JAVASCRIPT("js", Arrays.asList("js")),
   FLASH("swf", Arrays.asList("swf")),
   ALL("", new ArrayList<String>()),

   ;

   private String folder;
   private List<String> extensions;

   private ResourceType(String folder, List<String> extensions)
   {
      this.folder = folder;
      this.extensions = extensions;
   }

   public String getFolder()
   {
      return folder;
   }

   public List<String> getExtensions()
   {
      return extensions;
   }

   public static ResourceType getValueByFolder(String folder)
   {
      for (ResourceType res : ResourceType.values())
      {
         if (res.getFolder().equals(folder))
            return res;
      }
      return null;
   }

}
