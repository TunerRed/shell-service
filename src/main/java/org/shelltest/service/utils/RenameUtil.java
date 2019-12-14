package org.shelltest.service.utils;

import java.util.List;

public class RenameUtil {
    public static String getRename(String originName, List<String> prefixList, List<String> suffixList) {
        String rename = originName;
        if (prefixList != null)
            for (int i = 0; i < prefixList.size(); i++)
                if (rename.startsWith(prefixList.get(i)))
                    rename = rename.substring(prefixList.get(i).length());
        if (suffixList != null)
            for (int i = 0; i < suffixList.size(); i++)
                if (rename.endsWith(suffixList.get(i)))
                    rename = rename.substring(0, rename.length() - suffixList.get(i).length());
        return rename;
    }
}
