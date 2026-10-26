package cn.blockforge.meteorzhcn.util.online;

import java.util.List;

public interface Translator {
   List<String> translate(List<String> var1) throws Exception;

   int batchSize();

   int maxCharsPerBatch();
}
